package com.example.pracainz.fragments


import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView

import com.example.pracainz.R
import com.example.pracainz.models.GoogleDirections
import com.example.pracainz.models.LocationModel
import com.example.pracainz.models.Order
import com.example.pracainz.models.Polyline
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryDataEventListener
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.fragment_route.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.lang.Exception
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.util.*


class RouteFragment : Fragment() {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var distance:Int?=null
    private var decodedPoly:String?=null
    private var targetLocation:LatLng?=null
    private var myLastLocation:LocationModel?=null
    private var root:View?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        getMyLastLocation()
        root=inflater.inflate(R.layout.fragment_route, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sendToMapButton.setOnClickListener{
            Log.d("droga","sendToMapButton")
            val uid= FirebaseAuth.getInstance().uid
            val ref= FirebaseDatabase.getInstance().getReference("/OrderRequests")
            //val thisOrder= Order(myLastLocation!!,targetLocation!!,getPrice()!!,distance?.toDouble()!!)
           // ref.push().setValue(thisOrder)
            var geofire= GeoFire(ref)
            geofire.setLocation(uid, GeoLocation(myLastLocation!!.longitude, myLastLocation!!.latitude),GeoFire.CompletionListener { key, error ->

            })
            getClosestDriver()
            val fragmentMap=MapFragment()
            var bundle= Bundle()
            bundle.putInt("distance",distance!!)
            bundle.putString("decodedPoly",decodedPoly!!)
            fragmentMap.arguments=bundle
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.container, fragmentMap).commit()
        }
        Places.initialize(activity!!.applicationContext, "AIzaSyAAfIfjV2D8akbv2jCyPoaAfSKsD85TepQ")
        val placesClient = Places.createClient(activity!!.applicationContext)
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as? AutocompleteSupportFragment
        autocompleteFragment!!.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG))
        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: ${place.name}, ${place.latLng.toString()}")
                targetLocation=place.latLng
                findRoute()
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }
    fun getPrice(): Double? {
        return (distance!!*3.0)/1000
    }
    fun getClosestDriver(){
        val ref= FirebaseDatabase.getInstance().getReference("/AvailableDrivers")
        var geofire=GeoFire(ref)
        var geoQuery=geofire.queryAtLocation(GeoLocation(myLastLocation!!.longitude,myLastLocation!!.latitude),10.0)
        geoQuery.addGeoQueryEventListener(object:GeoQueryEventListener{
            override fun onGeoQueryReady() {
                Log.d("znaleziono","z")
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                Log.d("znaleziono",key)
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                Log.d("znaleziono",key)
            }

            override fun onKeyExited(key: String?) {
                Log.d("znaleziono",key)
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Log.d("znaleziono",error.toString())
            }

        })
    }


    @SuppressLint("MissingPermission")
    fun getMyLastLocation() {
        var longitude=0.0;var latitude=0.0
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                val newLocation=LocationModel(location!!.longitude,location!!.latitude)
                myLastLocation=newLocation
            }
    }
    fun findRoute(){
        val url=getRouteUrl(myLastLocation!!)
        Log.d("link",url)
        GetRoute(url).execute()
        Log.d("droga","findRoute")

    }

    fun showDistance(){
        distanceTextView.text="Odległość: "+distance.toString()+"m"
        priceTextView.text="Cena: "+getPrice().toString()+"zł"
        Log.d("droga","showDistance")
    }
    private fun getRouteUrl(lastLocation:LocationModel):String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${lastLocation.latitude},${lastLocation.longitude}&destination=${targetLocation?.latitude},${targetLocation?.longitude}&mode=transit&key=AIzaSyAAfIfjV2D8akbv2jCyPoaAfSKsD85TepQ"
    }





    inner class GetRoute(val url: String) : AsyncTask<Void, Void, ForReturn>() {


        override fun doInBackground(vararg p0: Void?): ForReturn {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            var distance=0
            var polyline=""
            try {
                val resObj = Gson().fromJson(data, GoogleDirections::class.java)
                distance=resObj.routes.get(0).legs.get(0).distance.value
                polyline=resObj.routes.get(0).overview_polyline.points

            } catch (e: Exception) {
                e.printStackTrace()
            }
            var distanceAndPoly=ForReturn(distance,polyline)
            return distanceAndPoly
        }

        override fun onPostExecute(distanceTemp:ForReturn) {
            Log.d("droga",distanceTemp.toString())
            if(distanceTemp!=null) {
                Log.d("dystans",distanceTemp.distance.toString())

                distance=distanceTemp.distance
                decodedPoly=distanceTemp.decodedPolyline
                showDistance()
                sendToMapButton.visibility=View.VISIBLE
            }
        }
    }
}
class ForReturn(val distance:Int,val decodedPolyline:String){
    constructor():this(0,"")
}