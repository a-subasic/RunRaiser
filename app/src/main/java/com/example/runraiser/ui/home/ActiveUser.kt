package com.example.runraiser.ui.home

import android.graphics.Bitmap

data class ActiveUser (
    var id: String,
    val lastLat: Double,
    val lastLng: Double,
    val username: String,
    val profilePhotoUrl: String,
    val tokenId: String
)