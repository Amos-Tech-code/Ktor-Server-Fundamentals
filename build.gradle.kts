
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    //Serialization
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    //Resources
    implementation(libs.ktor.server.resources)
    //Status Pages
    implementation(libs.ktor.server.status.pages)
    //Request Validation
    implementation(libs.ktor.server.request.validation)
    //Rate Limiting
    implementation(libs.ktor.server.rate.limit)
    //Auto Header Response
    implementation(libs.ktor.server.auto.head.response)
    //Partial Content
    implementation(libs.ktor.server.partial.content)
    //Sessions
    implementation(libs.ktor.server.sessions)
    //Basic Auth & JWT
    implementation(libs.ktor.server.auth)
    //JWT
    implementation(libs.ktor.server.auth.jwt)
    //Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    //Server Side Events
    implementation(libs.ktor.server.sse)
    //WebSockets
    implementation(libs.ktor.server.websockets)
    //Call Logging
    implementation(libs.ktor.server.call.logging)

}
