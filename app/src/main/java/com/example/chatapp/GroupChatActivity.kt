package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class GroupChatActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var sendMessageButton: ImageButton
    private lateinit var userMessageInput: EditText
    private lateinit var mScrollView: ScrollView
    private lateinit var displayTextMessages: TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var UsersRef: DatabaseReference
    private lateinit var GroupNameRef: DatabaseReference
    private lateinit var GroupMessageKeyRef: DatabaseReference


    private var currentGroupName: String = ""
    private var currentUserID: String = ""
    private var currentUserName: String = ""
    private var currentDate: String = ""
    private var currentTime: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        currentGroupName = intent.getStringExtra("groupName").toString()
        Toast.makeText(this@GroupChatActivity, currentGroupName, Toast.LENGTH_SHORT).show()


        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName)



        InitializeFields()

        GetUserInfo()

        sendMessageButton.setOnClickListener {
            saveMessageInfoToDatabase()

            userMessageInput.text.clear()

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }

    }

    override fun onStart() {
        super.onStart()

        GroupNameRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
                    displayMessages(dataSnapshot)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
                    displayMessages(dataSnapshot)
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    private fun InitializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = currentGroupName

        sendMessageButton = findViewById(R.id.send_message_button)
        userMessageInput = findViewById(R.id.input_group_message)
        displayTextMessages = findViewById(R.id.group_chat_text_display)
        mScrollView = findViewById(R.id.my_scroll_view)
    }


    private fun GetUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun saveMessageInfoToDatabase() {
        val message = userMessageInput.text.toString()
        val messageKey = GroupNameRef.push().key

        if (message.isEmpty()) {
            Toast.makeText(this, "Please write a message first...", Toast.LENGTH_SHORT).show()
        } else {
            val calForDate = Calendar.getInstance()
            val currentDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            currentDate = currentDateFormat.format(calForDate.time)

            val calForTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            currentTime = currentTimeFormat.format(calForTime.time)

            val groupMessageKey = HashMap<String, Any>()
            GroupNameRef.updateChildren(groupMessageKey)

            GroupMessageKeyRef = GroupNameRef.child(messageKey!!)

            val messageInfoMap = HashMap<String, Any>()
            messageInfoMap["name"] = currentUserName
            messageInfoMap["message"] = message
            messageInfoMap["date"] = currentDate
            messageInfoMap["time"] = currentTime

            GroupMessageKeyRef.updateChildren(messageInfoMap)
        }
    }

    private fun displayMessages(dataSnapshot: DataSnapshot) {
        val iterator = dataSnapshot.children.iterator()

        while (iterator.hasNext()) {
            val chatDate = (iterator.next() as DataSnapshot).value as String
            val chatMessage = (iterator.next() as DataSnapshot).value as String
            val chatName = (iterator.next() as DataSnapshot).value as String
            val chatTime = (iterator.next() as DataSnapshot).value as String

            displayTextMessages.append("$chatName :\n$chatMessage\n$chatTime     $chatDate\n\n\n")

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }


}
