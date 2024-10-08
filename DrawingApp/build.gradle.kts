// build.gradle.kts (Project-level)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Safe Args plugin classpath
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
