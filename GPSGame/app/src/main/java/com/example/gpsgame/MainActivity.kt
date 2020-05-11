package com.example.gpsgame

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.navigationgame.PlaceItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {


    var placeItems = mutableListOf<PlaceItem>()

    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_map, R.id.navigation_list, R.id.navigation_profile ))
        navView.setupWithNavController(navController)


        val item = PlaceItem( "Gamla stan",59.325695, 18.071869 )
        val item2 = PlaceItem( "Tantolunden", 59.312975, 18.049348 )
        placeItems.add(item)
        placeItems.add(item2)

//
//        val user: MutableMap<String, Any> = HashMap()
//        user["first"] = "Ada"
//        user["last"] = "Lovelace"
//        user["born"] = 1815
//
//        db.collection("users")
//            .add(user)
//            .addOnSuccessListener { documentReference ->
//                Log.d(
//                    "DB Callback: ",
//                    "DocumentSnapshot added with ID: " + documentReference.id
//                )
//            }
//            .addOnFailureListener { e -> Log.w("DB Callback: ", "Error adding document", e) }

    }
}
