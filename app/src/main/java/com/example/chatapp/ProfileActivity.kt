package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {
    private var receiverUserID: String? = null
    private var senderUserID: String? = null
    private var current_State: String? = null

    private lateinit var userProfileImage: CircleImageView
    private lateinit var userProfileName: TextView
    private lateinit var userProfileStatus: TextView
    private lateinit var sendMessageRequestButton: Button
    private lateinit var declineMessageRequestButton: Button

    private lateinit var userRef: DatabaseReference
    private lateinit var chatRequestRef: DatabaseReference
    private lateinit var contactsRef: DatabaseReference
    private lateinit var notificationRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests")
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts")
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications")

        receiverUserID = intent.getStringExtra("visit_user_id") ?: ""

        senderUserID = mAuth.currentUser?.uid

        userProfileImage = findViewById(R.id.visit_profile_image)
        userProfileName = findViewById(R.id.visit_user_name)
        userProfileStatus = findViewById(R.id.visit_profile_status)
        sendMessageRequestButton = findViewById(R.id.send_message_request_button)
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button)
        current_State = "new"

        retrieveUserInfo()

    }

    private fun retrieveUserInfo() {
        receiverUserID?.let {
            userRef.child(it).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                        /// val userImage = dataSnapshot.child("image").getValue().toString()
                        //val userName = dataSnapshot.child("name").getValue().toString()
                        //val userStatus = dataSnapshot.child("status").getValue().toString()

                        val userImage = dataSnapshot.child("image").getValue(String::class.java) ?: ""
                        val userName = dataSnapshot.child("name").getValue(String::class.java) ?: ""
                        val userStatus = dataSnapshot.child("status").getValue(String::class.java) ?: ""

                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage)
                        userProfileName.text = userName
                        userProfileStatus.text = userStatus

                        manageChatRequests()
                    } else {
                        val userName = dataSnapshot.child("name").getValue(String::class.java) ?: ""
                        val userStatus = dataSnapshot.child("status").getValue(String::class.java) ?: ""

                        userProfileName.text = userName
                        userProfileStatus.text = userStatus

                        manageChatRequests()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled event
                }
            })
        }
    }

    private fun manageChatRequests() {
        senderUserID?.let {
            chatRequestRef.child(it).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (receiverUserID?.let { it1 -> dataSnapshot.hasChild(it1) } == true) {
                        val requestType = receiverUserID?.let { it1 -> dataSnapshot.child(it1).child("request_type").getValue().toString() }

                        if (requestType == "sent") {
                            current_State = "request_sent"
                            sendMessageRequestButton.text = "Cancel Chat Request"
                        } else if (requestType == "received") {
                            current_State = "request_received"
                            sendMessageRequestButton.text = "Accept Chat Request"

                            declineMessageRequestButton.visibility = View.VISIBLE
                            declineMessageRequestButton.isEnabled = true

                            declineMessageRequestButton.setOnClickListener {
                                cancelChatRequest()
                            }
                        }
                    } else {
                        contactsRef.child(senderUserID!!).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (receiverUserID?.let { it1 -> dataSnapshot.hasChild(it1) } == true) {
                                    current_State = "friends"
                                    sendMessageRequestButton.text = "Remove this Contact"
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle onCancelled event
                            }
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled event
                }
            })
        }

        if (senderUserID != receiverUserID) {
            sendMessageRequestButton.setOnClickListener {
                sendMessageRequestButton.isEnabled = false

                when (current_State) {
                    "new" -> sendChatRequest()
                    "request_sent" -> cancelChatRequest()
                    "request_received" -> acceptChatRequest()
                    "friends" -> removeSpecificContact()
                }
            }
        } else {
            sendMessageRequestButton.visibility = View.INVISIBLE
        }
    }


    private fun removeSpecificContact() {
        senderUserID?.let {
            receiverUserID?.let { it1 ->
                contactsRef.child(it).child(it1)
                    .removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            contactsRef.child(receiverUserID!!).child(senderUserID!!)
                                .removeValue()
                                .addOnCompleteListener { innerTask ->
                                    if (innerTask.isSuccessful) {
                                        sendMessageRequestButton.isEnabled = true
                                        current_State = "new"
                                        sendMessageRequestButton.text = "Send Message"

                                        declineMessageRequestButton.visibility = View.INVISIBLE
                                        declineMessageRequestButton.isEnabled = false
                                    }
                                }
                        }
                    }
            }
        }
    }

    private fun acceptChatRequest() {
        senderUserID?.let {
            receiverUserID?.let { it1 ->
                contactsRef.child(it).child(it1)
                    .child("Contacts").setValue("Saved")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            contactsRef.child(receiverUserID!!).child(senderUserID!!)
                                .child("Contacts").setValue("Saved")
                                .addOnCompleteListener { innerTask ->
                                    if (innerTask.isSuccessful) {
                                        chatRequestRef.child(senderUserID!!).child(receiverUserID!!)
                                            .removeValue()
                                            .addOnCompleteListener { requestTask ->
                                                if (requestTask.isSuccessful) {
                                                    chatRequestRef.child(receiverUserID!!).child(
                                                        senderUserID!!
                                                    )
                                                        .removeValue()
                                                        .addOnCompleteListener { chatTask ->
                                                            sendMessageRequestButton.isEnabled = true
                                                            current_State = "friends"
                                                            sendMessageRequestButton.text = "Remove this Contact"

                                                            declineMessageRequestButton.visibility = View.INVISIBLE
                                                            declineMessageRequestButton.isEnabled = false
                                                        }
                                                }
                                            }
                                    }
                                }
                        }
                    }
            }
        }
    }

    private fun cancelChatRequest() {
        senderUserID?.let {
            receiverUserID?.let { it1 ->
                chatRequestRef.child(it).child(it1)
                    .removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            chatRequestRef.child(receiverUserID!!).child(senderUserID!!)
                                .removeValue()
                                .addOnCompleteListener { innerTask ->
                                    if (innerTask.isSuccessful) {
                                        sendMessageRequestButton.isEnabled = true
                                        current_State = "new"
                                        sendMessageRequestButton.text = "Send Message"

                                        declineMessageRequestButton.visibility = View.INVISIBLE
                                        declineMessageRequestButton.isEnabled = false
                                    }
                                }
                        }
                    }
            }
        }
    }

    private fun sendChatRequest() {
        senderUserID?.let {
            receiverUserID?.let { it1 ->
                chatRequestRef.child(it).child(it1)
                    .child("request_type").setValue("sent")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            chatRequestRef.child(receiverUserID!!).child(senderUserID!!)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener { innerTask ->
                                    if (innerTask.isSuccessful) {
                                        val chatNotificationMap = HashMap<String, String>()
                                        chatNotificationMap["from"] = senderUserID!!
                                        chatNotificationMap["type"] = "request"

                                        notificationRef.child(receiverUserID!!).push()
                                            .setValue(chatNotificationMap)
                                            .addOnCompleteListener { notificationTask ->
                                                if (notificationTask.isSuccessful) {
                                                    sendMessageRequestButton.isEnabled = true
                                                    current_State = "request_sent"
                                                    sendMessageRequestButton.text = "Cancel Chat Request"
                                                }
                                            }
                                    }
                                }
                        }
                    }
            }
        }
    }

}