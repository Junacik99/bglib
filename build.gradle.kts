// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.kotlin.android) version "1.9.22" apply false
    //noinspection GradleDependency
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    alias(libs.plugins.kotlin.compose) apply false
}