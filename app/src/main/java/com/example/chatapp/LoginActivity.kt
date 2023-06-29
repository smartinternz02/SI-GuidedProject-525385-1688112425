package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging



class LoginActivity : AppCompatActivity() {


    private lateinit var loginButton: Button
    private lateinit var phoneLoginButton: Button
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var needNewAccountLink: TextView
    private lateinit var forgetPasswordLink: TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var loadingProgressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")


        initializeFields()

        needNewAccountLink.setOnClickListener {
            sendUserToRegisterActivity()
        }
        loginButton.setOnClickListener {
            AllowUserToLogin()
        }
        phoneLoginButton.setOnClickListener {
            val phoneLoginIntent = Intent(this@LoginActivity, PhoneLoginActivity::class.java)
            startActivity(phoneLoginIntent)
        }


    }
    private fun AllowUserToLogin() {
        val email = userEmail.text.toString()
        val password = userPassword.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show()
            return
        }
        loadingProgressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUserId = mAuth.currentUser?.uid
                    val deviceToken = FirebaseMessaging.getInstance().token

                    val usersRef = FirebaseDatabase.getInstance().reference.child("users")

                    currentUserId?.let {
                        usersRef.child(it).child("device_token")
                            .setValue(deviceToken)
                            .addOnCompleteListener { deviceTokenTask ->
                                if (deviceTokenTask.isSuccessful) {
                                    sendUserToMainActivity()
                                    Toast.makeText(this@LoginActivity, "Logged in Successful...", Toast.LENGTH_SHORT).show()
                                    loadingProgressBar.visibility = View.VISIBLE
                                }
                            }
                    }
                } else {
                    val message = task.exception?.toString()
                    Toast.makeText(this@LoginActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                    loadingProgressBar.visibility = View.VISIBLE
                }
            }


    }



    private fun initializeFields() {
        loginButton = findViewById(R.id.login_button)
        phoneLoginButton = findViewById(R.id.phone_login_button)
        userEmail = findViewById(R.id.login_email)
        userPassword = findViewById(R.id.login_password)
        needNewAccountLink = findViewById(R.id.need_new_account_link)

        forgetPasswordLink = findViewById(R.id.forget_password_link)

        loadingProgressBar = ProgressBar(this)





    }


    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }


    private fun sendUserToRegisterActivity() {
        val registerIntent = Intent(this, RegisterActivity::class.java)
        startActivity(registerIntent)
    }


}


