package com.example.pracainz.fragments


import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupieViewHolder

import com.example.pracainz.R
import com.example.pracainz.models.ChatMessage
import com.example.pracainz.models.OrdersInProgress
import com.example.pracainz.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_item.view.*
import kotlinx.android.synthetic.main.chat_to_item.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import java.util.*


class ChatFragment : Fragment() {
    private var messagesRef:String?=null
    private var root:View?=null
    private var toId:String?=null
    private var alreadyAdded:Boolean?=null
    val uid= FirebaseAuth.getInstance().uid
    val adapter=GroupAdapter<GroupieViewHolder>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root=inflater.inflate(R.layout.fragment_chat, container, false)
        alreadyAdded=true
        loadMessages()
        val chatButton=root!!.findViewById(R.id.buttonChat) as Button
        val chatEditText=root!!.findViewById(R.id.editTextChat) as EditText
        chatButton.setOnClickListener {

            val ref=FirebaseDatabase.getInstance().getReference("/OrdersInProgress/"+messagesRef+"/messages").push()
            val message = ChatMessage(uid!!,toId!!,editTextChat.text.toString(),System.currentTimeMillis()/1000)
            ref.setValue(message)
            chatEditText.text.clear()
        }

        return root
    }

        fun loadMessages(){
            val chatButton=root!!.findViewById(R.id.buttonChat) as Button
            val chatEditText=root!!.findViewById(R.id.editTextChat) as EditText
            val personTextView=root!!.findViewById(R.id.chatPersonTextView) as TextView
            val chatRecyclerView=root!!.findViewById(R.id.chatRecyclerView) as RecyclerView
            val warningTextView=root!!.findViewById(R.id.warningTextView) as TextView

        val ref=FirebaseDatabase.getInstance().getReference("/OrdersInProgress")

        ref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (alreadyAdded == true) {
                    snapshot.children.forEach {
                        val orderinprogress = it.getValue(OrdersInProgress::class.java)
                        if (orderinprogress!!.driver == uid) {
                            chatEditText.visibility=View.VISIBLE
                            chatButton.visibility=View.VISIBLE
                            toId = orderinprogress.user
                            messagesRef = it.key
                            fetchMessages()
                            showPerson()
                            Log.d("ilerazy", "jeden")
                            alreadyAdded=false
                        } else if (orderinprogress!!.user == uid) {
                            chatEditText.visibility=View.VISIBLE
                            chatButton.visibility=View.VISIBLE
                            toId = orderinprogress.driver
                            messagesRef = it.key
                            fetchMessages()
                            showPerson()
                            Log.d("ilerazy", "dwa")
                            alreadyAdded=false
                        }
                    }
                }
                if(alreadyAdded==true){

                    chatButton.visibility=View.INVISIBLE
                    chatEditText.visibility=View.INVISIBLE
                    personTextView.visibility=View.INVISIBLE
                    chatRecyclerView.visibility=View.INVISIBLE
                    warningTextView.visibility=View.VISIBLE
                }
            }

        })

    }

    fun showPerson(){
        val personTextView=root!!.findViewById(R.id.chatPersonTextView) as TextView
        val ref=FirebaseDatabase.getInstance().getReference("/users/"+toId)
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userfrombase = snapshot.getValue(User::class.java)
                personTextView.text=userfrombase!!.username+" Telefon: "+userfrombase.phone
            }

        })
    }

    fun fetchMessages(){
        val chatRecyclerView=root!!.findViewById(R.id.chatRecyclerView) as RecyclerView
        chatRecyclerView.adapter=adapter
        val ref=FirebaseDatabase.getInstance().getReference("/OrdersInProgress/"+messagesRef+"/messages")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatMessage::class.java)
                if(chatMessage!=null){
                    Log.d("ilerazy","trzy")
                    if(chatMessage.fromId==uid)
                    adapter.add(ChatToItem(chatMessage))
                    else
                        adapter.add(ChatFromItem(chatMessage))


                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

        })

    }


}
class ChatFromItem(val chatMessage:ChatMessage):Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.text_message_body.setText(chatMessage.messageText)
        viewHolder.itemView.text_message_time.setText(getDateTimeFromEpocLongOfSeconds(chatMessage.timestamp))
    }
    private fun getDateTimeFromEpocLongOfSeconds(epoc: Long): String? {
        try {
            val netDate = Date(epoc * 1000)
            return netDate.hours.toString()+":"+netDate.minutes+"."+netDate.seconds
        } catch (e: Exception) {
            return e.toString()
        }
    }
}
class ChatToItem(val chatMessage:ChatMessage):Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.text_message_bodyy.setText(chatMessage.messageText)
        viewHolder.itemView.text_message_timee.setText(getDateTimeFromEpocLongOfSeconds(chatMessage.timestamp))
    }
    private fun getDateTimeFromEpocLongOfSeconds(epoc: Long): String? {
        try {
            val netDate = Date(epoc * 1000)
            return netDate.hours.toString()+":"+netDate.minutes+"."+netDate.seconds
        } catch (e: Exception) {
            return e.toString()
        }
    }
}