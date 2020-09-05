package com.example.runraiser

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.request.RequestOptions
import com.example.runraiser.authenticationActivities.LoginActivity
import com.example.runraiser.ui.home.ActiveUsersData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        var initialProfileImg = ActiveUsersData.currentUser?.profilePhotoUrl
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_donate, R.id.nav_history, R.id.nav_settings), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)

                if(initialProfileImg != ActiveUsersData.currentUser?.profilePhotoUrl) {
                    initialProfileImg = ActiveUsersData.currentUser?.profilePhotoUrl
                    GlideApp.with(applicationContext)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(ActiveUsersData.currentUser?.profilePhotoUrl)
                        .into(nav_profile_img)
                }
                nav_name.text = ActiveUsersData.currentUser?.fullName
                nav_username.text = ActiveUsersData.currentUser?.username
                nav_email.text = ActiveUsersData.currentUser?.email
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerOpened(drawerView: View) {
            }
        })


        logout_ll.setOnClickListener {
            val tokenMap: MutableMap<String, Any> = HashMap()
            tokenMap["token_id"] = ""

            Firebase.firestore?.collection("Users")?.document(Firebase.userId)?.update(tokenMap)
            Firebase.databaseUsers?.child(Firebase.userId)?.child("tokenId")?.removeValue()
            Firebase.auth?.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

//        navView.menu.findItem(R.id.logout).setOnMenuItemClickListener {
//            val tokenMap: MutableMap<String, Any> = HashMap()
//            tokenMap["token_id"] = ""
//
//            Firebase.firestore?.collection("Users")?.document(Firebase.userId)?.update(tokenMap)
//            Firebase.databaseUsers?.child(Firebase.userId)?.child("tokenId")?.removeValue()
//            Firebase.auth?.signOut()
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            startActivity(intent)
//            return@setOnMenuItemClickListener true
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        val requestOptions = RequestOptions()
            .placeholder(R.mipmap.ic_launcher_round)
            .skipMemoryCache(true)
            .error(R.mipmap.ic_launcher_round)

        GlideApp.with(applicationContext)
            .applyDefaultRequestOptions(requestOptions)
            .load(ActiveUsersData.currentUser?.profilePhotoUrl)
            .into(nav_profile_img)


        nav_name.text = ActiveUsersData.currentUser?.fullName
        nav_username.text = ActiveUsersData.currentUser?.username
        nav_email.text = ActiveUsersData.currentUser?.email
        return true
    }


    override fun onSupportNavigateUp(): Boolean {
            val navController = findNavController(R.id.nav_host_fragment)
            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }
}
