package com.example.pracainz.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

import com.example.pracainz.R
import com.example.pracainz.activities.DriveActivity
import com.example.pracainz.activities.MapsActivity
import com.example.pracainz.models.LocationModel
import com.example.pracainz.models.OrdersInProgress
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_maps.*
import java.math.RoundingMode
import java.text.DecimalFormat

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var uid:String
    private var myDriver:String?=null
    private var root:View?=null
    private var driverMaker: Marker?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        root = inflater.inflate(R.layout.fragment_map, container, false)
        val mapview=root!!.findViewById<MapView>(R.id.map)
        mapview.onCreate(savedInstanceState)
        mapview.onResume()
        mapview.getMapAsync(this)
        uid= FirebaseAuth.getInstance().uid?: ""
        getLocation()
        return root
    }
    fun showMyDriver(){
        Log.d("aktiwiti","showdriver")
        val ref= FirebaseDatabase.getInstance().getReference("/OrdersInProgress")
        ref.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val orderinprogress = it.getValue(OrdersInProgress::class.java)
                    Log.d("notestujese", orderinprogress!!.driver)
                    if (orderinprogress!!.user == uid) {
                        myDriver=orderinprogress.driver
                        getMyDriverLocation()
                    }
                }
            }

        })
    }

    fun getMyDriverLocation(){
        val firstref=FirebaseDatabase.getInstance().getReference("/users/"+myDriver+"/lastLocalization")
        val ref= FirebaseDatabase.getInstance().getReference("/users/"+myDriver)
        var geofire=GeoFire(ref)
        firstref.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                geofire.getLocation("lastLocalization",object:com.firebase.geofire.LocationCallback{

                    override fun onLocationResult(key: String?, location: GeoLocation?) {
                        Log.d("pokaz",key)
                        Log.d("pokaz",location!!.latitude.toString())
                        driverMaker?.remove()
                        driverMaker=mMap.addMarker(MarkerOptions().position(LatLng(location!!.longitude,location.latitude)).title("kierowca").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)))
                    }

                    override fun onCancelled(databaseError: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })
            }

        })


    }
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.isMyLocationEnabled=true
        val distance=arguments?.getInt("distance")
        val routePolylineCoded=arguments?.getString("decodedPoly")
        if(routePolylineCoded!=null){
            val decodedPolyLine=PolyUtil.decode(routePolylineCoded)
            mMap.addPolyline(PolylineOptions().addAll(decodedPolyLine))
            mMap.addMarker(MarkerOptions().position(decodedPolyLine.first()).title("Start").icon(BitmapDescriptorFactory.fromResource(R.drawable.starticon)))
            mMap.addMarker(MarkerOptions().position(decodedPolyLine.last()).title("Koniec").icon(BitmapDescriptorFactory.fromResource(R.drawable.finishicon)))
            val buttonEnd=root!!.findViewById(R.id.endRouteButton) as Button
            buttonEnd.visibility=View.VISIBLE
                /*
                val ref= FirebaseDatabase.getInstance().getReference("/users/"+uid+"/lastLocalization")
                var geofire= GeoFire(ref)
                var geoQuery=geofire.queryAtLocation(GeoLocation(decodedPolyLine.last().latitude,decodedPolyLine.last().longitude),0.2)
                geoQuery.addGeoQueryEventListener(object:GeoQueryEventListener{
                    override fun onGeoQueryReady() {
                        Log.d("ideczynieide","readi")
                    }

                    override fun onKeyEntered(key: String?, location: GeoLocation?) {
                        Log.d("ideczynieide",key)
                    }

                    override fun onKeyMoved(key: String?, location: GeoLocation?) {

                    }

                    override fun onKeyExited(key: String?) {

                    }

                    override fun onGeoQueryError(error: DatabaseError?) {

                    }

                })

                Log.d("aktiwiti","driver")
*/
                buttonEnd.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Potwierdź")
                    builder.setMessage("Czy aby napewno chcesz zakończyć przejazd?")

                    builder.setPositiveButton("Tak") { dialog, which ->
                        Log.d("cotusie","raz")
                        val ref= FirebaseDatabase.getInstance().getReference("/OrdersInProgress")
                        ref.addValueEventListener(object: ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {

                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.d("cotusie","dwa")
                                ref.removeEventListener(this)
                                snapshot.children.forEach {
                                    val orderinprogress = it.getValue(OrdersInProgress::class.java)
                                    if (orderinprogress!!.driver == uid || orderinprogress.user == uid  ) {
                                        it.ref.removeValue()

                                        val reffourth=FirebaseDatabase.getInstance().getReference("/users/"+orderinprogress.driver+"/orders")
                                        reffourth.addListenerForSingleValueEvent(object:ValueEventListener{
                                            override fun onCancelled(error: DatabaseError) {

                                            }

                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                Log.d("cotusie","trzy")
                                                var rating=0.0
                                                var i=0
                                                snapshot.children.forEach {
                                                    i++
                                                    val orderFromHistory = it.getValue(OrdersInProgress::class.java)
                                                    rating=orderFromHistory!!.rating+rating
                                                }
                                                rating=rating/i
                                                val refsecond=FirebaseDatabase.getInstance().getReference("/users/"+orderinprogress.user+"/orders").push()
                                                refsecond.setValue(orderinprogress)
                                                val refthird=FirebaseDatabase.getInstance().getReference("/users/"+orderinprogress.driver+"/orders").push()
                                                if(rating>4.0) {
                                                    val newOrder=OrdersInProgress(orderinprogress.driver,orderinprogress.user,orderinprogress.startlat,orderinprogress.startlng,orderinprogress.targetlat,orderinprogress.targetlng,convertDouble(orderinprogress.price*0.9),orderinprogress.distance,orderinprogress.rating,orderinprogress.timestamp)
                                                    refthird.setValue(newOrder)
                                                }
                                                if(rating>3.0 && rating <=4) {
                                                    val newOrder=OrdersInProgress(orderinprogress.driver,orderinprogress.user,orderinprogress.startlat,orderinprogress.startlng,orderinprogress.targetlat,orderinprogress.targetlng,convertDouble(orderinprogress.price*0.8),orderinprogress.distance,orderinprogress.rating,orderinprogress.timestamp)
                                                    refthird.setValue(newOrder)
                                                }
                                                if(rating <=3) {
                                                    val newOrder=OrdersInProgress(orderinprogress.driver,orderinprogress.user,orderinprogress.startlat,orderinprogress.startlng,orderinprogress.targetlat,orderinprogress.targetlng,convertDouble(orderinprogress.price*0.7),orderinprogress.distance,orderinprogress.rating,orderinprogress.timestamp)
                                                    refthird.setValue(newOrder)
                                                }
                                                else{
                                                    val newOrder=OrdersInProgress(orderinprogress.driver,orderinprogress.user,orderinprogress.startlat,orderinprogress.startlng,orderinprogress.targetlat,orderinprogress.targetlng,convertDouble(orderinprogress.price*0.8),orderinprogress.distance,orderinprogress.rating,orderinprogress.timestamp)
                                                    refthird.setValue(newOrder)
                                                }
                                                var reffifth= FirebaseDatabase.getInstance().getReference("/users/"+orderinprogress.driver+"/status")
                                                reffifth.setValue(false)
                                                buttonEnd.visibility=View.INVISIBLE
                                                mMap.clear()
                                                buttonEnd.setOnClickListener(null)
                                                reffourth.removeEventListener(this)
                                                Log.d("cotusie","cztery")
                                            }

                                        })
                                    }
                                }
                            }

                        })
                    }

                    builder.setNegativeButton("nie") { dialog, which ->

                    }
                    builder.show()


                }


            if(activity is MapsActivity )
            {
                Log.d("aktiwiti","driver")
                if(routePolylineCoded!=null)
                    showMyDriver()
            }

            val ref=FirebaseDatabase.getInstance().getReference("/OrdersInProgress")

            ref.addValueEventListener(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var isInBase=false
                        snapshot.children.forEach {
                            val orderinprogress = it.getValue(OrdersInProgress::class.java)
                            if (orderinprogress!!.driver == uid) {
                                isInBase=true
                            } else if (orderinprogress!!.user == uid) {
                                isInBase=true
                            }
                        }
                    if(isInBase==false){
                        mMap.clear()
                        decodedPolyLine.clear()
                        if(activity is MapsActivity) {
                            var bundle = Bundle()
                            bundle.putString("myDriver", myDriver!!)
                            val fragmentEnd = EndRouteFragment()
                            fragmentEnd.arguments = bundle
                            activity!!.supportFragmentManager.beginTransaction().replace(R.id.container, fragmentEnd)
                                .commit()
                        }
                    }
                }

            })

        }
        val rzeszow = LatLng(50.032369, 22.000550)




        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rzeszow, 15.0f))
    }
    fun convertDouble(value:Double):Double{
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    Log.d("liczbeczka",df.format(value))
    return df.format(value).replace(",", ".").toDouble()
    }
    @SuppressLint("MissingPermission")
    private fun getLocation(){
        var locationGps : Location?=null
        var locationNetwork : Location?=null
        var hasGps=false
        var hasNetwork=false
        val ref= FirebaseDatabase.getInstance().getReference("/AvailableDrivers")
        val refsecond=FirebaseDatabase.getInstance().getReference("/users/"+uid)
        val locationmanager=activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps=locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork=locationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(hasGps||hasNetwork){
            if(hasGps) { locationmanager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0F,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if (location != null) {
                                locationGps = location
                                if (activity is DriveActivity) {
                                    var geofiresecond = GeoFire(refsecond)
                                    geofiresecond.setLocation(
                                        "lastLocalization",
                                        GeoLocation(locationGps!!.longitude, locationGps!!.latitude),
                                        GeoFire.CompletionListener { key, error ->

                                        })
                                }

                                Log.d("CodeAndroidLocation", "GPS Latitude:" + locationGps!!.latitude)
                                Log.d("CodeAndroidLocation", "GPS Latitude:" + locationGps!!.longitude)
                            }
                        }

                        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

                        }

                        override fun onProviderEnabled(p0: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onProviderDisabled(p0: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                    })
            }
                val localGpsLocation=locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(localGpsLocation!=null){
                    locationGps=localGpsLocation
                }
                if(hasNetwork) {
                    locationmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object :
                        LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if (location != null) {
                                locationNetwork=location
                                if (activity is DriveActivity) {
                                    var geofiresecond = GeoFire(refsecond)
                                    geofiresecond.setLocation(
                                        "lastLocalization",
                                        GeoLocation(locationNetwork!!.longitude, locationNetwork!!.latitude),
                                        GeoFire.CompletionListener { key, error ->

                                        })
                                }

                                Log.d("CodeAndroidLocation", "Network Latitude:" + locationNetwork!!.latitude)
                                Log.d("CodeAndroidLocation", "Network Latitude:" + locationNetwork!!.longitude)
                            }
                        }

                        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                        }

                        override fun onProviderEnabled(p0: String?) {
                        }

                        override fun onProviderDisabled(p0: String?) {
                        }

                    })
                }
                    val localNewtorkLocation=locationmanager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if(localNewtorkLocation!=null){
                        locationNetwork=localNewtorkLocation
                    }
                    if(locationGps!=null && locationNetwork!=null){
                        if(locationGps!!.accuracy>locationNetwork!!.accuracy){
                            Log.d("CodeAndroidLocation","Network Latitude:"+locationNetwork!!.latitude)
                            Log.d("CodeAndroidLocation","Network Latitude:"+locationNetwork!!.longitude)

                        }
                        else{
                            Log.d("CodeAndroidLocation","GPS Latitude:"+locationGps!!.latitude)
                            Log.d("CodeAndroidLocation","GPS Latitude:"+locationGps!!.longitude)
                        }
                    }


        }
        else{
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }
}
