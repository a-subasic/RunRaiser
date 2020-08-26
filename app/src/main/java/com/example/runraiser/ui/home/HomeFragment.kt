package com.example.runraiser.ui.home

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.runraiser.ActiveUsersDataCallback
import com.example.runraiser.Firebase
import com.example.runraiser.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLngBounds.Builder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.send_message_dialog.view.*
import kotlinx.android.synthetic.main.trainig_setup_dialog.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.OutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mMap: GoogleMap

    private lateinit var previousLatLng: LatLng
    private lateinit var currentLatLng: LatLng
    private var distance: Float = 0F
    private var distanceKm: Float = 0F
    private var speed: Float = 0F
    private lateinit var trainingId: String
    private var timesRan: Int = 0

    private var latLngArray: ArrayList<LatLng> = ArrayList()
    private var speedArray: ArrayList<Float> = ArrayList()
    private lateinit var marker: Marker
    private var circle: Circle? = null
    val userId = Firebase.auth!!.currentUser!!.uid

    private var geoQuery: GeoQuery? = null
    private lateinit var geoFire: GeoFire

    private var activeUsersMarkers: MutableMap<String, Marker> = HashMap()
    private var entered: HashMap<String, Int> = HashMap()

    private var mFirestore: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFirestore = FirebaseFirestore.getInstance()

        //setting up GeoFire
        geoFire = GeoFire(FirebaseDatabase.getInstance().getReference("MyLocation/${userId}"))

        ActiveUsersData.getActiveUsersData(object: ActiveUsersDataCallback {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onActiveUsersDataCallback(activeUsersData: HashMap<String, ActiveUser>) {
                addActiveUsersPins()
            }
        })

        var stopTime: Long = 0
        var timer: Timer? = null
        start_btn.setOnClickListener {
            val mDialogView = LayoutInflater.from(context).inflate(R.layout.trainig_setup_dialog, null)
            val mBuilder = AlertDialog.Builder(context)
                .setView(mDialogView)
                .setTitle("Training Setup")

            val  mAlertDialog = mBuilder.show()

            mDialogView.ok_btn.setOnClickListener {
                //get text from EditTexts of custom layout
                trainingId = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT)
                val kilometers = mDialogView.et_kilometers.text.toString()
                val valueKn = mDialogView.et_value.text.toString()

                if(mDialogView.et_kilometers.text.toString().isEmpty() || mDialogView.et_value.text.toString().isEmpty()) {
                    Toast.makeText(activity, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
                }
                else if (kilometers.toInt() > 80) {
                    mDialogView.et_kilometers.error = "Maximum is 80km"
                    mDialogView.et_kilometers.requestFocus()
                }
                else if (valueKn.toInt() > 100) {
                    mDialogView.et_value.error = "Maximum is 100kn"
                    mDialogView.et_value.requestFocus()
                }
                else {
                    mAlertDialog.dismiss()
                    var startDate: String?
                    startDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                        current.format(formatter)
                    } else {
                        val date = Date()
                        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
                        formatter.format(date)
                    }
                    val training =
                        Firebase.auth?.uid?.let { it1 -> Training(trainingId, it1, kilometers.toInt(), valueKn.toInt(), startDate) }

                    Firebase.databaseTrainings
                        ?.child(trainingId)
                        ?.setValue(training)

                    Firebase.databaseUsers!!.child(userId).child("inTraining").setValue(true)

                    start_btn.visibility = View.GONE
                    training_content.visibility = View.VISIBLE
                    stop_btn.visibility = View.VISIBLE
                    reset_btn.visibility = View.GONE

                    tv_distance.text = distance.toString()
                    tv_speed.text = speed.toString()
                    chronometer.base = SystemClock.elapsedRealtime()+stopTime
                    chronometer.start()
                    timer = Timer()
                    val task = object: TimerTask() {
                        override fun run() {
                            println("timer passed ${++timesRan} time(s)")
                            calculateLatLng()
                        }
                    }
                    timer!!.schedule(task, 0, 1000)
                }

                stop_btn.setOnClickListener {
                    val raisedVal = (kotlin.math.floor(distanceKm) * valueKn.toDouble()).toInt()
                    val raisedAlert = AlertDialog.Builder(context)
                    Firebase.databaseUsers!!.child(userId).child("inTraining").setValue(false)

                    if(raisedVal > 0) {
                        raisedAlert.setTitle("Congratulations!")
                            ?.setMessage("You raised $raisedVal kn!")
                            ?.setPositiveButton("OK :)") { dialog, _ ->
                                stop_btn.visibility = View.GONE
                                reset_btn.visibility = View.VISIBLE
                                dialog.cancel()
                            }?.setCancelable(false)
                            ?.show()
                    }
                    else {
                        raisedAlert.setTitle("Oh no!")
                            ?.setMessage("Unfortunately you didn’t run enough mileage to raise money.")
                            ?.setPositiveButton("OK :(") { dialog, _ ->
                                stop_btn.visibility = View.GONE
                                reset_btn.visibility = View.VISIBLE
                                dialog.cancel()
                            }?.setCancelable(false)
                            ?.show()
                    }
                    latLngArray.add(currentLatLng)
                    stopTime = chronometer.base-SystemClock.elapsedRealtime()
                    zoomRoute(mMap, latLngArray)
                    chronometer.stop()
                    timer?.cancel()

                    val ref = Firebase.database?.getReference("/Trainings/${trainingId}")
                    ref?.child("time")?.setValue(chronometer.text.toString())
                    ref?.child("moneyRaised")?.setValue(raisedVal)
                }
                reset_btn.setOnClickListener {
                    training_content.visibility = View.GONE
                    start_btn.visibility = View.VISIBLE
                    reset_btn.visibility = View.GONE

                    mMap.clear()

                    val locationRequest = LocationRequest()
                    locationRequest.interval = 10000
                    locationRequest.fastestInterval = 3000
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                    LocationServices.getFusedLocationProviderClient(requireContext()).requestLocationUpdates(locationRequest, object:
                        LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            super.onLocationResult(locationResult)
                            LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(this)
                            if(locationResult.locations.size > 0) {
                                val latestLocationIndex = locationResult.locations.size-1
                                val latitude = locationResult.locations[latestLocationIndex].latitude
                                val longitude = locationResult.locations[latestLocationIndex].longitude
                                // Add a marker in Sydney and move the camera
                                val sydney = LatLng(latitude, longitude)
                                previousLatLng = sydney
                                latLngArray.add(previousLatLng)
                                marker = mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 25.0f))
                            }
                        }
                    }, Looper.getMainLooper())
                }
            }
            //cancel button click of custom layout
            mDialogView.cancel_btn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        openMessageWindow()

