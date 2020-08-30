package com.example.runraiser.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.runraiser.R
import com.example.runraiser.authenticationActivities.LoginActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_change_password.*


class ChangePasswordActivity : AppCompatActivity() {
    private val tag = "ChangePassword"

    //  Firebase references
    private var mAuth: FirebaseAuth? = null

    //  UI elements
    private var btnBack: ImageButton? = null
    private var etCurrentPassword: EditText? = null
    private var etNewPassword: EditText? = null
    private var etConfirmNewPassword: EditText? = null
    private var btnChangePassword: Button? = null
    private var pbChangePassword: ProgressBar? = null

    private var currentPassword: String? = null
    private var newPassword: String? = null
    private var confirmNewPassword: String? = null
    private var showPasswordFlag1: Boolean = false
    private var showPasswordFlag2: Boolean = false
    private var showPasswordFlag3: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        initialise()
    }

    private fun initialise() {
        mAuth = FirebaseAuth.getInstance()

        btnBack = findViewById<View>(R.id.btn_back) as ImageButton
        etCurrentPassword = findViewById<View>(R.id.et_current_password) as EditText
        etNewPassword = findViewById<View>(R.id.et_new_password) as EditText
        etConfirmNewPassword = findViewById<View>(R.id.et_confirm_new_password) as EditText
        btnChangePassword = findViewById<View>(R.id.btn_change_password) as Button
        pbChangePassword = findViewById<View>(R.id.pb_change_password) as ProgressBar

        btnChangePassword!!.setOnClickListener{ changePassword() }
        btnBack!!.setOnClickListener{ updateUI() }

        etCurrentPassword!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(etCurrentPassword!!.text.isNotEmpty()) {
                    btn_show_password1.visibility = View.VISIBLE
                }
                else {
                    btn_show_password1.visibility = View.GONE
                }
            }
        })

        btn_show_password1.setOnClickListener {
            showPasswordFlag1 = !showPasswordFlag1
            if(showPasswordFlag1) {
                etCurrentPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btn_show_password1.setImageResource(R.drawable.ic_hidden_eye)
            } else {
                etCurrentPassword!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btn_show_password1.setImageResource(R.drawable.ic_eye_black)
            }
            etCurrentPassword!!.typeface = tv_change_password.typeface
            etCurrentPassword!!.setSelection(etCurrentPassword!!.text.length)
        }

        etNewPassword!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                etNewPassword!!.error = null
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(etNewPassword!!.text.isNotEmpty()) {
                    btn_show_password2.visibility = View.VISIBLE
                }
                else {
                    btn_show_password2.visibility = View.GONE
                }
            }
        })

        btn_show_password2.setOnClickListener {
            showPasswordFlag2 = !showPasswordFlag2
            if(showPasswordFlag2) {
                etNewPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btn_show_password2.setImageResource(R.drawable.ic_hidden_eye)
            } else {
                etNewPassword!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btn_show_password2.setImageResource(R.drawable.ic_eye_black)
            }
            etNewPassword!!.typeface = tv_change_password.typeface
            etNewPassword!!.setSelection(etNewPassword!!.text.length)
        }

        etCurrentPassword!!.setOnClickListener {
            etCurrentPassword!!.error = null
            btn_show_password1.visibility = View.VISIBLE
        }

        etNewPassword!!.setOnClickListener {
            etNewPassword!!.error = null
            btn_show_password2.visibility = View.VISIBLE
        }

        etConfirmNewPassword!!.setOnClickListener {
            etConfirmNewPassword!!.error = null
            btn_show_password3.visibility = View.VISIBLE
        }

        etConfirmNewPassword!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(etConfirmNewPassword!!.text.isNotEmpty()) {
                    btn_show_password3.visibility = View.VISIBLE
                }
                else {
                    btn_show_password3.visibility = View.GONE
                }
            }
        })

        btn_show_password3.setOnClickListener {
            showPasswordFlag3 = !showPasswordFlag3
            if(showPasswordFlag3) {
                etConfirmNewPassword!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btn_show_password3.setImageResource(R.drawable.ic_hidden_eye)
            } else {
                etConfirmNewPassword!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btn_show_password3.setImageResource(R.drawable.ic_eye_black)
            }
            etConfirmNewPassword!!.typeface = tv_change_password.typeface
            etConfirmNewPassword!!.setSelection(etConfirmNewPassword!!.text.length)
        }
    }

    private fun changePassword() {
        pbChangePassword?.visibility = View.VISIBLE

        currentPassword = etCurrentPassword?.text.toString()
        newPassword = etNewPassword?.text.toString()
        confirmNewPassword = etConfirmNewPassword?.text.toString()

        // hide keyboard
        val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)


        if(TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
            Toast.makeText(this, "Please enter all details!", Toast.LENGTH_SHORT).show()
            pbChangePassword?.visibility = View.INVISIBLE
        }
        else if(currentPassword.equals(newPassword)) {
            etNewPassword?.error = "New password can't match current password"
            etNewPassword?.requestFocus()
            pbChangePassword?.visibility = View.INVISIBLE
            btn_show_password2.visibility = View.GONE
        }
        else if(!newPassword.equals(confirmNewPassword)) {
            etConfirmNewPassword?.error = "Passwords don't match"
            etConfirmNewPassword?.requestFocus()
            pbChangePassword?.visibility = View.INVISIBLE
            btn_show_password3.visibility = View.GONE
        }
        else {
            val user = mAuth?.currentUser
            if(user != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword!!)
                user.reauthenticate(credential).addOnCompleteListener{
                    if(it.isSuccessful) {
                        user.updatePassword(newPassword!!).addOnCompleteListener{ task ->
                            if(task.isSuccessful) {
                                Log.d(tag, "User password updated.")
                                Toast.makeText(this, "Password changed", Toast.LENGTH_SHORT).show()
                                UserData.logoutFlag = true
                                updateUI()
                            }
                            else {
                                Log.d(tag, task.exception?.message.toString())
                                Toast.makeText(this, (task.exception?.message
                                    ?: "Password not changed."), Toast.LENGTH_SHORT).show()
                                pbChangePassword?.visibility = View.INVISIBLE
                            }
                        }
                    }
                    else {
                        etCurrentPassword?.error = "Wrong password."
                        etCurrentPassword?.requestFocus()
                        pbChangePassword?.visibility = View.INVISIBLE
                        btn_show_password1.visibility = View.GONE
                    }
                }
            }
            else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun updateUI() {
//        val intent = Intent(this, SettingsFragment)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        startActivity(intent)
        finish()
    }
}
