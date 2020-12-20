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
import android.widget.Toast

import com.example.pracainz.R
import com.example.pracainz.models.OrdersInProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RateFragment : Fragment() {
    private var root:View?=null
    private var timestamp:Long?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root=inflater.inflate(R.layout.fragment_rate, container, false)
        val uid= FirebaseAuth.getInstance().uid
        timestamp=arguments!!.getLong("timestamp")
        val ratingBar=root!!.findViewById<RatingBar>(R.id.ratingBarAfter)
        ratingBar.onRatingBarChangeListener = object:RatingBar.OnRatingBarChangeListener{
            override fun onRatingChanged(p0: RatingBar?, p1: Float, p2: Boolean) {
                val ref= FirebaseDatabase.getInstance().getReference("users")
                ref.addValueEventListener(object:ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            val thisUserOrders=it.child("orders")
                            thisUserOrders.children.forEach {
                                val orderThis=it.getValue(OrdersInProgress::class.java)
                                if(orderThis!!.timestamp==timestamp) {
                                    val thisRating = it.child("rating")
                                    thisRating.ref.setValue(p1)

                                }
                            }
                        }
                        val fragmentOrders=OrdersFragment()
                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container, fragmentOrders)?.commit()
                    }

                })

            }

        }
        return root

    }

}
