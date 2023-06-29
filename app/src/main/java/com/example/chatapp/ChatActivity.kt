package com.example.chatapp


import android.content.Context
import android.os.Bundle

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var messageReceiverID: String
    private lateinit var messageReceiverName: String
    private lateinit var messageReceiverImage: String
    private lateinit var messageSenderID: String

    private lateinit var userName: TextView
    private lateinit var userLastSeen: TextView
    private lateinit var userImage: CircleImageView

    private lateinit var chatToolBar: Toolbar
    private lateinit var mAuth: FirebaseAuth
    private lateinit var rootRef: DatabaseReference

    private lateinit var sendMessageButton: ImageButton
    private lateinit var sendFilesButton: ImageButton
    private lateinit var messageInputText: EditText

    private val messagesList: MutableList<Messages> = ArrayList()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var userMessagesList: RecyclerView

    private var saveCurrentTime: String? = null
    private var saveCurrentDate: String? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mAuth = FirebaseAuth.getInstance()
        messageSenderID = mAuth.currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference

        messageReceiverID = intent.extras?.get("visit_user_id").toString()
        messageReceiverName = intent.extras?.get("visit_user_name").toString()
        messageReceiverImage = intent.extras?.get("visit_image").toString()

        initializeControllers()

        userName.text = messageReceiverName
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage)

        sendMessageButton.setOnClickListener { sendMessage() }

        displayLastSeen()
    }

    private fun initializeControllers() {
        chatToolBar = findViewById(R.id.chat_toolbar)
        setSupportActionBar(chatToolBar)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowCustomEnabled(true)

        val layoutInflater: LayoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionBarView: View = layoutInflater.inflate(R.layout.custom_chat_bar, null)
        actionBar?.customView = actionBarView

        userName = findViewById(R.id.custom_profile_name)
        userLastSeen = findViewById(R.id.custom_user_last_seen)
        userImage = findViewById(R.id.custom_profile_image)

        sendMessageButton = findViewById(R.id.send_message_btn)

        messageInputText = findViewById(R.id.input_message)

        messageAdapter = MessageAdapter(messagesList)
        userMessagesList = findViewById(R.id.private_messages_list_of_users)
        linearLayoutManager = LinearLayoutManager(this)
        userMessagesList.layoutManager = linearLayoutManager
        userMessagesList.adapter = messageAdapter

        val calendar: Calendar = Calendar.getInstance()
        val currentDate: SimpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        saveCurrentDate = currentDate.format(calendar.time)
        val currentTime: SimpleDateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        saveCurrentTime = currentTime.format(calendar.time)
    }





    private fun displayLastSeen() {
    rootRef.child("Users").child(messageReceiverID)
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("userState").hasChild("state")) {
                    val state = dataSnapshot.child("userState").child("state").value.toString()
                    val date = dataSnapshot.child("userState").child("date").value.toString()
                    val time = dataSnapshot.child("userState").child("time").value.toString()

                    if (state == "online") {
                        userLastSeen.text = "online"
                    } else if (state == "offline") {
                        userLastSeen.text = "Last Seen: $date $time"
                    }
                } else {
                    userLastSeen.text = "offline"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
}

override fun onStart() {
    super.onStart()

    rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
        .addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val messages: Messages? = dataSnapshot.getValue(Messages::class.java)
                messagesList.add(messages!!)
                messageAdapter.notifyDataSetChanged()
                userMessagesList.smoothScrollToPosition(userMessagesList.adapter!!.itemCount)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
}

private fun sendMessage() {
    val messageText = messageInputText.text.toString()

    if (TextUtils.isEmpty(messageText)) {
        Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show()
    } else {
        val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
        val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"

        val userMessageKeyRef: DatabaseReference =
            rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push()
        val messagePushID = userMessageKeyRef.key

        val messageTextBody: MutableMap<String, Any?> = HashMap()
        messageTextBody["message"] = messageText
        messageTextBody["type"] = "text"
        messageTextBody["from"] = messageSenderID
        messageTextBody["to"] = messageReceiverID
        messageTextBody["messageID"] = messagePushID!!
        messageTextBody["time"] = saveCurrentTime!!
        messageTextBody["date"] = saveCurrentDate!!

        val messageBodyDetails: MutableMap<String, Any> = HashMap()
        messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
        messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody

        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener { task: Task<Void?> ->
            if (task.isSuccessful) {
                Toast.makeText(this@ChatActivity, "Message Sent Successfully...", Toast.LENGTH_SHORT).show()


            } else {
                Toast.makeText(this@ChatActivity, "Error", Toast.LENGTH_SHORT).show()
            }
            messageInputText.setText("")
        }
    }
}
}

