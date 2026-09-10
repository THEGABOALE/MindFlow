plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.mindflow.nova"
    compileSdk {
        version = release(37) {
        }
    }

    defaultConfig {
        applicationId = "com.mindflow.nova"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Client ID de tipo "Aplicacion web" en Google Cloud (Google Auth Platform > Clientes).
        // No es secreto: es el mismo que el backend usa como GOOGLE_CLIENT_ID para
        // verificar el idToken. Hay ademas un cliente tipo "Android" (paquete +
        // huella SHA-1 del certificado de firma) que no se referencia en código:
        // solo tiene que existir para que Google confie en el APK que pide el token.
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"205907758061-vb4ehb8gu7fcps0mc85l4tsiq2qm4c5j.apps.googleusercontent.com\""
        )
    }

    buildTypes {
        debug {
            // Alias especial que, dentro del emulador, apunta al localhost de la compu.
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/\"")
        }
        release {
            // Reemplazar por la URL real una vez desplegado el backend en Railway.
            buildConfigField("String", "BASE_URL", "\"https://mindflow-production-e4b0.up.railway.app/\"")
            // Firma con la clave de debug: alcanza para instalar en un teléfono
            // de prueba o hacer una demo. NO usar para publicar en Play Store.
            signingConfig = signingConfigs.getByName("debug")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    // Guarda el token de sesión cifrado en el dispositivo.
    implementation("androidx.security:security-crypto:1.1.0")
    // Login con Google: Credential Manager es la API recomendada actual
    // (reemplaza al viejo GoogleSignInClient), y necesita el puente a
    // Play Services más la librería que arma el GoogleIdTokenCredential.
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}