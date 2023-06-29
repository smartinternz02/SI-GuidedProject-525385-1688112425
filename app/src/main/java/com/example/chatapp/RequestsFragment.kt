package com.example.chatapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class RequestsFragment : Fragment() {

    private lateinit var requestsFragmentView: View
    private lateinit var myRequestsList: RecyclerView

    private lateinit var chatRequestsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var contactsRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser?.uid.orEmpty()
        usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        chatRequestsRef = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        contactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")

        myRequestsList = requestsFragmentView.findViewById(R.id.chat_requests_list)
        myRequestsList.layoutManager = LinearLayoutManager(context)

        return requestsFragmentView
    }

    override fun onStart() {
        super.onStart()

        val options: FirebaseRecyclerOptions<Contacts> = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(chatRequestsRef.child(currentUserID), Contacts::class.java)
            .build()

        val adapter: FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> =
            object : FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                override fun onBindViewHolder(holder: RequestsViewHolder, position: Int, model: Contacts) {
                    holder.itemView.findViewById<Button>(R.id.request_accept_btn).visibility = View.VISIBLE
                    holder.itemView.findViewById<Button>(R.id.request_cancel_btn).visibility = View.VISIBLE

                    val listUserId = getRef(position).key

                    val getTypeRef = getRef(position).child("request_type").ref
                    getTypeRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val type = dataSnapshot.value.toString()
                                if (type == "received") {
                                    usersRef.child(listUserId!!).addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if (dataSnapshot.hasChild("image")) {
                                                val requestProfileImage = dataSnapshot.child("image").value.toString()
                                                Picasso.get().load(requestProfileImage).into(holder.profileImage)
                                            }

                                            val requestUserName = dataSnapshot.child("name").value.toString()
                                            val requestUserStatus = dataSnapshot.child("status").value.toString()

                                            holder.userName.text = requestUserName
                                            holder.userStatus.text = "wants to connect with you."

                                            holder.itemView.setOnClickListener {
                                                val options = arrayOf<CharSequence>(
                                                    "Accept",
                                                    "Cancel"
                                                )

                                                val builder = AlertDialog.Builder(context)
                                                builder.setTitle("$requestUserName Chat Request")

                                                builder.setItems(options) { dialogInterface: DialogInterface?, i: Int ->
                                                    if (i == 0) {
                                                        contactsRef.child(currentUserID).child(listUserId!!)
                                                            .child("Contact").setValue("Saved")
                                                            .addOnCompleteListener { task: Task<Void?> ->
                                                                if (task.isSuccessful) {
                                                                    contactsRef.child(listUserId!!)
                                                                        .child(currentUserID)
                                                                        .child("Contact")
                                                                        .setValue("Saved")
                                                                        .addOnCompleteListener { task: Task<Void?> ->
                                                                            if (task.isSuccessful) {
                                                                                chatRequestsRef.child(currentUserID)
                                                                                    .child(listUserId)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener { task: Task<Void?> ->
                                                                                        if (task.isSuccessful) {
                                                                                            chatRequestsRef.child(listUserId)
                                                                                                .child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener { task: Task<Void?> ->
                                                                                                    if (task.isSuccessful) {
                                                                                                        Toast.makeText(
                                                                                                            context,
                                                                                                            "New Contact Saved",
                                                                                                            Toast.LENGTH_SHORT
                                                                                                        ).show()
                                                                                                    }
                                                                                                }
                                                                                        }
                                                                                    }
                                                                            }
                                                                        }
                                                                }
                                                            }
                                                    }
                                                    if (i == 1) {
                                                        chatRequestsRef.child(currentUserID)
                                                            .child(listUserId!!)
                                                            .removeValue()
                                                            .addOnCompleteListener { task: Task<Void?> ->
                                                                if (task.isSuccessful) {
                                                                    chatRequestsRef.child(listUserId)
                                                                        .child(currentUserID)
                                                                        .removeValue()
                                                                        .addOnCompleteListener { task: Task<Void?> ->
                                                                            if (task.isSuccessful) {
                                                                                Toast.makeText(
                                                                                    context,
                                                                                    "Contact Deleted",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }
                                                                        }
                                                                }
                                                            }
                                                    }
                                                }
                                                builder.show()
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {}

                                    })
                                } else if (type == "sent") {
                                    val requestSentBtn = holder.itemView.findViewById<Button>(R.id.request_accept_btn)
                                    requestSentBtn.text = "Req Sent"
                                    holder.itemView.findViewById<Button>(R.id.request_cancel_btn).visibility = View.INVISIBLE

                                    usersRef.child(listUserId!!).addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if (dataSnapshot.hasChild("image")) {
                                                val requestProfileImage = dataSnapshot.child("image").value.toString()
                                                Picasso.get().load(requestProfileImage).into(holder.profileImage)
                                            }

                                            val requestUserName = dataSnapshot.child("name").value.toString()
                                            val requestUserStatus = dataSnapshot.child("status").value.toString()

                                            holder.userName.text = requestUserName
                                            holder.userStatus.text = "you have sent a request to $requestUserName"

                                            holder.itemView.setOnClickListener {
                                                val options = arrayOf<CharSequence>("Cancel Chat Request")

                                                val builder = AlertDialog.Builder(context)
                                                builder.setTitle("Already Sent Request")

                                                builder.setItems(options) { dialogInterface: DialogInterface?, i: Int ->
                                                    if (i == 0) {
                                                        chatRequestsRef.child(currentUserID)
                                                            .child(listUserId!!)
                                                            .removeValue()
                                                            .addOnCompleteListener { task: Task<Void?> ->
                                                                if (task.isSuccessful) {
                                                                    chatRequestsRef.child(listUserId)
                                                                        .child(currentUserID)
                                                                        .removeValue()
                                                                        .addOnCompleteListener { task: Task<Void?> ->
                                                                            if (task.isSuccessful) {
                                                                                Toast.makeText(
                                                                                    context,
                                                                                    "You have cancelled the chat request.",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }
                                                                        }
                                                                }
                                                            }
                                                    }
                                                }
                                                builder.show()
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {}
                                    })
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}

                    })
                }

                override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RequestsViewHolder {
                    val view = LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.users_display_layout, viewGroup, false)
                    return RequestsViewHolder(view)
                }
            }

        myRequestsList.adapter =adapter

        adapter.startListening()
    }

    class RequestsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_profile_name)
        val userStatus: TextView = itemView.findViewById(R.id.user_status)
        val profileImage: CircleImageView = itemView.findViewById(R.id.users_profile_image)
    }
}


