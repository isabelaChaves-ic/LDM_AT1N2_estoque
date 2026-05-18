package com.example.estoque

import com.example.estoque.plugins.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.Serializable

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: Long
)

fun createAppSupabaseClient(url: String, key: String): SupabaseClient {
    return createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
        install(Postgrest)
    }
}

fun Application.module() {
    var supabaseUrl = System.getProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL")
    var supabaseKey = System.getProperty("SUPABASE_KEY") ?: System.getenv("SUPABASE_KEY")

    if (supabaseUrl == null || supabaseKey == null) {
        val properties = java.util.Properties()
        val localPropFiles = listOf(
            java.io.File("local.properties"),
            java.io.File("../local.properties"),
            java.io.File("../../local.properties")
        )
        val file = localPropFiles.firstOrNull { it.exists() }
        if (file != null) {
            properties.load(java.io.FileInputStream(file))
            supabaseUrl = properties.getProperty("SUPABASE_URL")
            supabaseKey = properties.getProperty("SUPABASE_KEY")
        }
    }

    var supabase: SupabaseClient? = null
    if (supabaseUrl != null && supabaseKey != null) {
        supabase = createAppSupabaseClient(supabaseUrl, supabaseKey)
    }

    configureSerialization()
    configureRouting(supabase)
}