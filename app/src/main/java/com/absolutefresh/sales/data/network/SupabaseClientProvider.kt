package com.absolutefresh.sales.data.network

import com.absolutefresh.sales.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Provides singleton or injected SupabaseClient instance initialized
 * using project BuildConfig environment parameters.
 */
object SupabaseClientProvider {
    @Volatile
    private var instance: SupabaseClient? = null

    fun getClient(
        url: String = BuildConfig.SUPABASE_URL,
        key: String = BuildConfig.SUPABASE_ANON_KEY
    ): SupabaseClient {
        return instance ?: synchronized(this) {
            instance ?: createSupabaseClient(
                supabaseUrl = url,
                supabaseKey = key
            ) {
                install(Postgrest)
                install(Auth)
                install(Realtime)
            }.also { instance = it }
        }
    }

    /**
     * For unit and integration testing: override client with mock or isolated test instance.
     */
    fun setClientForTesting(client: SupabaseClient?) {
        instance = client
    }
}
