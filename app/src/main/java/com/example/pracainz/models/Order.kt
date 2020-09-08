package com.example.pracainz.models

import com.google.android.gms.maps.model.LatLng

class Order(val startLocation:LocationModel,val endLocation:LatLng,val price:Double,val distance:Double) {
}