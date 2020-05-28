package com.example.gpsgame

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        var docRef = db.collection("users")
            .document(auth.currentUser?.uid.toString())
            .collection("placeItems")

        docRef.get().addOnSuccessListener { result ->

            if ( result.documents.size > 0 ) {

//                TODO look over if firstActive is the right way to do this
                var firstActive = result.documents[0]
                val compareDate = (firstActive.data?.get("created") as Timestamp).toDate()
                val nowDate = Date()

                if ( compareDate?.time > nowDate?.time - 86400000  ) return@addOnSuccessListener
            }

//                Clear placeItems collection in database
            for ((index, document) in result.documents.withIndex()) {

                docRef.document(result.documents[index].id).update(mapOf("completed" to false, "active" to false))
            }

//            TODO change number of created place items per day
            for (i in 0..1) {
//                Create coordinates in close range of user location
                var lat = Random.nextDouble(location.latitude - 0.01, location.latitude + 0.01)
                var long = Random.nextDouble(location.longitude - 0.01, location.longitude + 0.01)
                lat = Math.round(lat * 1000000.0) / 1000000.0
                long = Math.round(long * 1000000.0) / 1000000.0

                val item = PlaceItem("Random place", lat, long)

                docRef.add(item)
            }

//            val item = PlaceItem( "Gamla stan",59.325695, 18.071869 )
//            docRef.add(item)
        }
    }

//    Navigation functions for profile and settings fragments
    fun goToSettings()  = navController.navigate( R.id.navigation_settings )
    fun goToProfile()   = navController.navigateUp()
}
