package com.example.pracainz.models

import com.google.android.gms.maps.model.LatLng

class OrderData(val price:Double, val distance: Int) {
    constructor():this(0.0,0)
}