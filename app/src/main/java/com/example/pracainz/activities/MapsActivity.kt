package com.example.pracainz.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.pracainz.R
import com.example.pracainz.models.LocationModel
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var uid:String
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        uid= FirebaseAuth.getInstance().uid?: ""
        getLocation()

    }
    @SuppressLint("MissingPermission")
    private fun getLocation(){
        var locationGps : Location?=null
        var locationNetwork : Location?=null
        var hasGps=false
        var hasNetwork=false
        val ref= FirebaseDatabase.getInstance().getReference("/users/$uid/lastLocalization")
        val locationmanager=this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps=locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork=locationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(hasGps||hasNetwork){
            if(hasGps){
                locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0F,object: LocationListener {
                    override fun onLocationChanged(location: Location?) {

                        if (location != null) {
                            val locationModel= LocationModel(locationGps!!.longitude,locationGps!!.latitude)
                            ref.setValue(locationModel).addOnSuccessListener {

                            }
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
                                val locationModel= LocationModel(locationNetwork!!.longitude,locationNetwork!!.latitude)
                                ref.setValue(locationModel).addOnSuccessListener {

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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled=true
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
