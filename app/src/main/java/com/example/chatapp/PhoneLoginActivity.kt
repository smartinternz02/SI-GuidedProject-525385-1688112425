package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {

    private lateinit var sendVerificationCodeButton: Button
    private lateinit var verifyButton: Button
    private lateinit var inputPhoneNumber: EditText
    private lateinit var inputVerificationCode: EditText

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mAuth: FirebaseAuth

    private lateinit var loadingBar: ProgressBar

    private var mVerificationId: String? = null
    private lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        mAuth = FirebaseAuth.getInstance()

        sendVerificationCodeButton = findViewById<Button>(R.id.send_ver_code_button)
        verifyButton = findViewById<Button>(R.id.verify_button)
        inputPhoneNumber = findViewById<EditText>(R.id.phone_number_input)
        inputVerificationCode = findViewById<EditText>(R.id.verification_code_input)
        loadingBar = findViewById<ProgressBar>(R.id.loading_bar)

        sendVerificationCodeButton.setOnClickListener {
            val phoneNumber = inputPhoneNumber.text.toString()

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this@PhoneLoginActivity, "Please enter your phone number first...", Toast.LENGTH_SHORT).show()
            } else {
                loadingBar.visibility = View.VISIBLE

                val options = PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this@PhoneLoginActivity)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }

        verifyButton.setOnClickListener {
            sendVerificationCodeButton.visibility = View.INVISIBLE
            inputPhoneNumber.visibility = View.INVISIBLE

            val verificationCode = inputVerificationCode.text.toString()

            if (verificationCode.isEmpty()) {
                Toast.makeText(this@PhoneLoginActivity, "Please write verification code first...", Toast.LENGTH_SHORT).show()
            } else {
                loadingBar.visibility = View.VISIBLE

                val credential = PhoneAuthProvider.getCredential(mVerificationId!!, verificationCode)
                signInWithPhoneAuthCredential(credential)
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                loadingBar.visibility = View.GONE
                Toast.makeText(this@PhoneLoginActivity, "Invalid Phone Number, Please enter correct phone number with your country code...", Toast.LENGTH_SHORT).show()

                sendVerificationCodeButton.visibility = View.VISIBLE
                inputPhoneNumber.visibility = View.VISIBLE

                verifyButton.visibility = View.INVISIBLE
                inputVerificationCode.visibility = View.INVISIBLE
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                mVerificationId = verificationId
                mResendToken = token

                loadingBar.visibility = View.GONE
                Toast.makeText(this@PhoneLoginActivity, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show()

                sendVerificationCodeButton.visibility = View.INVISIBLE
                inputPhoneNumber.visibility = View.INVISIBLE
                    verifyButton.visibility = View.VISIBLE
                inputVerificationCode.visibility = View.VISIBLE
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    loadingBar.visibility = View.GONE
                    Toast.makeText(this@PhoneLoginActivity, "Congratulations, you're logged in successfully...", Toast.LENGTH_SHORT).show()
                    sendUserToMainActivity()
                } else {
                    val message = task.exception?.toString()
                    Toast.makeText(this@PhoneLoginActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@PhoneLoginActivity, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
}

