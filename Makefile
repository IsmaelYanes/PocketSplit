.PHONY: build run test

build:
	bash ./gradlew :app:assembleDebug

run: build
	bash scripts/run-android.sh

test:
	bash ./gradlew :app:testDebugUnitTest
