plugins {
    id("common")
    application
}

dependencies {
    implementation(libs.logback)
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.databind)
    runtimeOnly(libs.postgres.jdbc)
}

application {
    mainClass.set("dataloader.Main")
}
