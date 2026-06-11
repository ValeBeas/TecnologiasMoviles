package com.undef.superahorro.data.network

import com.undef.superahorro.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Singleton del cliente Supabase con Auth, Postgrest, Storage y Realtime.
 * Es el punto de acceso único a todos los servicios de Supabase en la app.
 */
object ClienteSupabase {
    val cliente = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}
