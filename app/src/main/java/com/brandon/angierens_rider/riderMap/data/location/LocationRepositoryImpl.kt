package com.brandon.angierens_rider.riderMap.data.location

import android.Manifest
import androidx.annotation.RequiresPermission
import com.brandon.angierens_rider.riderMap.domain.location.LocationRepository

class LocationRepositoryImpl(
    private val locationService: LocationServiceImpl
): LocationRepository {
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startLocationUpdates(onLocation: (Double, Double) -> Unit) {
        locationService.start(onLocation)
    }
}