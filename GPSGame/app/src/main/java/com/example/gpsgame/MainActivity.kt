package com.example.gpsgame

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.navigationgame.PlaceItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.joda.time.DateTime
import kotlin.random.Random


class MainActivity : AppCompatActivity() {


    var placeItems = mutableListOf<PlaceItem>()

    lateinit var fusedLocationClient: FusedLocationProviderClient

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    lateinit var navController: NavController

    var userFullName = ""

    private val mapFragment         = MapFragment()
    private val profileFragment     = ProfileFragment()
    private val listFragment        = ListFragment()
    private val settingsFragment    = SettingsFragment()
    private var activeFragment: Fragment        = mapFragment
    private val fragmentManager  = supportFragmentManager


    val dailyItemsAmount = 2

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


        fragmentManager.beginTransaction().apply {
            add(R.id.container, mapFragment, "map")
            add(R.id.container, listFragment, "list").hide(listFragment)
            add(R.id.container, profileFragment, "profile").hide(profileFragment)
            add(R.id.container, settingsFragment, "settings").hide(settingsFragment)
        }.commit()

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

        findViewById<BottomNavigationView>(R.id.nav_view).setOnNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.navigation_map -> {
                    swapTo( mapFragment )
                    true
                }
                R.id.navigation_list -> {
                    swapTo( listFragment )
                    true
                }
                R.id.navigation_profile -> {
                    swapTo( profileFragment )
                    true
                }
                else -> false
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

                        docRef.document(result.documents[index].id).update(mapOf("active" to false))
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
    fun goToSettings() = swapTo( settingsFragment )
    fun goToProfile() = swapTo( profileFragment )

    fun goFocusMap( lat: Double, long: Double ) {

        val fragment = supportFragmentManager.findFragmentByTag("map") as MapFragment
        fragment.focusMap( lat, long )

        swapTo( mapFragment )

        findViewById<BottomNavigationView>(R.id.nav_view).selectedItemId = R.id.navigation_map
    }

    private fun swapTo( fragment: Fragment ) {

        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit()
        activeFragment = fragment
    }

    fun redrawList() {

        val fragment = supportFragmentManager.findFragmentByTag("list") as ListFragment
        fragment.resetAdapter()
    }
}
