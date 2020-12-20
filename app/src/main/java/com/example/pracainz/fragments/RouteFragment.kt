package com.example.pracainz.fragments


import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView

import com.example.pracainz.R
import com.example.pracainz.models.*
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
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
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*


class RouteFragment : Fragment() {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var distance:Int?=null
    private var rating:Double?=null
    private var decodedPoly:String?=null
    private var targetLocation:LatLng?=null
    private var targetName:String?=null
    private var myLastLocation:LocationModel?=null
    private var traffic:String?=null
    private var autocompleteFragment:AutocompleteSupportFragment ?=null
    private var root:View?=null
    private var alreadyHaveOrder=false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance=true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        getMyLastLocation()
        listenToOrders()
        root=inflater.inflate(R.layout.fragment_route, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as? AutocompleteSupportFragment
        autocompleteFragment?.view!!.visibility=View.INVISIBLE
        sendToMapButton.setOnClickListener{
            Log.d("droga","sendToMapButton")
            val uid= FirebaseAuth.getInstance().uid
            val ref= FirebaseDatabase.getInstance().getReference("/OrderRequests")
            var geofire= GeoFire(ref)
            geofire.setLocation(uid, GeoLocation(myLastLocation!!.latitude, myLastLocation!!.longitude),GeoFire.CompletionListener { key, error ->

            })
            val secondref=FirebaseDatabase.getInstance().getReference("/OrderRequestsTarget")
            var secondgeofire=GeoFire(secondref)
            secondgeofire.setLocation(uid,GeoLocation(targetLocation!!.latitude,targetLocation!!.longitude),GeoFire.CompletionListener { key, error ->

            })
            val thirdref=FirebaseDatabase.getInstance().getReference("/OrderRequestsTarget/"+uid+"/name")
            thirdref.setValue(targetName)
            val fourthref=FirebaseDatabase.getInstance().getReference("/OrderData/"+uid)
            val orderData=OrderData(getPrice()!!,distance!!)
            fourthref.setValue(orderData)
            progressBarRoute.visibility=View.VISIBLE
            sendToMapButton.visibility=View.INVISIBLE
            priceTextView.visibility=View.INVISIBLE
            distanceTextView.visibility=View.INVISIBLE
            trafficTextView.visibility=View.INVISIBLE
            infoTextView.visibility=View.VISIBLE
            cancelOrderButton.visibility=View.VISIBLE
            autocompleteFragment?.view!!.visibility=View.INVISIBLE

        }
        cancelOrderButton.setOnClickListener {
            val uid= FirebaseAuth.getInstance().uid
            val ref= FirebaseDatabase.getInstance().getReference("/OrderRequests/"+uid)
            val secondref=FirebaseDatabase.getInstance().getReference("/OrderRequestsTarget/"+uid)
            val thirdref=FirebaseDatabase.getInstance().getReference("/OrderData/"+uid)
            ref.removeValue()
            secondref.removeValue()
            thirdref.removeValue()
            progressBarRoute.visibility=View.INVISIBLE
            sendToMapButton.visibility=View.INVISIBLE
            priceTextView.visibility=View.VISIBLE
            distanceTextView.visibility=View.VISIBLE
            trafficTextView.visibility=View.VISIBLE
            infoTextView.visibility=View.INVISIBLE
            cancelOrderButton.visibility=View.INVISIBLE
            autocompleteFragment?.view!!.visibility=View.VISIBLE
            distance=null
            traffic=null
            distanceTextView.text=""
            priceTextView.text=""
            trafficTextView.text=""
        }
        Places.initialize(activity!!.applicationContext, "AIzaSyAAfIfjV2D8akbv2jCyPoaAfSKsD85TepQ")
        val placesClient = Places.createClient(activity!!.applicationContext)

        autocompleteFragment!!.setHint("Kliknij tutaj i podaj cel podróży")
        autocompleteFragment!!.view!!.setBackground(resources.getDrawable(R.drawable.rounded_login_register_text))
        autocompleteFragment!!.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG))
        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: ${place.name}, ${place.latLng.toString()}")
                targetLocation=place.latLng
                targetName=place.name
                findRoute()
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })
        val ref=FirebaseDatabase.getInstance().getReference("/rating")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                rating= snapshot.getValue(Double::class.java)!!
            }

        })
    }
    fun getPrice(): Double? {
        val value=(distance!!*3.0*rating!!)/1000
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        Log.d("liczbeczka",df.format(value))
        return df.format(value).replace(",", ".").toDouble()
    }
    fun listenToOrders(){
        Log.d("notestujese","start")
        val uid= FirebaseAuth.getInstance().uid
        val ref= FirebaseDatabase.getInstance().getReference("/OrdersInProgress")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val orderinprogress = it.getValue(OrdersInProgress::class.java)
                    Log.d("notestujese", orderinprogress!!.user)
                    if (orderinprogress!!.user == uid) {
                        val url=getSecondRouteUrl(orderinprogress)
                        alreadyHaveOrder=true
                        GetRoute(url).execute()
                        Log.d("notestujese", orderinprogress.user)
                    }
                }
                if(alreadyHaveOrder==false)
                autocompleteFragment?.view?.visibility=View.VISIBLE
            }

        })
        val secondRef=FirebaseDatabase.getInstance().getReference("/OrderData")
        secondRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if(it.key==uid){
                        //tutaj byl blad
                        progressBarRoute?.visibility=View.VISIBLE
                        sendToMapButton?.visibility=View.INVISIBLE
                        priceTextView?.visibility=View.INVISIBLE
                        distanceTextView?.visibility=View.INVISIBLE
                        trafficTextView?.visibility=View.INVISIBLE
                        infoTextView?.visibility=View.VISIBLE
                        cancelOrderButton?.visibility=View.VISIBLE
                        //autocomplete_fragment.view!!.visibility=View.INVISIBLE
                    }
                }
            }

        })
    }
    fun sendToMap(){
        val fragmentMap=MapFragment()
        var bundle= Bundle()
        bundle.putInt("distance",distance!!)
        bundle.putString("decodedPoly",decodedPoly!!)
        fragmentMap.arguments=bundle
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container, fragmentMap)?.commit()
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
                Log.d("wymiary",(myLastLocation!!.latitude+0.08).toString())
                Log.d("wymiary",(myLastLocation!!.longitude+0.08).toString())
                Log.d("wymiary",(myLastLocation!!.latitude-0.08).toString())
                Log.d("wymiary",(myLastLocation!!.longitude-0.08).toString())
                val northEast = LatLng(myLastLocation!!.latitude+0.08, myLastLocation!!.longitude+0.08)
                val southWest = LatLng(myLastLocation!!.latitude-0.08, myLastLocation!!.longitude-0.08)
                val border=RectangularBounds.newInstance(southWest, northEast)
                autocompleteFragment!!.setLocationRestriction(border)
            }

    }
    fun findRoute(){
        val url=getRouteUrl(myLastLocation!!)
        Log.d("notestujese",url)
        GetRoute(url).execute()
        Log.d("droga","findRoute")

    }

    fun showDistance(){
        distanceTextView.text="Odległość: "+distance.toString()+"m"
        priceTextView.text="Cena: "+getPrice().toString()+"zł"
        trafficTextView.text=traffic
        Log.d("droga","showDistance")
    }
    private fun getRouteUrl(lastLocation:LocationModel):String{
        Log.d("lala","https://maps.googleapis.com/maps/api/directions/json?origin=${lastLocation.latitude},${lastLocation.longitude}&destination=${targetLocation?.latitude},${targetLocation?.longitude}&departure_time=now&key=AIzaSyAAfIfjV2D8akbv2jCyPoaAfSKsD85TepQ")
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${lastLocation.latitude},${lastLocation.longitude}&destination=${targetLocation?.latitude},${targetLocation?.longitude}&departure_time=now&key=AIzaSyAAfIfjV2D8akbv2jCyPoaAfSKsD85TepQ"
    }
    private fun getSecondRouteUrl(orderinprogress:OrdersInProgress):String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${orderinprogress.startlat},${orderinprogress.startlng}&destination=${orderinprogress?.targetlat},${orderinprogress?.targetlng}&departure_time=now&key=AIzaSyAAfIfjV2D8akbv2jCyPoaAfSKsD85TepQ"
    }




    inner class GetRoute(val url: String) : AsyncTask<Void, Void, ForReturn>() {


        override fun doInBackground(vararg p0: Void?): ForReturn {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            var distance=0
            var durationInTraffic=""
            var polyline=""
            try {
                val resObj = Gson().fromJson(data, GoogleDirections::class.java)
                distance=resObj.routes.get(0).legs.get(0).distance.value
                polyline=resObj.routes.get(0).overview_polyline.points
                durationInTraffic=resObj.routes.get(0).legs.get(0).duration_in_traffic.text

            } catch (e: Exception) {
                e.printStackTrace()
            }
            var distanceAndPoly=ForReturn(distance,polyline,durationInTraffic)
            return distanceAndPoly
        }

        override fun onPostExecute(distanceTemp:ForReturn) {
            Log.d("droga",distanceTemp.toString())
            if(distanceTemp!=null) {

                distance=distanceTemp.distance
                decodedPoly=distanceTemp.decodedPolyline
                traffic="Czas w korku: "+distanceTemp.traffic
                if(alreadyHaveOrder==false) {
                    showDistance()
                    sendToMapButton.visibility = View.VISIBLE
                }
                else{

                    sendToMap()
                }
            }
        }
    }
}
class ForReturn(val distance:Int,val decodedPolyline:String,val traffic:String){
    constructor():this(0,"","")
}