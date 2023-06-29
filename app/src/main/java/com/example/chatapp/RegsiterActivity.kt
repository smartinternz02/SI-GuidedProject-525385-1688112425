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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class RegisterActivity : AppCompatActivity() {

    private lateinit var createAccountButton: Button
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var alreadyHaveAccountLink: TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var rootRef: DatabaseReference


    private lateinit var loadingBar: ProgressBar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference


        initializeFields()

        alreadyHaveAccountLink.setOnClickListener {
            sendUserToLoginActivity()
        }

        createAccountButton.setOnClickListener {
            createNewAccount()
        }


    }



    private fun initializeFields() {
        createAccountButton = findViewById(R.id.register_button)
        userEmail = findViewById(R.id.register_email)
        userPassword = findViewById(R.id.register_password)
        alreadyHaveAccountLink = findViewById(R.id.already_have_account_link)


        loadingBar = ProgressBar(this)
    }

    private fun createNewAccount() {
        val email = userEmail.text.toString()
        val password = userPassword.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show()
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show()
        } else {
            val progressBar = ProgressBar(this)
            progressBar.visibility = View.VISIBLE

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val deviceToken = FirebaseMessaging.getInstance().token

                        val currentUserID = mAuth.currentUser?.uid
                        currentUserID?.let {
                            rootRef.child("Users").child(it).setValue("")
                            rootRef.child("Users").child(it).child("device_token")
                                .setValue(deviceToken)
                        }

                        sendUserToMainActivity()
                        Toast.makeText(this@RegisterActivity, "Account Created Successfully...", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.VISIBLE
                    } else {
                        val message = task.exception?.toString()
                        Toast.makeText(this@RegisterActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.VISIBLE
                    }
                }


        }
        }



    private fun sendUserToLoginActivity() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
}