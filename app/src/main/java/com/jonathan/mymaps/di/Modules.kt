package com.jonathan.mymaps.di

import com.jonathan.mymaps.workers.MapsSyncWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val appModules = module {
    worker { MapsSyncWorker(get(), get(), get()) }
}