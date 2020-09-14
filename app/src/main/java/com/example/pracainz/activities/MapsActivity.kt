package com.example.pracainz.activities


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
import com.example.pracainz.fragments.RouteFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private var drawerlayout: DrawerLayout?=null
    private var toolbar: Toolbar?=null
    private var toggle: ActionBarDrawerToggle?=null
    private var fragment:Fragment?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        drawerlayout = findViewById(R.id.drawer)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        toggle= ActionBarDrawerToggle(this,drawerlayout,toolbar,R.string.drawerOpen,R.string.drawerClose)
        drawerlayout!!.addDrawerListener(toggle!!)
        toggle!!.syncState()
        fragment=supportFragmentManager.findFragmentByTag("myfragment")
        navigationView.setNavigationItemSelectedListener(this)
        Log.d("klik","czy to wogole to")
        if(savedInstanceState==null)
            replaceFragment(MapFragment())
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

}
