package com.example.pracainz.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.pracainz.R
import com.example.pracainz.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    var roleselection=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        spinnerRole.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                roleselection=p2
            }

        }
        registerButtonConfirm.setOnClickListener {
            performRegister()
        }
    }
    fun performRegister(){

        val login = emailTextRegister.text.toString()
        val password = passwordTextRegister.text.toString()
        val repeatpassword=passwordRepeatTextRegister.text.toString()
        val phone=phoneTextView.text.toString()
        if(login.isEmpty()||password.isEmpty()||repeatpassword.isEmpty()||phone.isEmpty()) {

            Toast.makeText(this,"Please enter email/password", Toast.LENGTH_SHORT).show()
            return

        }
        if(repeatpassword.equals(password)==false){
            Toast.makeText(this,"Hasła nie są takie same", Toast.LENGTH_SHORT).show()
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
        val phone=phoneTextView.text.toString()
        val uid= FirebaseAuth.getInstance().uid?: ""
        val ref= FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user= User(uid, username,roleselection,false,false,phone)
        ref.setValue(user).addOnSuccessListener {
            Toast.makeText(this, "New User Added", Toast.LENGTH_SHORT).show()
        }

    }
}
