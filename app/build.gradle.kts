plugins {
	id("com.android.application")
	kotlin("android")
}

val releaseKeystorePath = providers.environmentVariable("ZUZU_ANDROID_KEYSTORE").orNull
val releaseKeystorePassword =
	providers.environmentVariable("ZUZU_ANDROID_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("ZUZU_ANDROID_KEY_ALIAS").orNull
val releaseKeyPassword =
	providers.environmentVariable("ZUZU_ANDROID_KEY_PASSWORD").orNull ?: releaseKeystorePassword
val hasReleaseSigning = !releaseKeystorePath.isNullOrBlank() &&
	!releaseKeystorePassword.isNullOrBlank() &&
	!releaseKeyAlias.isNullOrBlank() &&
	!releaseKeyPassword.isNullOrBlank()

android {
	namespace = "org.zuzulang.repl"
	compileSdk = 35

	defaultConfig {
		applicationId = "org.zuzulang.repl"
		minSdk = 26
		targetSdk = 35
		versionCode = 3
		versionName = "0.3.0"

		testInstrumentationRunner =
			"androidx.test.runner.AndroidJUnitRunner"
	}

	signingConfigs {
		if (hasReleaseSigning) {
			create("release") {
				storeFile = rootProject.file(releaseKeystorePath!!)
				storePassword = releaseKeystorePassword
				keyAlias = releaseKeyAlias
				keyPassword = releaseKeyPassword
			}
		}
	}

	buildTypes {
		release {
			signingConfig = signingConfigs.getByName(
				if (hasReleaseSigning) "release" else "debug"
			)
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile(
					"proguard-android-optimize.txt"
				),
				"proguard-rules.pro"
			)
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
	}
}

dependencies {
	implementation("androidx.core:core-ktx:1.13.1")
	implementation("androidx.appcompat:appcompat:1.7.0")
	implementation("com.google.android.material:material:1.12.0")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
}
