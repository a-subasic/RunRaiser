package com.example.runraiser.ui.home

interface IOnLoadLocationListener {
    fun onLocationLoadSuccess(latLngs: List<MyLatLng>)
    fun onLocationLoadFailed(message: String)
}