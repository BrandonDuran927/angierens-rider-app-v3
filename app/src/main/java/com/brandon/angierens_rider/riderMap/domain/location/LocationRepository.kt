package com.brandon.angierens_rider.riderMap.domain.location

interface LocationRepository {
    fun startLocationUpdates(onLocation: (Double, Double) -> Unit)
}