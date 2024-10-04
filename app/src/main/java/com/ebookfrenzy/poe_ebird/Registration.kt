package com.ebookfrenzy.poe_ebird

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.mindrot.jbcrypt.BCrypt

class Registration : AppCompatActivity() {
    lateinit var username: TextView
    lateinit var name: TextView
    lateinit var surname: TextView
    lateinit var email: TextView
    lateinit var password: TextView
    lateinit var confirm: TextView
    lateinit var model:Model
    lateinit var register:Button
    lateinit var back:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        model=globalModel
        username=findViewById(R.id.edtLogUsername)
        name=findViewById(R.id.edtName)
        surname=findViewById(R.id.edtSurname)
        email=findViewById(R.id.edtEmail)
        password=findViewById(R.id.edtLogPassword)
        confirm= findViewById(R.id.edtConfirm)
        register=findViewById(R.id.btnRegister)
        back=findViewById(R.id.btnBack)

        register.setOnClickListener {
            validateInputs()
        }
        back.setOnClickListener {
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
        }


    }
    fun store(username:String,name:String,surname:String,email:String,password:String) {
        model.usersList.add(Users(username,name,surname,email,null,null,password))
        Toast.makeText(this, "$username successfully registered", Toast.LENGTH_SHORT).show()

    }
    private fun validateInputs() {
        val usernames = username.text.toString().trim()
        val names = name.text.toString().trim()
        val surnames = surname.text.toString().trim()
        val emails = email.text.toString().trim()
        val passwords = password.text.toString().trim()
        val confirmp = confirm.text.toString().trim()

        if (usernames.isEmpty()) {
            username.error = "Username required"
            return
        }
        if (names.isEmpty()) {
            name.error = "Name is required"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emails).matches()) {
            email.error = "Enter a valid email address"
            return
        }

        if (passwords.length < 8) {
            password.error = "Password must be at least 8 characters long"
            return
        }
        val hashPassword: String
        if (passwords!=confirmp) {
            password.error = "Password does not match confirm password"
            return
        }
        else{
         hashPassword  = BCrypt.hashpw(passwords, BCrypt.gensalt())
        }

        // If all inputs are valid, proceed with registration process
        store(usernames,names,surnames, emails, hashPassword)
    }
}