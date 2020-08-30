package com.example.runraiser.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.runraiser.Firebase
import com.example.runraiser.GlideApp
import com.example.runraiser.R
import com.example.runraiser.UsersMarkersDataCallback
import com.example.runraiser.authenticationActivities.LoginActivity
import com.example.runraiser.ui.home.ActiveUsersData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class SettingsFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null

    private var btnSelectProfilePicture: Button? = null
    private var imgProfilePicture: CircleImageView? = null
    private var etName: EditText? = null
    private var etUsername: EditText? = null
    private var etKm: EditText? = null
    private var etValue: EditText? = null
    private var tvChangePassword: TextView? = null
    private var tvChangeEmail: TextView? = null
    private var tvDeleteAccount: TextView? = null
    private var btnConfirm: Button? = null
    private var btnClose: ImageButton? = null
    private var pbSaveChanges: ProgressBar? = null

    // Logged in user ID
    private var uid: String? = null
    private var initialName: String? = null
    private var initialUsername: String? = null
    private var initialKm: String? = null
    private var initialValue: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialise()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initialise() {
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        uid = mAuth!!.uid

        etName = requireView().findViewById<View>(R.id.et_name) as EditText
        etUsername = requireView().findViewById<View>(R.id.et_username) as EditText
        etKm = requireView().findViewById<View>(R.id.et_km) as EditText
        etValue = requireView().findViewById<View>(R.id.et_value) as EditText
        tvChangePassword = requireView().findViewById<View>(R.id.tv_change_password) as TextView
        tvChangeEmail = requireView().findViewById<View>(R.id.tv_change_email) as TextView
        tvDeleteAccount = requireView().findViewById<View>(R.id.tv_delete_account) as TextView
        pbSaveChanges = requireView().findViewById<View>(R.id.pb_save_changes) as ProgressBar

        // set values of Edit Texts
        setEditTextValues()

        btnSelectProfilePicture = requireView().findViewById<View>(R.id.btn_select_profile_photo) as Button
        imgProfilePicture = requireView().findViewById<View>(R.id.img_profile_photo) as CircleImageView
        btnConfirm = requireView().findViewById<View>(R.id.btn_confirm) as Button
//        btnClose = requireView().findViewById<View>(R.id.btn_close) as ImageButton

        //Load profile photo from Firebase Storage into image circle view
        val refUser = uid?.let { mDatabaseReference!!.child(it) }
        refUser?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (UserData.profilePhotoUrl == null) {
                    val profilePhotoUrl = p0.child("profilePhotoUrl").value

                    GlideApp.with(context!!)
                        .load(profilePhotoUrl)
                        .placeholder(R.drawable.rr_logo_red)
                        .into(imgProfilePicture!!)
                } else {
                    imgProfilePicture?.setImageURI(UserData.profilePhotoUrl)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(tag, p0.message)
            }
        })


        btnSelectProfilePicture!!.setOnClickListener {
            Log.d(tag, "Try to show photo selector")

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        btnConfirm!!.setOnClickListener { saveChanges() }

        //Account Settings
        tvChangePassword!!.setOnClickListener {
            val intent =
                Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        tvChangeEmail!!.setOnClickListener {
            val intent = Intent(requireContext(), ChangeEmailActivity::class.java)
            startActivity(intent)
        }
        tvDeleteAccount!!.setOnClickListener { deleteAccount() }
    }

    private fun setEditTextValues() {
        val userRef = mDatabaseReference!!.child("/$uid")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                initialName = p0.child("/fullName").value.toString()
                initialUsername = p0.child("/username").value.toString()

                if (UserData.name == null) {
                    UserData.name = initialName
                }
                if (UserData.username == null) {
                    UserData.username = initialUsername
                }
                if (UserData.defaultKm == null) {
                    UserData.defaultKm = p0.child("/defaultKm").value.toString()
                }
                if (UserData.defaultValue == null) {
                    UserData.defaultValue = p0.child("/defaultValue").value.toString()
                }

                etName!!.setText(UserData.name)
                etUsername!!.setText(UserData.username)
                etKm!!.setText(UserData.defaultKm)
                etValue!!.setText(UserData.defaultValue)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(tag, p0.message)
            }
        })

        etName!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                UserData.name = etName?.text.toString()
            }
        })

        // Check if username already exists in database
        etUsername!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                UserData.username = etUsername?.text.toString()
                if (initialUsername != null && initialUsername != UserData.username) {
                    val usernameDb =
                        mDatabaseReference!!.orderByChild("username").equalTo(UserData.username)
                    usernameDb.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                etUsername?.error = "Username already exists"
                                etUsername?.requestFocus()
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                            Log.e(tag, "Failed to read value. " + p0.message)
                        }
                    })
                }
            }
        })
    }

    // gets called on startActivityForResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed and check what the selected image was
            Log.d(tag, "Photo was selected")

            // location where selected image is stored on the device
            UserData.profilePhotoUrl = data.data
            imgProfilePicture?.setImageURI(UserData.profilePhotoUrl)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun uploadProfilePhotoToFirebaseStorage() {
        if (UserData.profilePhotoUrl == null) return

        val refStorage =
            FirebaseStorage.getInstance().getReference("/images/profile_pictures/${uid.toString()}")

        refStorage.putFile(UserData.profilePhotoUrl!!)
            .addOnSuccessListener { task ->
                Log.d(tag, "Successfully uploaded image: ${task.metadata?.path}")

                refStorage.downloadUrl.addOnSuccessListener {
                    Log.d(tag, "File location: $it")

                    saveProfilePhotoToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d(tag, it.message.toString())
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveProfilePhotoToFirebaseDatabase(profileImageUrl: String) {
        val ref = mDatabase!!.getReference("/Users/$uid")

        ref.child("profilePhotoUrl").setValue(profileImageUrl)
            .addOnSuccessListener {
                Log.d(tag, "Saved profile photo to firebase database")
                ActiveUsersData.getUsersMarkers(requireContext(), object: UsersMarkersDataCallback {
                    override fun onUsersMarkersDataCallback() {
                        btnConfirm?.isEnabled = true
                        pbSaveChanges?.visibility = View.INVISIBLE
                    }})
//                updateUI(false)
            }
            .addOnFailureListener {
                Log.d(tag, "Failed to save profile photo firebase database")
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveChanges() {
        val ref = mDatabase!!.getReference("/Users/$uid")

        if (etUsername?.error == "Username already exists") {
            Toast.makeText(requireContext(), "Invalid username!", Toast.LENGTH_SHORT).show()
            return
        }
        else if (TextUtils.isEmpty(UserData.name) || TextUtils.isEmpty(UserData.username) || TextUtils.isEmpty(UserData.defaultKm) || TextUtils.isEmpty(UserData.defaultKm)) {
            Toast.makeText(requireContext(), "Fields can't be empty", Toast.LENGTH_SHORT).show()
            return
        }
        else if(UserData.defaultKm!!.toInt() > 80) {
            etKm!!.error = "Maximum is 80km"
            etKm!!.requestFocus()
        }
        else if(UserData.defaultValue!!.toInt() > 100) {
            etValue!!.error = "Maximum is 100kn"
            etValue!!.requestFocus()
        }
        else if (UserData.profilePhotoUrl == null) {
            btnConfirm?.isEnabled = false
            pbSaveChanges?.visibility = View.VISIBLE

            val map = HashMap<String, Any>()
            map["fullName"] = UserData.name!!
            map["username"] = UserData.username!!
            map["defaultKm"] = UserData.defaultKm!!
            map["defaultValue"] = UserData.defaultValue!!

            ref.updateChildren(map)
                .addOnSuccessListener {
                    Log.d(tag, "Updated user profile")
                    btnConfirm?.isEnabled = true
                    pbSaveChanges?.visibility = View.INVISIBLE
                }
                .addOnFailureListener {
                    Log.d(tag, "Failed to update user profile")
                }

            Firebase.firestore
                ?.collection("Users")
                ?.document(Firebase.userId)
                ?.update(map)

        } else {
            btnConfirm?.isEnabled = false
            pbSaveChanges?.visibility = View.VISIBLE

            uploadProfilePhotoToFirebaseStorage()

            val map = HashMap<String, Any>()
            map["fullName"] = UserData.name!!
            map["username"] = UserData.username!!
            map["defaultKm"] = UserData.defaultKm!!
            map["defaultValue"] = UserData.defaultValue!!

            ref.updateChildren(map)
                .addOnSuccessListener {
                    Log.d(tag, "Updated user profile")
                }
                .addOnFailureListener {
                    Log.d(tag, "Failed to update user profile")
                }
        }
    }

    private fun deleteAccount() {
        val user = mAuth?.currentUser
        val refStorage =
            FirebaseStorage.getInstance().getReference("/images/profile_pictures/${uid.toString()}")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Delete") { _, _ ->
                val ref = mDatabase!!.getReference("/Users")
                ref.child("${mAuth?.uid}").removeValue()

                user?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(tag, "User account deleted.")
                    } else {
                        Toast.makeText(requireContext(), task.exception?.message.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                val tokenMap: MutableMap<String, Any> = HashMap()
                tokenMap["token_id"] = FieldValue.delete()

                Firebase.firestore?.collection("Users")?.document(Firebase.userId)?.update(tokenMap)
                Firebase.firestore?.collection("Users")?.document(Firebase.userId)?.delete()
                refStorage.delete()

                mAuth?.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()

    }

    private fun resetUserSettings() {
        UserData.name = null
        UserData.username = null
        UserData.profilePhotoUrl = null
        UserData.logoutFlag = false
        UserData.defaultValue = null
        UserData.defaultKm = null
    }

    private fun updateUI(saveChangesAlert: Boolean) {
        exitSettings()
//        val changesFlag =
//            (initialName != UserData.name) || (initialUsername != UserData.username) || (UserData.profilePhotoUrl != null)
//
//        val logoutAlert = AlertDialog.Builder(requireContext())
//        logoutAlert.setMessage("Login with your new credentials to continue using the app.")
//            .setPositiveButton("Ok") { _, _ ->
//                mAuth?.signOut()
//                resetUserSettings()
//                startActivity(
//                    Intent(
//                        requireContext(),
//                        LoginActivity::class.java
//                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                )
//            }
//            .setNegativeButton("Cancel") { dialog, _ ->
//                dialog.cancel()
//            }
//            .setCancelable(false)
//
//        val unsavedChangesAlert = AlertDialog.Builder(requireContext())
//        unsavedChangesAlert.setTitle("Unsaved changes")
//            .setNegativeButton("No") { dialog, _ ->
//                dialog.cancel()
//            }
//            .setCancelable(false)
//
//
//        if (saveChangesAlert) {
//            btnConfirm?.visibility = View.VISIBLE
//            pbSaveChanges?.visibility = View.INVISIBLE
//            if (UserData.logoutFlag!! && changesFlag) {
//                unsavedChangesAlert.setMessage("You have unsaved changes and you need to login with your new credentials to continue using the app. Are you sure you want to cancel?")
//                    .setPositiveButton("Yes") { _, _ ->
//                        mAuth?.signOut()
//                        resetUserSettings()
//                        startActivity(
//                            Intent(
//                                requireContext(),
//                                LoginActivity::class.java
//                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        )
//                    }
//                    .show()
//            } else if (UserData.logoutFlag!! && !changesFlag) {
//                logoutAlert.show()
//            } else if (changesFlag) {
//                unsavedChangesAlert.setMessage("You have unsaved changes. Are you sure you want to cancel?")
//                    .setPositiveButton("Yes") { _, _ ->
//                        exitSettings()
//                    }
//                    .show()
//            } else {
//                exitSettings()
//            }
//        } else if (UserData.logoutFlag!!) {
//            btnConfirm?.visibility = View.VISIBLE
//            pbSaveChanges?.visibility = View.INVISIBLE
//            logoutAlert.show()
//        } else {
//            exitSettings()
//        }
    }

    private fun exitSettings() {
        resetUserSettings()
    }

//    override fun onPause() {
//        super.onPause()
//        updateUI(true)
//        println("pause")
//    }
}