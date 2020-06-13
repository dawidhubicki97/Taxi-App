package com.example.pracainz.fragments


import android.annotation.SuppressLint
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
import com.example.pracainz.models.Polyline
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.fragment_route.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.lang.Exception

class RouteFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var distance:Int?=null
    private var decodedPoly:String?=null
    private var targetLocation:LocationModel?=null
    private var myLastLocation:LocationModel?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        getMyLastLocation()
        return inflater.inflate(R.layout.fragment_route, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //confirmLocationButton.setOnClickListener {
       //     findRoute()
       // }

        routeSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                var loc=routeSearchView.query.toString()
                var addressList = listOf<Address>()
                if(loc!=null && !loc.equals("")){
                    val geoCoder=Geocoder(context)
                    try{
                        addressList=geoCoder.getFromLocationName(loc,1)
                    }
                    catch (e: IOException){
                        Log.d("szukanie","nic nie znalazlo")
                    }
                    val target=LocationModel(addressList.get(0).longitude,addressList.get(0).latitude)
                    targetLocation=target
                    findRoute()
                }
             return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })
        sendToMapButton.setOnClickListener{
            Log.d("droga","sendToMapButton")
            val fragmentMap=MapFragment()
            var bundle= Bundle()
            bundle.putInt("distance",distance!!)
            bundle.putString("decodedPoly",decodedPoly!!)
            fragmentMap.arguments=bundle
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.container, fragmentMap).commit()
        }
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
        distanceTextView.text=distance.toString()
        val price= distance?.times(3.0)
        priceTextView.text=price.toString()
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