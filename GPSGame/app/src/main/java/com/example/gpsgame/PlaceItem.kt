package com.example.navigationgame

import com.google.android.gms.maps.model.Circle

class PlaceItem(val name: String, val latitude: Double, val longitude: Double, val radius: Double = 100.0 ) {

    val points: Int
    var circle: Circle? = null

    init {

        points = 10
    }

    fun complete() {

        // Null check?
        circle?.remove()
    }


}