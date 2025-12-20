import com.android.build.gradle.BaseExtension
import org.gradle.api.tasks.Delete
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.io.FileInputStream

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Custom build directories
val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}


subprojects {
    afterEvaluate {
        // Apply to all Android modules/plugins
        project.extensions.findByType<BaseExtension>()?.apply {
            // Ensure namespace exists
            if (namespace == null) namespace = project.group.toString()

            // Force compileSdk
            compileSdkVersion(36)

            // Force defaultConfig SDKs safely
            defaultConfig {
                targetSdk = targetSdk?.let { maxOf(it, 36) } ?: 36
                minSdk = minSdk?.let { maxOf(it, 24) } ?: 24
            }

            // Force Java 17
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            // ✅ Force NDK version for this module
            ndkVersion = "29.0.14206865"
        }

        // Force Kotlin 17
        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }

    // Ensure app module is evaluated first
    project.evaluationDependsOn(":app")
}


// subprojects {
//     afterEvaluate {
//         // Apply to all Android modules/plugins
//         project.extensions.findByType<BaseExtension>()?.apply {
//             // Ensure namespace exists
//             if (namespace == null) namespace = project.group.toString()

//             // ✅ Force compileSdk
//             compileSdkVersion(36)

//             // ✅ Force defaultConfig SDKs safely (nullable-safe)
//             defaultConfig {
//                 targetSdk = targetSdk?.let { maxOf(it, 36) } ?: 36
//                 minSdk = minSdk?.let { maxOf(it, 24) } ?: 24
//             }

//             // ✅ Force Java 17
//             compileOptions {
//                 sourceCompatibility = JavaVersion.VERSION_17
//                 targetCompatibility = JavaVersion.VERSION_17
//             }
//         }

//         // ✅ Force Kotlin 17
//         tasks.withType<KotlinCompile>().configureEach {
//             compilerOptions {
//                 jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
//             }
//         }
//     }

//     // Ensure app module is evaluated first
//     project.evaluationDependsOn(":app")
// }

// Clean task
tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}