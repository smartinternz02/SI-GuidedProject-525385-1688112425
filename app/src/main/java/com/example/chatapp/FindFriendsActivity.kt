package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FindFriendsActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var findFriendsRecyclerList: RecyclerView
    private lateinit var UsersRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)



        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")

        findFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list)
        findFriendsRecyclerList.layoutManager = LinearLayoutManager(this)

        mToolbar = findViewById(R.id.find_friends_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Find Friends"

    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(UsersRef, Contacts::class.java)
            .build()

        val adapter = object : FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            override fun onBindViewHolder(holder: FindFriendViewHolder, position: Int, model: Contacts) {
                holder.userName.text = model.name
                holder.userStatus.text = model.status

               // Picasso.get().load(model.image).placeholder(R.drawable.profile_image).into(holder.profileImage)
                if (!model.image.isNullOrEmpty()) {
                    Picasso.get().load(model.image).placeholder(R.drawable.profile_image).into(holder.profileImage)
                } else {
                    // If image path is empty, load a placeholder image
                    Picasso.get().load(R.drawable.profile_image).into(holder.profileImage)
                }
                holder.itemView.setOnClickListener {
                    val visitUserId = getRef(position).key

                    val profileIntent = Intent(this@FindFriendsActivity, ProfileActivity::class.java)
                    profileIntent.putExtra("visit_user_id", visitUserId)
                    startActivity(profileIntent)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindFriendViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.users_display_layout, parent, false)
                return FindFriendViewHolder(view)
            }
        }

        findFriendsRecyclerList.adapter = adapter
        adapter.startListening()
    }

    class FindFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_profile_name)
        val userStatus: TextView = itemView.findViewById(R.id.user_status)
        val profileImage: CircleImageView = itemView.findViewById(R.id.users_profile_image)
    }

}