package com.example.gpsgame

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.navigationgame.PlaceItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random


class MainActivity : AppCompatActivity() {


    var placeItems = mutableListOf<PlaceItem>()

    lateinit var fusedLocationClient: FusedLocationProviderClient

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    lateinit var navController: NavController

    var userFullName = ""

//    TODO change number of created place items per day
    private val dailyItemsAmount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_map, R.id.navigation_list, R.id.navigation_profile ))
        navView.setupWithNavController(navController)

        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser

        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

//                    Get user location and create place items with location
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->

                        setupActive( location )
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Userlogin", "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupActive(location: Location) {

        val docRef = db.collection("users")
            .document(auth.currentUser?.uid.toString())
            .collection("placeItems")

        val yesterday = DateTime.now().minusDays(1).toDate()

        docRef.whereGreaterThanOrEqualTo("created", yesterday).get().addOnSuccessListener { resultToday ->

            if ( resultToday.documents.size < dailyItemsAmount) {
                docRef.get().addOnSuccessListener { result ->

//                Clear placeItems collection in database
                    for ((index, document) in result.documents.withIndex()) {

                        docRef.document(result.documents[index].id).update(mapOf("completed" to false, "active" to false))
                    }

                    for (i in 0 until dailyItemsAmount) {
//                      Create coordinates in close range of user location
                        var lat = Random.nextDouble(location.latitude - 0.01, location.latitude + 0.01)
                        var long = Random.nextDouble(location.longitude - 0.01, location.longitude + 0.01)
                        lat = Math.round(lat * 1000000.0) / 1000000.0
                        long = Math.round(long * 1000000.0) / 1000000.0

                        val item = PlaceItem("Random place", lat, long)

                        docRef.add(item)
                    }
                }
            }
        }
    }

//    Navigation functions for profile and settings fragments
    fun goToSettings()  = navController.navigate( R.id.navigation_settings )
    fun goToProfile()   = navController.navigateUp()
}
