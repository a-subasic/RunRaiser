package com.example.runraiser.ui.home

import android.util.Log
import com.example.runraiser.ActiveUsersDataCallback
import com.example.runraiser.Firebase
import com.example.runraiser.TrainingsDataCallback
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ActiveUsersData {
    companion object {
        var activeUsersData = ArrayList<ActiveUser>()

        fun getActiveUsersData(activeUsersDataCallback: ActiveUsersDataCallback) {
            activeUsersData = ArrayList()
            Firebase.databaseUsers?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.i("fetchUsersData", "Failed to read value. " + error.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val children = p0.children
                    children.forEach{
                        val isInTraining = it.child("isInTraining").value
                        val userId = it.child("id").value.toString()
                        if(isInTraining as Boolean && userId != Firebase.auth?.uid) {
                            activeUsersData.add (
                                ActiveUser (
                                    it.child("id").value.toString(),
                                    it.child("lastLat").value as Double,
                                    it.child("lastLng").value as Double,
                                    it.child("username").value.toString(),
                                    it.child("profilePhotoUrl").value.toString(),
                                    it.child("tokenId").value.toString()
                                )
                            )
                        }
                    }
                    activeUsersDataCallback.onActiveUsersDataCallback(activeUsersData)
                }
            })
        }
    }
}