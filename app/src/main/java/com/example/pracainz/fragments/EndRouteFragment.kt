package com.example.pracainz.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar

import com.example.pracainz.R
import com.google.android.gms.maps.MapView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_end_route.*

class EndRouteFragment : Fragment() {

    private var root:View?=null
    private lateinit var myDriver:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myDriver=arguments!!.getString("myDriver")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root=inflater.inflate(R.layout.fragment_end_route, container, false)
        val ratingBar=root!!.findViewById<RatingBar>(R.id.ratingBar)
        ratingBar.setOnRatingBarChangeListener(object:RatingBar.OnRatingBarChangeListener{
            override fun onRatingChanged(p0: RatingBar?, p1: Float, p2: Boolean) {
                Log.d("thisrating",p1.toString())
                val ref= FirebaseDatabase.getInstance().getReference("/users/"+myDriver).child("orders").orderByKey().limitToLast(1)
                ref.addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            val orderKey=it.key
                        val secondRef = FirebaseDatabase.getInstance()
                            .getReference("/users/" + myDriver + "/orders/" + orderKey + "/rating")
                        secondRef.setValue(p1)
                            val fragmentMap=MapFragment()
                            activity!!.supportFragmentManager.beginTransaction().replace(R.id.container, fragmentMap).commit()
                    }
                    }

                })
            }

        })
        return root
    }


}
