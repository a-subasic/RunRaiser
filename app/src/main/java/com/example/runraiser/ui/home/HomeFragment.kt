package com.example.runraiser.ui.home

import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.runraiser.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.roundToInt

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mMap: GoogleMap

    private lateinit var previousLatLng: LatLng
    private lateinit var currentLatLng: LatLng
    private var distance: Float = 0F
    private var speed: Float = 0F

//    private var location1: LatLng = LatLng(45.330992, 14.430281)
//    private var location2: LatLng = LatLng(45.240600, 14.397654)
    private var location1: LatLng = LatLng(13.0356745, 77.5881522)
    private var location2: LatLng = LatLng(13.029727, 77.5933021)
    private lateinit var marker: Marker

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

        var stopTime: Long = 0
        var timer: Timer? = null
        start.setOnClickListener {
            tv_distance.text = distance.toString()
            tv_speed.text = speed.toString()
            chronometer.base = SystemClock.elapsedRealtime()+stopTime
            chronometer.start()
            timer = Timer()
            val task = object: TimerTask() {
                var timesRan = 0
                override fun run() {
                    println("timer passed ${++timesRan} time(s)")
                    calculateLatLng()
                }
            }
            timer!!.schedule(task, 0, 1000)
        }
        stop.setOnClickListener {
            stopTime = chronometer.base-SystemClock.elapsedRealtime()
            chronometer.stop()
            timer?.cancel()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

//        mMap.addMarker(MarkerOptions().position(location1).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location1, 15.0f))
//        mMap.addMarker(MarkerOptions().position(location2).title("Marker in Sydney"))

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
                    marker = mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
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
                    val currentLatLng = LatLng(latitude, longitude)
                    marker.position = currentLatLng
                    distance(previousLatLng, currentLatLng)
                }
            }
        }, Looper.getMainLooper())
    }

    private fun distance(start: LatLng, end: LatLng) {
        val location1 = Location("locationA")
        location1.latitude = start.latitude
        location1.longitude = start.longitude
        val location2 = Location("locationB")
        location2.latitude = end.latitude
        location2.longitude = end.longitude
        val distance_tmp = location1.distanceTo(location2)
//        println(distance*1000)

        val lineoption = PolylineOptions()
        lineoption.add(start, end)
        lineoption.width(10f)
        lineoption.color(Color.BLUE)
        lineoption.geodesic(true)
        mMap.addPolyline(lineoption)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(end, 25.0f))
        distance += distance_tmp
        speed = (distance_tmp * 3.6).toFloat()
        if(distance > 999) {
            var distance_km = distance/1000
            distance_km = BigDecimal(distance_km.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toFloat()
            tv_distance.text = distance_km.toString() + " km"
        }
        else {
            distance = BigDecimal(distance.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toFloat()
            tv_distance.text = distance.toString() + " m"
        }
        speed = BigDecimal(speed.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toFloat()
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
}


