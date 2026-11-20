package com.develop.pocketsplit.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.develop.pocketsplit.data.local.PocketSplitDatabase
import com.develop.pocketsplit.data.repository.FinanceRepository
import com.develop.pocketsplit.model.Finance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinanceRepositoryTest {

    private lateinit var db: PocketSplitDatabase
    private lateinit var repository: FinanceRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PocketSplitDatabase::class.java).build()
        repository = FinanceRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeFinances_initiallyEmpty() = runBlocking {
        assertTrue(repository.observeFinances().first().isEmpty())
    }

    @Test
    fun addFinance_appearsInList() = runBlocking {
        repository.addFinance("Viaje a Tenerife", isGroup = true)

        val finances = repository.observeFinances().first()

        assertEquals(1, finances.size)
        assertEquals("Viaje a Tenerife", finances[0].name)
        assertTrue(finances[0].isGroup)
    }

    @Test
    fun addFinance_trimsName() = runBlocking {
        repository.addFinance("  Piso compartido  ", isGroup = false)

        assertEquals("Piso compartido", repository.observeFinances().first()[0].name)
    }

    @Test
    fun observeFinance_byId_returnsThatFinance() = runBlocking {
        repository.addFinance("Gastos personales", isGroup = false)
        val id = repository.observeFinances().first()[0].id

        assertEquals("Gastos personales", repository.observeFinance(id).first()?.name)
    }

    @Test
    fun observeFinance_unknownId_emitsNull() = runBlocking {
        assertNull(repository.observeFinance("no-existe").first())
    }

    @Test
    fun renameFinance_updatesNameAndKeepsId() = runBlocking {
        repository.addFinance("Piso", isGroup = true)
        val original = repository.observeFinances().first()[0]

        repository.renameFinance(original, "Piso compartido")

        val updated = repository.observeFinance(original.id).first()
        assertEquals("Piso compartido", updated?.name)
        assertEquals(original.id, updated?.id)
    }

    @Test
    fun deleteFinance_removesItFromList() = runBlocking {
        repository.addFinance("A borrar", isGroup = false)
        val id = repository.observeFinances().first()[0].id

        repository.deleteFinance(id)

        assertTrue(repository.observeFinances().first().isEmpty())
        assertNull(repository.observeFinance(id).first())
    }

    @Test
    fun observeFinances_ordersByCreationDate() = runBlocking {
        // Se insertan al revés para comprobar que el orden lo da el ORDER BY
        db.financeDao().insert(Finance(name = "Segunda", isGroup = false, createdAt = 2_000L))
        db.financeDao().insert(Finance(name = "Primera", isGroup = false,createdAt = 1_000L))

        val names = repository.observeFinances().first().map { it.name }

        assertEquals(listOf("Primera", "Segunda"), names)
    }
}