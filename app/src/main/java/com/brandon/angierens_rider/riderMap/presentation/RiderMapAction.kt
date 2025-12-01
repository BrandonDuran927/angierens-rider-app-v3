package com.brandon.angierens_rider.riderMap.presentation

sealed class RiderMapAction {
    data object UpdateOrderStatus : RiderMapAction()
}