package com.example.pracainz.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.example.pracainz.R
import com.example.pracainz.models.ChatMessage
import com.example.pracainz.models.OrdersInProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_item.view.*
import kotlinx.android.synthetic.main.chat_to_item.view.*
import kotlinx.android.synthetic.main.chat_to_item.view.messageTextView
import kotlinx.android.synthetic.main.order_item.view.*
import java.util.*


class OrdersFragment : Fragment() {
    private var root:View?=null
    val adapter=GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listenToOrders()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root=inflater.inflate(R.layout.fragment_orders, container, false)
        return root
    }

    fun listenToOrders(){
        val uid= FirebaseAuth.getInstance().uid
        val ref= FirebaseDatabase.getInstance().getReference("users/"+uid+"/orders")
        val driverRecycler=root!!.findViewById(R.id.ordersRecyclerView) as RecyclerView

        ref.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val order = it.getValue(OrdersInProgress::class.java)

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
        viewHolder.itemView.orderTextView.text=order.driver

    }

}