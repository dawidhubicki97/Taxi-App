package com.example.pracainz.models

import com.firebase.geofire.GeoLocation

data class OrdersInProgress(val driver: String,val user:String,val startlat:Double,val startlng:Double,val targetlat:Double,val targetlng:Double,val price:Double,val distance:Int){
    constructor():this("","", 0.0,0.0,0.0,0.0,0.0,0)
}