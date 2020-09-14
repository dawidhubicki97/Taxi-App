package com.example.pracainz.models

import com.firebase.geofire.GeoLocation

data class OrdersInProgress(val driver: String,val User:String,val startlat:Double,val startlng:Double,val targetlat:Double,val targetlng:Double){
    constructor():this("","", 0.0,0.0,0.0,0.0)
}