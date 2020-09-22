package com.example.pracainz.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.pracainz.R
import com.example.pracainz.adapters.RecyclerAdapter
import com.example.pracainz.models.*
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_driver.*
import kotlinx.android.synthetic.main.fragment_route.*
import kotlinx.android.synthetic.main.route_item.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception

class DriverFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var myLastLocation:LocationModel?=null
    private lateinit var driverAdapter: RecyclerAdapter
    private var distance:Int?=null
    private var decodedPoly:String?=null
    private var root:View?=null
    var adapter=GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        root=inflater.inflate(R.layout.fragment_driver, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        getMyLastLocation()
        listenToOrders()
        return root
    }
    fun sendToMap(){
        val fragmentMap=MapFragment()
        var bundle= Bundle()
        bundle.putInt("distance",distance!!)
        bundle.putString("decodedPoly",decodedPoly!!)
        fragmentMap.arguments=bundle
        activity!!.supportFragmentManager.beginTransaction().replace(R.id.container, fragmentMap).commit()
    }
    fun listenToOrders(){
        Log.d("notestujese","start")
        val uid= FirebaseAuth.getInstance().uid
        val ref= FirebaseDatabase.getInstance().getReference("/OrdersInProgress")
        ref.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val orderinprogress = it.getValue(OrdersInProgress::class.java)
                    Log.d("notestujese", orderinprogress!!.driver)
                    if (orderinprogress!!.driver == uid) {
                        val url=getRouteUrl(orderinprogress)
                        GetRoute(url).execute()
                        Log.d("notestujese", orderinprogress.driver)
                    }
                }
            }

        })
    }
    private fun getRouteUrl(orderinprogress:OrdersInProgress):String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${orderinprogress.startlat},${orderinprogress.startlng}&destination=${orderinprogress?.targetlat},${orderinprogress?.targetlng}&mode=transit&key=AIzaSyAAfIfjV2D8akbv2jCyPoaAfSKsD85TepQ"
    }
    @SuppressLint("MissingPermission")
    fun getMyLastLocation() {
        var longitude=0.0;var latitude=0.0
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                val newLocation= LocationModel(location!!.longitude,location!!.latitude)
                myLastLocation=newLocation
                populateList()

            }
    }
    fun populateList(){
        linearLayoutManager = LinearLayoutManager(context)
        val driverRecycler=root!!.findViewById(R.id.driverRecyclerView) as RecyclerView
        driverRecycler!!.layoutManager = linearLayoutManager
        driverAdapter = RecyclerAdapter()
        val data: ArrayList<AvailableDrive> = ArrayList()
        val ref= FirebaseDatabase.getInstance().getReference("/OrderRequests")
        var geofire= GeoFire(ref)
        var geoQuery=geofire.queryAtLocation(GeoLocation(myLastLocation!!.latitude,myLastLocation!!.longitude),10.0)
        geoQuery.addGeoQueryEventListener(object: GeoQueryEventListener {
            override fun onGeoQueryReady() {
                Log.d("znaleziono","z")
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                activity!!.runOnUiThread {
                    var availabledrive=AvailableDrive(key!!,location!!.latitude,location!!.longitude)
                    data.add(availabledrive)
                    driverAdapter.submitList(data)
                    driverRecycler!!.adapter=driverAdapter
                }
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
                sendToMap()

            }
        }
    }
}



class RouteItem(val availableDrive: AvailableDrive): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.route_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Log.d("znaleziono","doszlotu")
        viewHolder.itemView.routeTextView.setText(availableDrive.user)
    }


}