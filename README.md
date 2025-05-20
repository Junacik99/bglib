# Smart Assistants for Board Games
*BGLIB* is an Android library for the development of smart assistants
for board games (board games library). It utilises various computer
vision techniques and machine learning algorithms. Packages like OpenCV,
ML Kit and Mediapipe are already integrated.

## Installation
To integrate a library into a project a user needs to add the jitpack
repository into `settings.gradle.kts` as follows:

    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            mavenCentral()
            maven { url = uri("https://jitpack.io") }
        }
    }

Afterwards it is possible to integrate the library inside
`build.gradle.kts`:

    dependencies {
        implementation(libs.junacik99.bglib)
    }

Or refer to <https://jitpack.io/#Junacik99/bglib> for detailed
instructions on how to integrate the *bglib* library into an Android
project.

## Documentation
The docs are in the [Documentation](./documentation.md) file.

## Source Code
The source code of the library is in the [bglib](./bglib/) directory.

Used models are in the [assets](./app/src/main/assets/) directory.

## Train Your Own Model
To train a custom model for the image classification, you can use a [model training pipeline](https://github.com/Junacik99/airflow-model-pipeline) in Airflow.

To install Airflow, follow [these](https://airflow.apache.org/docs/apache-airflow/stable/installation/index.html) instructions.
