package com.example.gpsgame

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_map.*


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    lateinit var activity: MainActivity
    var placeItems = mutableListOf<PlaceItem>()

    // 1
    private lateinit var locationCallback: LocationCallback
    // 2
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        // 3
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        auth = FirebaseAuth.getInstance()

        return inflater.inflate(R.layout.fragment_map, container, false)
    }


    override fun onStart() {
        super.onStart()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation

                Log.d("loccallback", lastLocation.latitude.toString() + " " + lastLocation.longitude.toString())

                if ( placeItems.size > 0 ) {

                    val itemsCopy = placeItems.toMutableList()
                    val distance = FloatArray(2)

                    for( item in itemsCopy ) {

                        Location.distanceBetween(
                            lastLocation.latitude,
                            lastLocation.longitude,
                            item.latitude,
                            item.longitude,
                            distance )

                        if ( distance[0] <= item.radius ) {

                            item.complete()
                            placeItems.remove(item)

                            db.collection("users")
                                .document(auth.currentUser?.uid.toString())
                                .collection("placeItems")
                                .document(item.id)
                                .set(item)
                        }
                        else {
                            Log.d("loccallback", "Not in circle")
                        }
                    }
                }
            }
        }

        createLocationRequest()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        activity = context as MainActivity

        placeItems = activity.placeItems
    }

    override fun onMapReady(map: GoogleMap) {

        if (map == null) {
            return
        }
        map.let {
            googleMap = it
        }

        map.uiSettings.isZoomControlsEnabled = false
        map.setOnMarkerClickListener(this)

        setUpMap()
    }

    override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this.requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        // 1
        googleMap.isMyLocationEnabled = true

        // 2
        fusedLocationClient.lastLocation.addOnSuccessListener(this.requireActivity()) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                val docRef = db.collection("users")
                                                .document(auth.currentUser?.uid.toString())
                                                .collection("placeItems")


//                Get place items from db and add circles
//                doesn't change on remove
                docRef.addSnapshotListener { querySnapshot, e ->

                    if ( querySnapshot != null && querySnapshot.documents.size > 0 ) {

//                        Remove all circles from map and clear placeItems array
                        for ( item in placeItems ) item.complete()
                        placeItems.clear()

                        for ( doc in querySnapshot.documents ) {

                            if ( doc["active"] as Boolean && !(doc["completed"] as Boolean) ) {

                                val item = PlaceItem(
                                    doc["name"].toString(),
                                    doc["latitude"] as Double,
                                    doc["longitude"] as Double,
                                    doc.id
                                )
                                val latlong = LatLng(item.latitude, item.longitude)
                                val circle = googleMap.addCircle(
                                    CircleOptions()
                                        .center(latlong)
                                        .radius(item.radius)
                                        .strokeColor(Color.parseColor("#33FFF3"))
                                        .fillColor(Color.parseColor("#4933FFF3"))
                                )

                                item.circle = circle
                                placeItems.add(item)
                            }

                        }
                        activity.redrawList()
                    }

                }
            }
        }
    }

    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this.requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 800
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this.requireActivity())
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this.requireActivity(),
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    // 1
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    fun focusMap( lat: Double, long: Double ) {

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, long), 15f))
    }


}
