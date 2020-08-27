package com.example.runraiser.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.bumptech.glide.request.target.CustomTarget
import com.example.runraiser.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ActiveUsersData {
    companion object {
        var activeUsersData = HashMap<String, ActiveUser>()
        var usersBitmapMarker = HashMap<String, Bitmap>()

        fun getActiveUsersData(activeUsersDataCallback: ActiveUsersDataCallback) {
            activeUsersData = HashMap()
            Firebase.databaseUsers?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.i("fetchUsersData", "Failed to read value. " + error.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val children = p0.children
                    children.forEach{
                        val inTraining = it.child("inTraining").value
                        val userId = it.child("id").value.toString()
                        if(inTraining as Boolean && userId != Firebase.auth?.uid) {
                            activeUsersData[userId] =
                                ActiveUser (
                                    it.child("id").value.toString(),
                                    it.child("lastLat").value as Double,
                                    it.child("lastLng").value as Double,
                                    it.child("username").value.toString(),
                                    it.child("profilePhotoUrl").value.toString(),
                                    it.child("tokenId").value.toString()
                                )
                        }
                    }
                    activeUsersDataCallback.onActiveUsersDataCallback(activeUsersData)
                }
            })
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getUsersMarkers(context: Context) {
            usersBitmapMarker = HashMap()
            Firebase.databaseUsers?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.i("fetchUsersData", "Failed to read value. " + error.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val children = p0.children
                    children.forEach{
                        val profilePhotoUrl = it.child("profilePhotoUrl").value
                        val id = it.child("id").value.toString()

                        var tmpBitmap : Bitmap? = null
                        GlideApp.with(context)
                            .asBitmap()
                            .load(profilePhotoUrl)
                            .into(object : CustomTarget<Bitmap>(){
                                override fun onLoadCleared(placeholder: Drawable?) {
                                }

                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                                ) {
                                    tmpBitmap = BitmapMarker.getCroppedBitmap(Bitmap.createScaledBitmap(resource, 100,100, true))
                                    if(id == FirebaseAuth.getInstance().uid)
                                        tmpBitmap = BitmapMarker.addBorderToCircularBitmap(tmpBitmap!!, 5.0f, Color.RED)
                                    else
                                        tmpBitmap = BitmapMarker.addBorderToCircularBitmap(tmpBitmap!!, 5.0f, Color.BLUE)

                                    usersBitmapMarker[id] = tmpBitmap!!
                                }
                            })
                    }
                }
            })
        }
    }
}