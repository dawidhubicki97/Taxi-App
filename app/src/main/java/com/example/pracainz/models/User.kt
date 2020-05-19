package com.example.pracainz.models



class User(val uid:String,val username:String,val description:String,val avatarurl:String)
{
    constructor():this("","","","")
}
