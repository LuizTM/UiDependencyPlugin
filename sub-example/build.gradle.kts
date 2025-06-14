plugins {
    alias(examplelibs.plugins.android.library)
    id("ui.dependency.plugin")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

uiTreeExtension {
//    constraints = { group, artifact, _ ->
//        group == "androidx.datastore" &&
//            artifact == "datastore-core"
//    }

    with(style) {
        projectNodeColor = "#FF0000"
        dependencyNodeColor = "#3DDC84"
        linkStrokeColor = "#CCC"
    }
}

dependencies {
    implementation(examplelibs.androidx.core.ktx)
    implementation(examplelibs.androidx.appcompat)
    implementation(examplelibs.androidx.dataStore.core)
    implementation(examplelibs.androidx.activity.compose)
    implementation(examplelibs.coil.kt.compose)

    testImplementation(examplelibs.mockk.android)
    testImplementation(examplelibs.mockk.agent)
    testImplementation(examplelibs.google.truth)
    testImplementation(examplelibs.kotlinx.coroutines.test)

//    debugImplementation(examplelibs.androidx.compose.ui.tooling)
//    debugImplementation(examplelibs.androidx.compose.ui.tooling.preview)
//    debugImplementation(examplelibs.androidx.compose.ui.util)
}
