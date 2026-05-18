package com.example.estoque.plugins

import com.example.estoque.plugins.routes.productRoutes
import com.example.estoque.plugins.routes.stockRoutes
import io.github.jan.supabase.SupabaseClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(supabase: SupabaseClient?) {
    routing {
        if (supabase != null) {
            productRoutes(supabase)
            stockRoutes(supabase)
        } else {
            get("/") {
                call.respondText("Servidor rodando, mas Supabase não configurado corretamente.")
            }
        }
    }
}