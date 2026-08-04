#!/bin/bash
sed -i 's/agp = "9.1.1"/agp = "9.3.0"/' gradle/libs.versions.toml
sed -i 's/coreKtx = "1.18.0"/coreKtx = "1.19.0"/' gradle/libs.versions.toml
sed -i 's/kotlin = "2.2.10"/kotlin = "2.4.10"/' gradle/libs.versions.toml
sed -i 's/lifecycleRuntimeKtx = "2.8.7"/lifecycleRuntimeKtx = "2.11.0"/' gradle/libs.versions.toml
sed -i 's/lifecycleViewmodelCompose = "2.8.7"/lifecycleViewmodelCompose = "2.11.0"/' gradle/libs.versions.toml
sed -i 's/lifecycleRuntimeCompose = "2.8.7"/lifecycleRuntimeCompose = "2.11.0"/' gradle/libs.versions.toml
sed -i 's/activityCompose = "1.10.1"/activityCompose = "1.13.0"/' gradle/libs.versions.toml
sed -i 's/composeBom = "2024.09.00"/composeBom = "2026.06.01"/' gradle/libs.versions.toml
