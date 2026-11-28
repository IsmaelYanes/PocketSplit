package com.develop.pocketsplit.data.repository

import com.develop.pocketsplit.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Gestión de usuarios con Firebase.
 *
 * - Firebase Authentication: credenciales y sesión (correo + contraseña).
 * - Cloud Firestore, colección `users/{uid}`: perfil del usuario.
 *
 * CRUD: registro (create), observación del perfil (read), cambio de nombre (update)
 * y baja de la cuenta (delete).
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val users get() = firestore.collection(USERS_COLLECTION)

    /** Fuerza a releer el perfil (p. ej. tras confirmar un cambio de correo). */
    private val refreshTrigger = MutableStateFlow(0)

    // `true` si Firebase ya tiene una sesión persistida (se lee de forma síncrona).
    fun hasSession(): Boolean = auth.currentUser != null

    // ---------------------------------------------------------------- READ

    private fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Perfil del usuario con sesión (`null` si no hay sesión). Se actualiza en tiempo real. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeCurrentUser(): Flow<User?> =
        combine(observeAuthState(), refreshTrigger) { firebaseUser, _ -> firebaseUser }
            .flatMapLatest { firebaseUser ->
                if (firebaseUser == null) flowOf(null) else observeProfile(firebaseUser)
            }

    private fun observeProfile(firebaseUser: FirebaseUser): Flow<User> = callbackFlow {
        val fallback = firebaseUser.toUser()
        val registration = users.document(firebaseUser.uid).addSnapshotListener { snapshot, error ->
            // Si el documento aún no existe (o no se puede leer) se usan los datos de Auth.
            val profile = if (error == null) snapshot?.toObject(User::class.java) else null
            // El correo de Authentication manda: es el que se verifica al cambiarlo.
            trySend(profile?.let { it.copy(email = fallback.email.ifBlank { it.email }) } ?: fallback)
        }
        awaitClose { registration.remove() }
    }

    // -------------------------------------------------------------- CREATE

    suspend fun register(name: String, email: String, password: String): Result<Unit> = safeCall {
        val firebaseUser = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: throw NotLoggedInException()

        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(name).build()
        ).await()

        val profile = User(id = firebaseUser.uid, name = name, email = email)
        saveProfile { users.document(firebaseUser.uid).set(profile) }
    }

    // ---------------------------------------------------------------- AUTH

    suspend fun login(email: String, password: String): Result<Unit> = safeCall {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    fun logout() = auth.signOut()

    suspend fun sendPasswordReset(email: String): Result<Unit> = safeCall {
        auth.sendPasswordResetEmail(email).await()
        Unit
    }

    // -------------------------------------------------------------- UPDATE

    suspend fun updateName(name: String): Result<Unit> = safeCall {
        val firebaseUser = auth.currentUser ?: throw NotLoggedInException()

        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(name).build()
        ).await()

        val data = mapOf(
            "id" to firebaseUser.uid,
            "name" to name,
            "email" to firebaseUser.email.orEmpty()
        )
        // merge: no pisa otros campos del perfil que puedan añadirse en el futuro.
        saveProfile { users.document(firebaseUser.uid).set(data, SetOptions.merge()) }
    }

    /**
     * Cambia el correo. Firebase envía un enlace de verificación al correo nuevo y el
     * cambio solo se aplica cuando el usuario lo confirma. Requiere reautenticar.
     */
    suspend fun changeEmail(newEmail: String, password: String): Result<Unit> = safeCall {
        val firebaseUser = auth.currentUser ?: throw NotLoggedInException()
        reauthenticate(firebaseUser, password)
        firebaseUser.verifyBeforeUpdateEmail(newEmail).await()
        Unit
    }

    /** Cambia la contraseña. Requiere reautenticar con la actual. */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = safeCall {
        val firebaseUser = auth.currentUser ?: throw NotLoggedInException()
        reauthenticate(firebaseUser, currentPassword)
        firebaseUser.updatePassword(newPassword).await()
        Unit
    }

    /**
     * Recarga el usuario desde Firebase. Sirve para reflejar un cambio de correo
     * confirmado fuera de la app; si el correo cambió, se actualiza también el perfil.
     */
    suspend fun refreshUser(): Result<Unit> = safeCall {
        val firebaseUser = auth.currentUser ?: return@safeCall
        val previousEmail = firebaseUser.email
        firebaseUser.reload().await()
        if (firebaseUser.email != previousEmail) {
            saveProfile {
                users.document(firebaseUser.uid)
                    .set(mapOf("email" to firebaseUser.email.orEmpty()), SetOptions.merge())
            }
        }
        refreshTrigger.update { it + 1 }
    }

    // -------------------------------------------------------------- DELETE

    /**
     * Elimina la cuenta y su perfil. Firebase exige una sesión reciente para borrar
     * una cuenta, por lo que se reautentica con la contraseña antes de nada.
     */
    suspend fun deleteAccount(password: String): Result<Unit> = safeCall {
        val firebaseUser = auth.currentUser ?: throw NotLoggedInException()

        reauthenticate(firebaseUser, password)
        saveProfile { users.document(firebaseUser.uid).delete() }
        firebaseUser.delete().await()
        Unit
    }

    // ------------------------------------------------------------- HELPERS

    private suspend fun reauthenticate(firebaseUser: FirebaseUser, password: String) {
        val email = firebaseUser.email ?: throw NotLoggedInException()
        firebaseUser.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
    }

    /**
     * Firestore encola las escrituras sin conexión y su `Task` no se completa hasta
     * que el servidor responde. Para no dejar la interfaz esperando indefinidamente,
     * se limita la espera: la escritura sigue encolada y se sincroniza al recuperar red.
     */
    private suspend fun saveProfile(write: () -> com.google.android.gms.tasks.Task<Void>) {
        withTimeoutOrNull(WRITE_TIMEOUT_MS) { write().await() }
    }

    /** Como `runCatching`, pero sin tragarse la cancelación de la corrutina. */
    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun FirebaseUser.toUser() =
        User(id = uid, name = displayName.orEmpty(), email = email.orEmpty())

    private companion object {
        const val USERS_COLLECTION = "users"
        const val WRITE_TIMEOUT_MS = 5_000L
    }
}