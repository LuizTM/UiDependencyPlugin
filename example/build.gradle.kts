plugins {
    alias(examplelibs.plugins.android.application)
    id("ui.dependency.plugin")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "luiz.dev.example"
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
    constraints = { group, artifact, _ ->
        group == "androidx.datastore" &&
            artifact == "datastore-core"
    }

    with(style) {
        dependencyNodeColor = "#3DDC84"
        linkStrokeColor = "#CCC"
    }
}

dependencies {
    implementation(project(":sub-example"))
    implementation(platform(examplelibs.androidx.compose.bom))
    implementation(examplelibs.androidx.compose.foundation)
    implementation(examplelibs.androidx.compose.foundation.layout)
    implementation(examplelibs.androidx.compose.ui)
    implementation(examplelibs.androidx.constraintlayout)
    implementation(examplelibs.androidx.compose.material)
    implementation(examplelibs.androidx.compose.material3)
    implementation(examplelibs.androidx.compose.material.iconsExtended)
    implementation(examplelibs.androidx.compose.material3.windowSizeClass)
    implementation(examplelibs.androidx.compose.runtime)
    implementation(examplelibs.androidx.compose.runtime.livedata)

    implementation(examplelibs.androidx.core.ktx)
    implementation(examplelibs.androidx.appcompat)
    implementation(examplelibs.androidx.dataStore.core)
    implementation(examplelibs.androidx.activity.compose)
    implementation(examplelibs.coil.kt.compose)

    implementation(examplelibs.room.ktx)
    implementation(examplelibs.room.runtime)
//    kapt(examplelibs.room.compiler)
//    ksp "androidx.room:room.compiler:$room_version"

    implementation(examplelibs.androidx.navigation.compose)
    implementation(examplelibs.androidx.lifecycle.viewModelCompose)
    implementation(examplelibs.androidx.lifecycle.runtimeCompose)

    testImplementation(examplelibs.junit4)


    testImplementation(examplelibs.mockk.android)
    testImplementation(examplelibs.mockk.agent)
    testImplementation(examplelibs.google.truth)
    testImplementation(examplelibs.kotlinx.coroutines.test)

//    debugImplementation(examplelibs.androidx.compose.ui.tooling)
//    debugImplementation(examplelibs.androidx.compose.ui.tooling.preview)
//    debugImplementation(examplelibs.androidx.compose.ui.util)
}