//        mMap.addCircle(CircleOptions().center(LatLng(45.3310694, 14.4303084)).radius(500.0).strokeColor(Color.RED).fillColor(0x220000FF).strokeWidth(7.0f))

//        val URL = getDirectionURL(location1, location2)
//        GetDirection(URL).execute()

        LocationServices.getFusedLocationProviderClient(requireContext()).requestLocationUpdates(locationRequest, object:
            LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(this)
                if(locationResult.locations.size > 0) {
                    val latestLocationIndex = locationResult.locations.size-1
                    val latitude = locationResult.locations[latestLocationIndex].latitude
                    val longitude = locationResult.locations[latestLocationIndex].longitude
                    // Add a marker in Sydney and move the camera
                    val sydney = LatLng(latitude, longitude)
                    previousLatLng = sydney
                    latLngArray.add(previousLatLng)
                    marker = mMap.addMarker(MarkerOptions().position(sydney).title("Me"))
                    marker.tag = userId
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 25.0f))
                }
            }
        }, Looper.getMainLooper())
    }

    private fun calculateLatLng() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationServices.getFusedLocationProviderClient(requireContext()).requestLocationUpdates(locationRequest, object:
            LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(this)
                if(locationResult.locations.size > 0) {
                    val latestLocationIndex = locationResult.locations.size-1
                    val latitude = locationResult.locations[latestLocationIndex].latitude
                    val longitude = locationResult.locations[latestLocationIndex].longitude
                    currentLatLng = LatLng(latitude, longitude)
                    Firebase.databaseUsers!!.child(userId).child("lastLat").setValue(currentLatLng.latitude)
                    Firebase.databaseUsers!!.child(userId).child("lastLng").setValue(currentLatLng.longitude)
//                    geoFire.setLocation(userId, GeoLocation(currentLatLng.latitude, currentLatLng.longitude))
                    marker.position = currentLatLng
                    addCircleArea()
                    distance(previousLatLng, currentLatLng)
                }
            }
        }, Looper.getMainLooper())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addActiveUsersPins() {
        ActiveUsersData.activeUsersData.forEach { (s, activeUser) ->
            activeUsersMarkers[s] = mMap.addMarker(MarkerOptions().position(LatLng(activeUser.lastLat, activeUser.lastLng)).title(activeUser.username))
            activeUsersMarkers[s]?.tag = activeUser.id
            activeUsersMarkers[s]?.isVisible = false
            entered[s] = 0
        }
    }

    private fun addCircleArea() {
//        if(geoQuery != null) {
//            geoQuery!!.removeAllListeners()
//        }

        ActiveUsersData.getActiveUsersData(object: ActiveUsersDataCallback {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onActiveUsersDataCallback(activeUsersData: HashMap<String, ActiveUser>) {
                activeUsersData.forEach { (s, activeUser) ->
                    if(!activeUsersMarkers.containsKey(s)) {
                        activeUsersMarkers[s] = mMap.addMarker(MarkerOptions().position(LatLng(activeUser.lastLat, activeUser.lastLng)).title(activeUser.username))
                        activeUsersMarkers[s]?.tag = activeUser.id
                        activeUsersMarkers[s]?.isVisible = false
                        entered[s] = 0
                    }
                    activeUsersMarkers[s]?.position = LatLng(activeUser.lastLat, activeUser.lastLng)
                    geoFire.setLocation(s, GeoLocation(activeUser.lastLat, activeUser.lastLng))
                }
                activeUsersMarkers.forEach { (s, _) ->
                    Firebase.databaseUsers?.child(s)?.child("inTraining")
                        ?.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.value == false) {
                                    activeUsersMarkers[s]?.remove()
                                    activeUsersMarkers.remove(s)
                                    geoFire.removeLocation(s)
                                    entered.remove(s)
                                }
                            }
                        })
                }
            }
        })


        if(circle == null) {
            circle = mMap.addCircle(CircleOptions().center(currentLatLng).radius(500.0).strokeColor(Color.BLUE).fillColor(0x220000FF).strokeWidth(5.0f))
        }

        circle?.center = currentLatLng
        if(geoQuery == null) geoQuery = geoFire.queryAtLocation(GeoLocation(currentLatLng.latitude, currentLatLng.longitude), 0.5)
        else geoQuery!!.center = GeoLocation(currentLatLng.latitude, currentLatLng.longitude)
        geoQuery!!.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryReady() {}
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                if(entered[key] == 0 && key != userId && ActiveUsersData.activeUsersData[key]?.username != null) sendNotification("ENTERED", String.format("%s entered the dangerous area",
                    ActiveUsersData.activeUsersData[key]?.username))
                if (key != null) {
                    entered[key]?.plus(1)?.let { entered.put(key, it) }
                }
                activeUsersMarkers[key]?.isVisible = true
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                println("moving")
                println(key)
            }

            override fun onKeyExited(key: String?) {
                println("exited")
                println(key)
                if (key != null) {
                    entered.put(key, 0)
                }
                activeUsersMarkers[key]?.isVisible = false
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun sendNotification(title: String, content: String) {
        val NOTIFICATION_CHANNEL_ID = "run_raiser"
        val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "myNotification", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = "Channel desc"

            notificationManager.createNotificationChannel(notificationChannel)
            val builder = context?.let { NotificationCompat.Builder(it, NOTIFICATION_CHANNEL_ID) }
            builder?.setContentTitle(title)
                ?.setContentText(content)
                ?.setAutoCancel(false)
                ?.setSmallIcon(R.mipmap.ic_launcher)
                ?.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))

            val notification = builder?.build()
            notificationManager.notify(Random().nextInt(), notification)
        }
    }

    private fun distance(start: LatLng, end: LatLng) {
        latLngArray.add(start)
        val location1 = Location("locationA")
        location1.latitude = start.latitude
        location1.longitude = start.longitude
        val location2 = Location("locationB")
        location2.latitude = end.latitude
        location2.longitude = end.longitude
        val distance_tmp = location1.distanceTo(location2)

        val lineoption = PolylineOptions()
        lineoption.add(start, end)
        lineoption.width(10f)
        lineoption.color(Color.BLUE)
        lineoption.geodesic(true)
        mMap.addPolyline(lineoption)
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(end, 25.0f))
        distance += distance_tmp
        distanceKm = distance/1000
        speed = (distance_tmp * 3.6).toFloat()
        if(distance > 999) {
            distanceKm = distance/1000
            distanceKm = BigDecimal(distanceKm.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toFloat()
            tv_distance.text = distanceKm.toString() + " km"
        }
        else {
            distance = BigDecimal(distance.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toFloat()
            tv_distance.text = distance.toString() + " m"
        }
        speed = BigDecimal(speed.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toFloat()
        speedArray.add(speed)
        tv_speed.text = speed.toString() + " km/h"
        previousLatLng = end
    }

    private fun getDirectionURL(origin: LatLng, dest: LatLng) : String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key="+getString(R.string.google_maps_key)
    }

    inner class GetDirection(val url: String) : AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg p0: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()?.string()
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()
                for(i in 0 until respObj.routes[0].legs[0].steps.size) {
                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
                        ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
                    path.add(startLatLng)
                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
                        ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.add(endLatLng)
//                    println(i)
//                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e: Exception) {
                e.printStackTrace()
            }
            println(result)
            return result
        }
        override fun onPostExecute(result: List<List<LatLng>>?) {
            val lineoption = PolylineOptions()

            if (result != null) {
                for (i in result.indices){
                    lineoption.addAll(result[i])
                    lineoption.width(10f)
                    lineoption.color(Color.BLUE)
                    lineoption.geodesic(true)
                }
            }
            mMap.addPolyline(lineoption)
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

    private fun snapShot() {
        val callback: GoogleMap.SnapshotReadyCallback = object : GoogleMap.SnapshotReadyCallback {
            var bitmap: Bitmap? = null
            override fun onSnapshotReady(snapshot: Bitmap) {
                bitmap = snapshot
                saveImage(bitmap!!)
            }
        }
        mMap.snapshot(callback)
    }

    @Throws(IOException::class)
    private fun saveImage(bitmap: Bitmap) {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "myImage${trainingId}.png")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        val resolver: ContentResolver? = context?.contentResolver
        val uri: Uri? =
            resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        var imageOutStream: OutputStream? = null
        println(uri)

        try {
            if (uri == null) {
                throw IOException("Failed to insert MediaStore row")
            }
            imageOutStream = resolver.openOutputStream(uri)
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                throw IOException("Failed to compress bitmap")
            }
        } finally {
            imageOutStream?.close()
            if (uri != null) {
                uploadProfilePhotoToFirebaseStorage(uri)
            }
        }
    }

    private fun uploadProfilePhotoToFirebaseStorage(uri: Uri) {
        val refStorage =
            FirebaseStorage.getInstance().getReference("/images/maps_screenshots/${trainingId}")

        refStorage.putFile(uri)
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

    private fun saveProfilePhotoToFirebaseDatabase(trainingImageUrl: String) {
        val endDate: String?
        endDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            current.format(formatter)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
            formatter.format(date)
        }

        val ref = Firebase.database?.getReference("/Trainings/${trainingId}")
        ref?.child("trainingMapScreenshot")?.setValue(trainingImageUrl)?.addOnSuccessListener {
            Log.d(tag, "Saved profile photo to firebase database")
        }?.addOnFailureListener {
            Log.d(tag, "Failed to save profile photo firebase database")
        }
        if(distanceKm < 1) {
            distanceKm = BigDecimal(distanceKm.toDouble()).setScale(3, RoundingMode.HALF_EVEN).toFloat()
        }
        ref?.child("distanceKm")?.setValue(distanceKm.toString())
        ref?.child("avgSpeed")?.setValue(BigDecimal(speedArray.average()).setScale(2, RoundingMode.HALF_EVEN).toFloat().toString())
        ref?.child("endDate")?.setValue(endDate)

        distance = 0F
        distanceKm = 0F
        speed = 0F
        timesRan = 0
        latLngArray = ArrayList()
        speedArray = ArrayList()
    }

    private fun zoomRoute(
        googleMap: GoogleMap?,
        lstLatLngRoute: ArrayList<LatLng>
    ) {
        if (googleMap == null || lstLatLngRoute.isEmpty()) return
        val boundsBuilder: LatLngBounds.Builder = Builder()
        for (latLngPoint in lstLatLngRoute) boundsBuilder.include(
            latLngPoint
        )
        val routePadding = 100
        val latLngBounds: LatLngBounds = boundsBuilder.build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding))
        googleMap.setPadding(10,10,10,10)
        Timer("Loading Map", false).schedule(2000) {
            snapShot()
        }
    }

    private fun openMessageWindow() {
        mMap.setOnInfoWindowClickListener(object: GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(p0: Marker?) {
                if(p0?.tag.toString() != userId) {
                    val mDialogView =
                        LayoutInflater.from(context).inflate(R.layout.send_message_dialog, null)
                    val mBuilder = AlertDialog.Builder(context)
                        .setView(mDialogView)
                        .setTitle("Message")

                    val mAlertDialog = mBuilder.show()
                    mDialogView.send_btn.setOnClickListener {
                        val message = mDialogView.et_message.text.toString()
                        if (message.isEmpty()) {
                            mDialogView.et_message.error = "Message cannot be empty!"
                            mDialogView.et_message.requestFocus()
                        } else {
                            mAlertDialog.dismiss()
                            val notificationMessage: MutableMap<String, Any> = HashMap()
                            notificationMessage["fromId"] = Firebase.userId
                            notificationMessage["message"] = message

                            mFirestore!!
                                .collection("Users/${p0?.tag.toString()}/Notifications")
                                .add(notificationMessage)
                                .addOnSuccessListener {
                                    Log.d(tag, "Notification saved to Firestore")
                                }
                        }
                    }
                    //cancel button click of custom layout
                    mDialogView.cancel_msg_btn.setOnClickListener {
                        //dismiss dialog
                        mAlertDialog.dismiss()
                    }
                }
            }
        })
    }
}

class Training(
    val id: String,
    val userId: String,
    val kilometers: Int,
    val value: Int,
    val startDate: String
)

