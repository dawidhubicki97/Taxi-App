package com.example.pracainz.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.example.pracainz.R
import com.example.pracainz.activities.DriveActivity
import com.example.pracainz.activities.MapsActivity
import com.example.pracainz.models.ChatMessage
import com.example.pracainz.models.OrdersInProgress
import com.example.pracainz.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_item.view.*
import kotlinx.android.synthetic.main.chat_to_item.view.*
import kotlinx.android.synthetic.main.fragment_end_route.view.*
import kotlinx.android.synthetic.main.fragment_orders.view.*
import kotlinx.android.synthetic.main.order_item.view.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class OrdersFragment : Fragment() {
    private var root:View?=null
    private var rating:Double?=null
    private var earnings:Double?=null
    private var ratingCounter=0
    val adapter=GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root=inflater.inflate(R.layout.fragment_orders, container, false)
        listenToOrders()
        return root
    }

    fun getPrice(): Double? {
        val value=earnings
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        Log.d("liczbeczka",df.format(value))
        return df.format(value).replace(",", ".").toDouble()
    }

    fun getRating(value:Double): Double? {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(value).replace(",", ".").toDouble()
    }

    fun listenToOrders(){
        earnings=0.0
        rating=0.0
        val uid= FirebaseAuth.getInstance().uid
        val ref= FirebaseDatabase.getInstance().getReference("users/"+uid+"/orders")
        val orderRecycler=root!!.findViewById(R.id.ordersRecyclerView) as RecyclerView
        val earningsTextView=root!!.findViewById(R.id.earningsTextView) as TextView
        val orderRatingTextView=root!!.ratingOrdersTextView
        orderRecycler.adapter=adapter
        ref.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val order = it.getValue(OrdersInProgress::class.java)
                    Log.d("orderinio",order!!.distance.toString())
                    adapter.add(OrderItem(order!!))
                    earnings=earnings!!+ order.price
                    Log.d("zarobki",earnings.toString())
                    if(order.rating!=null && order.rating!=0.0) {
                        rating = rating!!+order.rating
                        ratingCounter++
                        Log.d("rejting",rating.toString())
                    }
                }
                if (activity is MapsActivity) {

                    adapter.setOnItemClickListener { item, view ->
                        val thisItem=item as OrderItem
                        if(thisItem.order.rating==0.0) {
                            val fragmentRate = RateFragment()
                            var bundle = Bundle()
                            bundle.putLong("timestamp", thisItem.order.timestamp)
                            fragmentRate.arguments = bundle
                            activity!!.supportFragmentManager.beginTransaction().replace(R.id.container, fragmentRate)
                                .commit()
                        }
                    }
                }
                if(ratingCounter!=0)
                    if (activity is DriveActivity) {
                        val temp=rating!! / ratingCounter
                        orderRatingTextView.text = "Średnia ocena: " + getRating(temp).toString()
                    }
                if (activity is DriveActivity) {
                    earningsTextView.text = "Laczne Zarobki:" + getPrice() + "zł"
                }
            }

        })


    }




}
class OrderItem(val order: OrdersInProgress): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.order_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.orderTextView.text="Stawka: "+order.price.toString()+"zł"
        viewHolder.itemView.priceDistanceTextView.text="Dystans: "+order.distance.toString()+"m"

        val sdf = SimpleDateFormat("dd/MM/yy hh:mm")
        val netDate = Date(order.timestamp*1000)
        val date =sdf.format(netDate)
        viewHolder.itemView.textViewDate.text=date.toString()
        if(order.rating==0.0){
            Picasso.get().load(R.drawable.abc_ic_star_half_black_48dp).into( viewHolder.itemView.imageViewOrderRated)

        }
        else{
            viewHolder.itemView.textViewRate.text=order.rating.toString()
            Picasso.get().load(R.drawable.abc_ic_star_black_48dp).into( viewHolder.itemView.imageViewOrderRated)
        }
        val ref= FirebaseDatabase.getInstance().getReference("users/"+order.user)
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user=snapshot.getValue(User::class.java)
                viewHolder.itemView.textViewOrderUser.text="Klient: "+user?.username
            }

        })
        val secondref= FirebaseDatabase.getInstance().getReference("users/"+order.driver)
        secondref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val driver=snapshot.getValue(User::class.java)
                viewHolder.itemView.textViewOrderDriver.text="Kierowca: "+driver?.username
            }

        })


    }

}