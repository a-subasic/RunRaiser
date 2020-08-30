package com.example.runraiser.ui.history

data class HistoryCard (
    var id: String,
    var startDate: String,
    var userId: String,
    var distanceKm: String,
    var kilometers: String,
    var avgSpeed: String,
    var moneyRaised: String,
    val mapsScreenshotUrl: String,
    val duration: String
)