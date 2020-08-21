package com.example.runraiser.ui.home

import com.google.android.gms.maps.model.LatLng

data class ActiveUser (
    var id: String,
    val lastLat: Double,
    val lastLng: Double,
    val username: String,
    val profilePhotoUrl: String,
    val tokenId: String
)