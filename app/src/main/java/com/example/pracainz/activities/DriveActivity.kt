package com.example.pracainz.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pracainz.R
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.pracainz.fragments.*
import com.example.pracainz.models.OrdersInProgress
import com.firebase.geofire.GeoFire
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_maps.*

class DriveActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private var drawerlayout: DrawerLayout?=null
    private var toolbar: Toolbar?=null
    private var toggle: ActionBarDrawerToggle?=null
    private var fragment:Fragment?=null
    private var firstListen=false
    private val CHANNEL_ID="przyklad"
    private val notificationId=111
    private lateinit var uid:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive)
        createNotificationChannel()
        drawerlayout = findViewById(R.id.drawer)
        toolbar = findViewById(R.id.toolbar)



        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)
        uid= FirebaseAuth.getInstance().uid?: ""
        toggle= ActionBarDrawerToggle(this,drawerlayout,toolbar,R.string.drawerOpen,R.string.drawerClose)
        drawerlayout!!.addDrawerListener(toggle!!)
        toggle!!.syncState()
        fragment=supportFragmentManager.findFragmentByTag("myfragment")
        navigationView.setNavigationItemSelectedListener(this)
        val ref= FirebaseDatabase.getInstance().getReference(".info/connected")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val connected=snapshot.getValue(Boolean::class.java)
                if(connected!!){
                    val con= FirebaseDatabase.getInstance().getReference("users/"+uid+"/isOnline")
                    con.setValue(true)
                    con.onDisconnect().setValue(false)
                }
            }

        })
        listenOrders()
        if(savedInstanceState==null)
            replaceFragment(MapFragment())
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "nazwa"
            val descriptionText = "opis"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        Log.d("klik","cos")
        when(p0.itemId){
            R.id.mapmenuitem ->{
                Log.d("klik","map")
                replaceFragment(MapFragment())
            }
            R.id.newcourse->{
                Log.d("klik","newcourse")
                replaceFragment(DriverFragment())
            }
            R.id.chatmenuitem->{
                replaceFragment(ChatFragment())
            }
            R.id.ordersMenuItem->{
                replaceFragment(OrdersFragment())
            }
            R.id.logoutmenuitem->{
                logout()
            }
        }
        return true
    }

    fun replaceFragment(fragment: Fragment, routeid:Int?=null) {
        this.fragment=fragment
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if(routeid!=null){
            var bundle= Bundle()
            bundle.putInt("routeid",routeid)
            fragment.arguments=bundle

        }
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }

    fun sendNotification(mystatus:Boolean){
            Log.d("droganotyfikacji","sendNotification"+mystatus.toString())
        if(mystatus) {

            var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.starticon)
                .setContentTitle("Status Zamowienia")
                .setContentText("Dodano zamowienie")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        }
        else{
            var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.starticon)
                .setContentTitle("Status Zamowienia")
                .setContentText("Zakonczono zamowienie")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        }
    }





    fun listenOrders(){
        val con= FirebaseDatabase.getInstance().getReference("users/"+uid+"/status")
        con.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("droganotyfikacji","onDataChange"+firstListen.toString())
                if(firstListen==false){
                    firstListen=true
                }
                else{
                    val mystatus=snapshot.getValue(Boolean::class.java)
                    sendNotification(mystatus!!)
                }

            }

        })

    }
    fun logout(){
            var instance=FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
    }


    override fun onStop() {
        val uid= FirebaseAuth.getInstance().uid
       // val ref= FirebaseDatabase.getInstance().getReference("/AvailableDrivers")
       // var geofire= GeoFire(ref)
       // geofire.removeLocation(uid,object:GeoFire.CompletionListener{
        //    override fun onComplete(key: String?, error: DatabaseError?) {

        //    }

       // })
        super.onStop()
    }
}