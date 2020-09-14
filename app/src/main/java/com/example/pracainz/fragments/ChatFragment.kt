package com.example.pracainz.fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupieViewHolder

import com.example.pracainz.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item


class ChatFragment : Fragment() {

    private var root:View?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root=inflater.inflate(R.layout.fragment_chat, container, false)

        configureRecyclerView()
        return root
    }
   fun configureRecyclerView(){
        val adapter=GroupAdapter<GroupieViewHolder>()
       val chatRecyclerView=root!!.findViewById(R.id.chatRecyclerView) as RecyclerView
       adapter.add(ChatItem())
       adapter.add(ChatItem())
       adapter.add(ChatItem())
       adapter.add(ChatItem())
       chatRecyclerView.adapter=adapter
    }

}
class ChatItem:Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_from_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }

}