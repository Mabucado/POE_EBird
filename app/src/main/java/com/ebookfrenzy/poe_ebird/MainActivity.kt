package com.ebookfrenzy.poe_ebird

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.mindrot.jbcrypt.BCrypt

class MainActivity : AppCompatActivity() {
    lateinit var username:TextView
    lateinit var password:TextView
    lateinit var login:Button
    lateinit var register:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val model= globalModel
        username=findViewById(R.id.edtUsername)
        password=findViewById(R.id.edtPassword)
        login=findViewById(R.id.btnLogin)
        register=findViewById(R.id.btnLogRegister)

        retrieveAllUsers({ users ->
            // Successfully retrieved all users
            Log.d("Firestore", "Users retrieved successfully: $users")
        }) { exception ->
            // Handle any errors
            Log.w("Firestore", "Error retrieving users: ${exception.message}")
        }

        login.setOnClickListener {
            val user =username.text.toString()
            val pass= password.text.toString()
           if(model.usersList.map { it.username }.contains(user)){
                val passList= model.usersList.find { it.username==user }?.password
                if(BCrypt.checkpw(pass,passList)){
                    val intent= Intent(this,Home::class.java)
                    startActivity(intent)
                    model.loggedInUser=user
                    retrieveModelDataFromFirestore("loggedInUserId", { model ->
                        // Successfully retrieved the model
                        Log.d("Firestore", "Model retrieved successfully: $model")
                    }, { error ->
                        // Handle any errors
                        Log.w("Firestore", "Error retrieving model: ${error.message}")
                    })


                }
                else{
                    Toast.makeText(this, "Username or password is incorrect", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Username does not exist", Toast.LENGTH_SHORT).show()
            }

        }
        register.setOnClickListener {
            val intent=Intent(this,Registration::class.java)
            startActivity(intent)
        }


    }
}