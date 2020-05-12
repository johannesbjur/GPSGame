package com.example.gpsgame

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.navigationgame.PlaceItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {


    var placeItems = mutableListOf<PlaceItem>()

    var db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

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

        auth = FirebaseAuth.getInstance()

        val item = PlaceItem( "Gamla stan",59.325695, 18.071869 )
        val item2 = PlaceItem( "Tantolunden", 59.312975, 18.049348 )
        placeItems.add(item)
        placeItems.add(item2)

        Log.d("date", item.created.toString())
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

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser


        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Userlogin", "signInAnonymously:success")

                    val user = auth.currentUser

                    val data: MutableMap<String, Any> = HashMap()

//                    Set user data
//                    move somewhere else
//                    data["first"] = "Johannes"
//                    data["last"] = "Bjurstromer"
//                    db.collection("users")
//                        .document(auth.currentUser?.uid.toString())
//                        .set(data)

                    var docRef = db.collection("users")
                                    .document(auth.currentUser?.uid.toString())
                                    .collection("active")

                    docRef.get().addOnSuccessListener { result  ->

                        var firstActive = result.documents[0]

                        Log.d( "activeDoc", firstActive.data?.get("created").toString())

                        Log.d( "date", Date().time.toString())

                        var compareDate = Date( firstActive.data?.get("created").toString() )
                        var nowDate = Date()

                        if ( compareDate.time < ( nowDate.time - 86400000 ) ) {

                            Log.d( "activeDoc", "compare")
//                            Clear active and create new active place items
                        }

                    }

//                    val item = PlaceItem( "Gamla stan",59.325695, 18.071869 )
//                    db.collection("users")
//                        .document(auth.currentUser?.uid.toString())
//                        .collection("active")
//                        .add(item)



                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Userlogin", "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }

    }


}
