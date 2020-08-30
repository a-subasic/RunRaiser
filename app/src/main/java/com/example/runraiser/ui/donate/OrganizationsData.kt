package com.example.runraiser.ui.donate

import android.util.Log
import com.example.runraiser.Firebase
import com.example.runraiser.OrganizationsDataCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class OrganizationsData {
    companion object {
        var myOrganizationsData = ArrayList<OrganizationCard>()

        fun fetchOrganizationsData(organizationsDataCallback: OrganizationsDataCallback) {
            myOrganizationsData = ArrayList()
            Firebase.databaseOrganizations?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.i("fetchOrganizationsData", "Failed to read value. " + error.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val children = p0.children
                    children.forEach{
                        myOrganizationsData.add (
                            OrganizationCard(
                                it.child("id").value.toString(),
                                it.child("address").value.toString(),
                                it.child("description").value.toString(),
                                it.child("email").value.toString(),
                                it.child("imageUrl").value.toString(),
                                it.child("name").value.toString(),
                                it.child("phone").value.toString(),
                                it.child("web").value.toString()
                            )
                        )
                    }
                    myOrganizationsData.sortByDescending { it.name }
                    organizationsDataCallback.onOrganizationsDataCallback(myOrganizationsData)
                }
            })
        }
    }
}
