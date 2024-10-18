// build.gradle.kts (Project-level)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Safe Args plugin classpath
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
