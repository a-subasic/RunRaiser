package com.example.runraiser.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.runraiser.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class ChangeEmailActivity : AppCompatActivity() {
    private val tag = "ChangeEmail"

    //  Firebase references
    private var mAuth: FirebaseAuth? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null

    //  UI elements
    private var btnBack: ImageButton? = null
    private var etNewEmail: EditText? = null
    private var btnChangeEmail: Button? = null
    private var pbChangeEmail: ProgressBar? = null

    private var newEmail: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        initialise()
    }

    private fun initialise() {
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")

        btnBack = findViewById<View>(R.id.btn_back) as ImageButton
        etNewEmail = findViewById<View>(R.id.et_new_email) as EditText
        btnChangeEmail = findViewById<View>(R.id.btn_change_email) as Button
        pbChangeEmail = findViewById<View>(R.id.pb_change_email) as ProgressBar

        etNewEmail!!.setText(mAuth!!.currentUser?.email.toString())

        btnChangeEmail!!.setOnClickListener{ changeEmail() }
        btnBack!!.setOnClickListener{ updateUI() }
    }

    private fun changeEmail() {
        pbChangeEmail?.visibility = View.VISIBLE
        newEmail = etNewEmail?.text.toString()
        val user = mAuth?.currentUser

        // hide keyboard
        val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)

        if(TextUtils.isEmpty(newEmail)) {
            etNewEmail?.error = "Enter an email"
            etNewEmail?.requestFocus()
            pbChangeEmail?.visibility = View.INVISIBLE
        }
        else if (user != null) {
            if(user.email == newEmail) {
                etNewEmail?.error = "New email can't match current email"
                etNewEmail?.requestFocus()
                pbChangeEmail?.visibility = View.INVISIBLE
            }
            else {
                user.updateEmail(newEmail!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(tag, "User email address updated.")
                            user.sendEmailVerification().addOnCompleteListener(this) {
                                if(it.isSuccessful) {
                                    Log.d(tag, "Verification email sent.")

                                    val ref = mDatabase!!.getReference("/Users/${mAuth?.uid}")
                                    ref.child("email").setValue(newEmail)
                                    Toast.makeText(this, ("Email changed"), Toast.LENGTH_SHORT).show()
                                    UserData.logoutFlag = true
                                    updateUI()
                                }
                                else {
                                    Log.d(tag, task.exception?.message.toString())
                                    Toast.makeText(this, (it.exception?.message ?: "Verification email failed to send."), Toast.LENGTH_SHORT).show()
                                    pbChangeEmail?.visibility = View.INVISIBLE
                                }
                            }
                        }
                        else {
                            Log.d(tag, task.exception?.message.toString())
                            Toast.makeText(this, (task.exception?.message ?: "Failed to update email."), Toast.LENGTH_SHORT).show()
                            pbChangeEmail?.visibility = View.INVISIBLE
                        }
                    }
            }
        }
    }

    private fun updateUI() {
        finish()
//        val intent = Intent(this, SettingsFragment::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        startActivity(intent)
    }

}
