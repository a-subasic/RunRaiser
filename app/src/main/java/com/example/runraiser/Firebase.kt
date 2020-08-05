package com.example.runraiser

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class Firebase {
    companion object {
        var firestore: FirebaseFirestore? = FirebaseFirestore.getInstance()
        var auth: FirebaseAuth? = FirebaseAuth.getInstance()
        var database: FirebaseDatabase? = FirebaseDatabase.getInstance()
        var databaseUsers: DatabaseReference? = database!!.reference.child("Users")
        var userId: String = auth!!.currentUser!!.uid
        val currentUserDb = databaseUsers!!.child(userId)
    }
}
