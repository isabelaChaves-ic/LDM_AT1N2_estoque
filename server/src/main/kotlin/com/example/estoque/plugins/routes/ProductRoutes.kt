package com.example.estoque.plugins.routes

import com.example.estoque.models.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(supabase: SupabaseClient) {
    route("/products") {
        get {
            val products = supabase.from("products").select().decodeList<Product>()
            call.respond(HttpStatusCode.OK, products)
        }
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val product = supabase.from("products").select { filter { eq("id", id) } }.decodeSingleOrNull<Product>()
            if (product != null) call.respond(HttpStatusCode.OK, product) else call.respond(HttpStatusCode.NotFound)
        }
        post {
            val product = call.receive<Product>()
            supabase.from("products").insert(product)
            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val productUpdates = call.receive<Product>()
            supabase.from("products").update(productUpdates) { filter { eq("id", id) } }
            call.respond(HttpStatusCode.OK)
        }
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            supabase.from("products").delete { filter { eq("id", id) } }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}