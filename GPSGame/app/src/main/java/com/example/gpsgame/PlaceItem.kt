package com.example.navigationgame

import com.google.android.gms.maps.model.Circle
import com.google.firebase.firestore.Exclude
import java.util.*


class PlaceItem(val name: String, val latitude: Double, val longitude: Double, @get:Exclude var id: String = "", val radius: Double = 100.0  ) {

    val points: Int
    @get:Exclude var circle: Circle? = null
    var created = Date().toString()
    var isActive: Boolean = true

    init {

        points = 10
    }

    fun complete() {

        // Null check?
        circle?.remove()
        isActive = false
    }


}