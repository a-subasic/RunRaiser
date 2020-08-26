package com.example.runraiser.notificationActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.runraiser.R
import kotlinx.android.synthetic.main.activity_notification.*

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val dataMessage = intent.getStringExtra("message")
        val dataFrom = intent.getStringExtra("from_id")

        tv_notification.text = "$dataMessage from $dataFrom"
    }
}