package com.example.pracainz.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pracainz.R
import com.example.pracainz.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        registerButtonConfirm.setOnClickListener {
            performRegister()
        }
    }
    fun performRegister(){

        val login = emailTextRegister.text.toString()
        val password = passwordTextRegister.text.toString()
        if(login.isEmpty()||password.isEmpty()) {

            Toast.makeText(this,"Please enter email/password", Toast.LENGTH_SHORT).show()
            return

        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(login,password).addOnCompleteListener {
            if(!it.isSuccessful)return@addOnCompleteListener
            saveUserToDatabase()
            finish()
        }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Create User", Toast.LENGTH_SHORT).show()
            }

    }
    private fun saveUserToDatabase(){
        val username=usernameText.text.toString()
        val uid= FirebaseAuth.getInstance().uid?: ""
        val ref= FirebaseDatabase.getInstance().getReference("/users/$uid")
        val description=""
        val user= User(uid, username,description,"",0)
        ref.setValue(user).addOnSuccessListener {
            Toast.makeText(this, "New User Added", Toast.LENGTH_SHORT).show()
        }

    }
}
