package com.example.runraiser.notificationActivities


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.runraiser.R
import com.google.firebase.messaging.RemoteMessage


class FirebaseMessagingService: com.google.firebase.messaging.FirebaseMessagingService() {
    private val CHANNEL_ID = "following_notification"
    private val CHANNEL_DESCRIPTION = "Receive notification on follow"
    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_DESCRIPTION,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager!!.createNotificationChannel(channel)
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        val deviceToken = s
        // Do whatever you want with your token now
        // i.e. store it on SharedPreferences or DB
        // or directly send it to server
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val messageTitle = remoteMessage.notification?.title
        val messageBody = Html.fromHtml(remoteMessage.data["message"])
        val clickAction = remoteMessage.notification?.clickAction
        val dataMessage = remoteMessage.data["message"]
        val dataFrom = remoteMessage.data["from_id"]

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setChannelId(CHANNEL_ID)

        val resultIntent = Intent(clickAction)
        resultIntent.putExtra("message", dataMessage)
        resultIntent.putExtra("from_id", dataFrom)
        val resultPendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)


        val notificationId: Long = System.currentTimeMillis()
        notificationManager?.notify(notificationId.toInt(), builder.build())
    }
}