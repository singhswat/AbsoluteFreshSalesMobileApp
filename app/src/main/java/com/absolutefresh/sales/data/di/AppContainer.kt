package com.absolutefresh.sales.data.di

import android.content.Context
import com.absolutefresh.sales.data.local.AppDatabase
import com.absolutefresh.sales.data.network.NetworkDataSource
import com.absolutefresh.sales.data.network.SupabaseNetworkDataSource
import com.absolutefresh.sales.data.repository.BeatRepository
import com.absolutefresh.sales.data.repository.OrderRepository
import com.absolutefresh.sales.data.repository.OutletRepository
import com.absolutefresh.sales.data.repository.RoomBeatRepository
import com.absolutefresh.sales.data.repository.RoomOrderRepository
import com.absolutefresh.sales.data.repository.RoomOutletRepository
import com.absolutefresh.sales.data.sync.OutboxSyncWorker
import com.absolutefresh.sales.data.sync.SyncRepository

/**
 * Lightweight Dependency Container providing singleton Room database,
 * network data source, repositories, and synchronization manager.
 */
class AppContainer(private val context: Context) {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val networkDataSource: NetworkDataSource by lazy {
        SupabaseNetworkDataSource()
    }

    val syncRepository: SyncRepository by lazy {
        SyncRepository(database, networkDataSource)
    }

    val authRepository: com.absolutefresh.sales.data.repository.AuthRepository by lazy {
        com.absolutefresh.sales.data.repository.SupabaseAuthRepository()
    }

    val outletRepository: OutletRepository by lazy {
        RoomOutletRepository(database.outletDao(), database.outboxDao())
    }

    val orderRepository: OrderRepository by lazy {
        RoomOrderRepository(database.orderDao(), database.outboxDao())
    }

    val beatRepository: BeatRepository by lazy {
        RoomBeatRepository(database.beatDao(), database.outletDao())
    }

    /**
     * Initializes background sync listeners and constraints.
     */
    fun initializeSync() {
        OutboxSyncWorker.enqueue(context)
    }
}
