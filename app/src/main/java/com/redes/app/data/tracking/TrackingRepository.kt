package com.redes.app.data.tracking

import com.redes.app.network.RedesApiClient

class TrackingRepository(private val apiClient: RedesApiClient) {
    fun postLocation(lat: Double, lng: Double, accuracy: Float?, speed: Float?) {
        apiClient.postTracking(lat, lng, accuracy, speed)
    }
}
