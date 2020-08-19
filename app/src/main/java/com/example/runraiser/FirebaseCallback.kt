package com.example.runraiser

import com.example.runraiser.ui.history.HistoryCard

interface TrainingsDataCallback {
    fun onTrainingsDataCallback(myTrainingsData: ArrayList<HistoryCard>)
}
