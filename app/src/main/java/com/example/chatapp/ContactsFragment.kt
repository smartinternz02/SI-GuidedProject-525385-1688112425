package com.example.chatapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ContactsFragment : Fragment() {
    private lateinit var contactsView: View
    private lateinit var myContactsList: RecyclerView
    private lateinit var contactsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false)

        myContactsList = contactsView.findViewById(R.id.contacts_list)
        myContactsList.layoutManager = LinearLayoutManager(context)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid

        contactsRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserID)
        usersRef = FirebaseDatabase.getInstance().reference.child("Users")

        return contactsView
    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(contactsRef, Contacts::class.java)
            .build()

        val adapter = object : FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            override fun onBindViewHolder(holder: ContactsViewHolder, position: Int, model: Contacts) {
                val userIDs = getRef(position).key!!

                usersRef.child(userIDs).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                val state = dataSnapshot.child("userState").child("state").value.toString()
                                val date = dataSnapshot.child("userState").child("date").value.toString()
                                val time = dataSnapshot.child("userState").child("time").value.toString()

                                if (state == "online") {
                                    holder.onlineIcon.visibility = View.VISIBLE
                                } else if (state == "offline") {
                                    holder.onlineIcon.visibility = View.INVISIBLE
                                }
                            } else {
                                holder.onlineIcon.visibility = View.INVISIBLE
                            }

                            if (dataSnapshot.hasChild("image")) {
                                val userImage = dataSnapshot.child("image").value.toString()
                                val profileName = dataSnapshot.child("name").value.toString()
                                val profileStatus = dataSnapshot.child("status").value.toString()

                                holder.userName.text = profileName
                                holder.userStatus.text = profileStatus
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image)
                                    .into(holder.profileImage)
                            } else {
                                val profileName = dataSnapshot.child("name").value.toString()
                                val profileStatus = dataSnapshot.child("status").value.toString()
                                        holder.userName.text = profileName
                                        holder.userStatus.text = profileStatus
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}

                })
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ContactsViewHolder {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.users_display_layout, viewGroup, false)
                return ContactsViewHolder(view)
            }
        }

        myContactsList.adapter = adapter
        adapter.startListening()
    }

    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_profile_name)
        val userStatus: TextView = itemView.findViewById(R.id.user_status)
        val profileImage: CircleImageView = itemView.findViewById(R.id.users_profile_image)
        val onlineIcon: ImageView = itemView.findViewById(R.id.user_online_status)
    }
}
