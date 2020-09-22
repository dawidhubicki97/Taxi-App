package com.example.pracainz.models


data class ChatMessage(val fromId: String,val toId:String,val messageText:String,val timestamp: Long){
    constructor():this("","", "",0)

}