package com.example.pracainz.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.pracainz.R
import com.example.pracainz.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton.setOnClickListener {
            performLogin()
        }

        registerButtonGo.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent) }


    }
    private fun userTypeDirecting(loggedUser:Int){

        if(loggedUser==0){
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            Log.d("brawo",loggedUser.toString())
            finish()
        }
        else if(loggedUser==1){
            val intent = Intent(this, DriveActivity::class.java)
            startActivity(intent)
            Log.d("brawo",loggedUser.toString())
            finish()
        }
        else{
            Toast.makeText(this,"Zly typ uzytkownika", Toast.LENGTH_SHORT).show()
        }
    }
    private fun performLogin(){
        val login=emailTextRegister.text.toString()
        val password=passwordTextRegister.text.toString()
        if(login.isEmpty()||password.isEmpty()) {

            Toast.makeText(this,"Please enter email/password", Toast.LENGTH_SHORT).show()
            return

        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(login,password).addOnCompleteListener {
            if(!it.isSuccessful)return@addOnCompleteListener
            val uid= FirebaseAuth.getInstance().uid?: ""
            val ref=FirebaseDatabase.getInstance().getReference("/users/$uid/isDriver")
            ref.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                  var loggedUser=p0.getValue(Int::class.java)
                       userTypeDirecting(loggedUser!!)

                }

            })


        }.addOnFailureListener {
            Toast.makeText(this,"Zly login haslo", Toast.LENGTH_SHORT).show()
        }
    }

}