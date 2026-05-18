package com.example.estoque.plugins.routes

import com.example.estoque.models.StockItem
import com.example.estoque.models.StockSummary
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.stockRoutes(supabase: SupabaseClient) {
    route("/stock") {
        get("/summary") {
            val summary = supabase.from("stock_summary").select().decodeList<StockSummary>()
            call.respond(HttpStatusCode.OK, summary)
        }
        get {
            val stockItems = supabase.from("stock_items").select().decodeList<StockItem>()
            call.respond(HttpStatusCode.OK, stockItems)
        }
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val item = supabase.from("stock_items").select { filter { eq("id", id) } }.decodeSingleOrNull<StockItem>()
            if (item != null) call.respond(HttpStatusCode.OK, item) else call.respond(HttpStatusCode.NotFound)
        }
        post {
            val item = call.receive<StockItem>()
            supabase.from("stock_items").insert(item)
            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val itemUpdates = call.receive<StockItem>()
            supabase.from("stock_items").update(itemUpdates) { filter { eq("id", id) } }
            call.respond(HttpStatusCode.OK)
        }
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            supabase.from("stock_items").delete { filter { eq("id", id) } }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}