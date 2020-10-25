package com.example.pracainz.models

data class GoogleDirections (

    val geocoded_waypoints : List<Geocoded_waypoints>,
    val routes : List<Routes>,
    val status : String
)
data class Distance (

    val text : String,
    val value : Int
)
data class Duration (

    val text : String,
    val value : Int
)
data class Bounds (

    val northeast : Northeast,
    val southwest : Southwest
)
data class End_location (

    val lat : Double,
    val lng : Double
)

data class Geocoded_waypoints (

    val geocoder_status : String,
    val place_id : String,
    val types : List<String>
)
    data class Legs (
        val duration_in_traffic : Duration_in_traffic,
        val distance : Distance,
        val duration : Duration,
        val end_address : String,
        val end_location : End_location,
        val start_address : String,
        val start_location : Start_location,
        val steps : List<Steps>,
        val traffic_speed_entry : List<String>,
        val via_waypoint : List<String>
)
data class Duration_in_traffic (

    val text : String,
    val value : Int
)
data class Northeast (

    val lat : Double,
    val lng : Double
)
data class Overview_polyline (

    val points : String
)
data class Polyline (

    val points : String
)
data class Routes (

    val bounds : Bounds,
    val copyrights : String,
    val legs : List<Legs>,
    val overview_polyline : Overview_polyline,
    val summary : String,
    val warnings : List<String>,
    val waypoint_order : List<String>
)
data class Southwest (

    val lat : Double,
    val lng : Double
)
data class Start_location (

    val lat : Double,
    val lng : Double
)
data class Steps (

    val distance : Distance,
    val duration : Duration,
    val end_location : End_location,
    val html_instructions : String,
    val polyline : Polyline,
    val start_location : Start_location,
    val travel_mode : String
)