package com.jonathan.mymaps.data

interface MapRepository {
    suspend fun syncMaps()
}