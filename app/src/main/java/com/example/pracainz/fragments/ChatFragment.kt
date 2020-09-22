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
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupieViewHolder

import com.example.pracainz.R
import com.example.pracainz.models.ChatMessage
import com.example.pracainz.models.OrdersInProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_item.view.*
import kotlinx.android.synthetic.main.chat_to_item.view.*
import kotlinx.android.synthetic.main.chat_to_item.view.messageTextView
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
        loadMessages()
        alreadyAdded=true
        val chatButton=root!!.findViewById(R.id.buttonChat) as Button
        val chatEditText=root!!.findViewById(R.id.editTextChat) as EditText
        chatButton.setOnClickListener {

            val ref=FirebaseDatabase.getInstance().getReference("/OrdersInProgress/"+messagesRef+"/messages").push()
            val message = ChatMessage(uid!!,toId!!,editTextChat.text.toString(),System.currentTimeMillis()/1000)
            ref.setValue(message)
        }

        return root
    }

        fun loadMessages(){

        val ref=FirebaseDatabase.getInstance().getReference("/OrdersInProgress")

        ref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (alreadyAdded == true) {
                    snapshot.children.forEach {
                        val orderinprogress = it.getValue(OrdersInProgress::class.java)
                        if (orderinprogress!!.driver == uid) {
                            toId = orderinprogress.user
                            messagesRef = it.key
                            fetchMessages()
                            Log.d("ilerazy", "jeden")
                            alreadyAdded=false
                        } else if (orderinprogress!!.user == uid) {
                            toId = orderinprogress.driver
                            messagesRef = it.key
                            fetchMessages()
                            Log.d("ilerazy", "dwa")
                            alreadyAdded=false
                        }
                    }
                }
            }

        })

    }
    fun fetchMessages(){
        val chatRecyclerView=root!!.findViewById(R.id.chatRecyclerView) as RecyclerView
        chatRecyclerView.adapter=adapter
        val ref=FirebaseDatabase.getInstance().getReference("/OrdersInProgress/"+messagesRef+"/messages")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatMessage::class.java)
                if(chatMessage!=null){
                    Log.d("ilerazy","trzy")
                    if(chatMessage.fromId==uid)
                    adapter.add(ChatFromItem(chatMessage))
                    else
                        adapter.add(ChatToItem(chatMessage))


                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

    }


}
class ChatFromItem(val chatMessage:ChatMessage):Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.messageTextView.setText(chatMessage.messageText)
        viewHolder.itemView.dateTextView.setText(getDateTimeFromEpocLongOfSeconds(chatMessage.timestamp))
    }
    private fun getDateTimeFromEpocLongOfSeconds(epoc: Long): String? {
        try {
            val netDate = Date(epoc * 1000)
            return netDate.toString()
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
        viewHolder.itemView.messageTextView.setText(chatMessage.messageText)
        viewHolder.itemView.dateTextViewT.setText(getDateTimeFromEpocLongOfSeconds(chatMessage.timestamp))
    }
    private fun getDateTimeFromEpocLongOfSeconds(epoc: Long): String? {
        try {
            val netDate = Date(epoc * 1000)
            return netDate.toString()
        } catch (e: Exception) {
            return e.toString()
        }
    }
}