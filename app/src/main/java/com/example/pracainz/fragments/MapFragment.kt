package com.example.pracainz.fragments

import android.annotation.SuppressLint
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

import com.example.pracainz.R
import com.example.pracainz.activities.DriveActivity
import com.example.pracainz.models.LocationModel
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_maps.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var uid:String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        val mapview=root!!.findViewById<MapView>(R.id.map)
        mapview.onCreate(savedInstanceState)
        mapview.onResume()
        mapview.getMapAsync(this)
        uid= FirebaseAuth.getInstance().uid?: ""
        getLocation()
        return root
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
        }
        val rzeszow = LatLng(50.032369, 22.000550)




        mMap.moveCamera(CameraUpdateFactory.newLatLng(rzeszow))
    }
    @SuppressLint("MissingPermission")
    private fun getLocation(){
        var locationGps : Location?=null
        var locationNetwork : Location?=null
        var hasGps=false
        var hasNetwork=false
        val ref= FirebaseDatabase.getInstance().getReference("/AvailableDrivers")
        val locationmanager=activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps=locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork=locationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(hasGps||hasNetwork){
            if(hasGps){
                locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0F,object: LocationListener {
                    override fun onLocationChanged(location: Location?) {

                        if (location != null) {
                            //val locationModel= LocationModel(locationGps!!.longitude,locationGps!!.latitude)
                            //ref.setValue(locationModel).addOnSuccessListener {
                            var geofire=GeoFire(ref)
                                geofire.setLocation(uid, GeoLocation(locationGps!!.longitude, locationGps!!.latitude))

                            Log.d("CodeAndroidLocation","GPS Latitude:"+locationGps!!.latitude)
                            Log.d("CodeAndroidLocation","GPS Longitude:"+locationGps!!.longitude)
                        }
                    }

                    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(p0: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderDisabled(p0: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })
                val localGpsLocation=locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(localGpsLocation!=null){
                    locationGps=localGpsLocation
                }
                if(hasNetwork){
                    Log.d("CodeAndroidLocation","hasGps")
                    locationmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,0F,object:
                        LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if(location!=null){
                                if(activity is DriveActivity) {

                                    var geofire = GeoFire(ref)
                                    geofire.setLocation(
                                        uid,
                                        GeoLocation(locationNetwork!!.longitude, locationNetwork!!.latitude),
                                        GeoFire.CompletionListener { key, error ->

                                        })
                                }

                                Log.d("CodeAndroidLocation","Network Latitude:"+locationNetwork!!.latitude)
                                Log.d("CodeAndroidLocation","Network Latitude:"+locationNetwork!!.longitude)
                            }
                        }

                        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                        }

                        override fun onProviderEnabled(p0: String?) {
                        }

                        override fun onProviderDisabled(p0: String?) {
                        }

                    })
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
            }
        }
        else{
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }
}
