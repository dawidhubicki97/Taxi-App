package com.example.pracainz.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.pracainz.R
import com.example.pracainz.fragments.ChatFragment
import com.example.pracainz.fragments.MapFragment
import com.example.pracainz.fragments.OrdersFragment
import com.example.pracainz.fragments.RouteFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_maps.*




class MapsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private var drawerlayout: DrawerLayout?=null
    private var toolbar: Toolbar?=null
    private var toggle: ActionBarDrawerToggle?=null
    private var fragment:Fragment?=null
    private lateinit var uid:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        drawerlayout = findViewById(R.id.drawer)
        toolbar = findViewById(R.id.toolbar)
        uid= FirebaseAuth.getInstance().uid?: ""
        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        toggle= ActionBarDrawerToggle(this,drawerlayout,toolbar,R.string.drawerOpen,R.string.drawerClose)
        drawerlayout!!.addDrawerListener(toggle!!)
        toggle!!.syncState()
        fragment=supportFragmentManager.findFragmentByTag("myfragment")
        navigationView.setNavigationItemSelectedListener(this)
        val ref= FirebaseDatabase.getInstance().getReference(".info/connected")
        ref.addValueEventListener(object:ValueEventListener{
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
        if(savedInstanceState==null)
            replaceFragment(MapFragment())
    }

    override fun onBackPressed() {

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
                replaceFragment(RouteFragment())
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

    fun logout(){
        var instance= FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun replaceFragment(fragment: Fragment, routeid:Int?=null) {
        this.fragment=fragment
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.popBackStack()
        if(routeid!=null){
            var bundle= Bundle()
            bundle.putInt("routeid",routeid)
            fragment.arguments=bundle

        }
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }

}
