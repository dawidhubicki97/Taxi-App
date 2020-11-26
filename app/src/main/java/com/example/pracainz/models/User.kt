package com.example.pracainz.models

import com.google.firebase.database.PropertyName


class User(val uid:String, val username:String, @get:PropertyName("isDriver") @set:PropertyName("isDriver") @PropertyName("isDriver")var isDriver:Int, @get:PropertyName("isOnline") @set:PropertyName("isOnline") @PropertyName("isOnline")var isOnline:Boolean, val status:Boolean,var phone:String)
{
    constructor():this("","",0,false,false,"")
}
