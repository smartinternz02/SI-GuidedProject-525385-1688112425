package com.example.chatapp
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class ChatFragment : Fragment() {
    private lateinit var privateChatsView: View
    private lateinit var chatsList: RecyclerView
    private lateinit var chatsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var currentUserID = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        privateChatsView = inflater.inflate(R.layout.fragment_chat, container, false)

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser?.uid ?: ""
        chatsRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(currentUserID)
        usersRef = FirebaseDatabase.getInstance().reference.child("Users")

        chatsList = privateChatsView.findViewById(R.id.chats_list)
        chatsList.layoutManager = LinearLayoutManager(context)

        return privateChatsView
    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(chatsRef, Contacts::class.java)
            .build()

        val adapter = object : FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            override fun onBindViewHolder(holder: ChatsViewHolder, position: Int, model: Contacts) {
                val usersIDs = getRef(position).key
                val retImage = arrayOf("default_image")

                usersRef.child(usersIDs!!).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                retImage[0] = dataSnapshot.child("image").value.toString()
                                Picasso.get().load(retImage[0]).into(holder.profileImage)
                            }

                            val retName = dataSnapshot.child("name").value.toString()
                            val retStatus = dataSnapshot.child("status").value.toString()

                            holder.userName.text = retName

                            if (dataSnapshot.child("userState").hasChild("state")) {
                                val state = dataSnapshot.child("userState").child("state").value.toString()
                                val date = dataSnapshot.child("userState").child("date").value.toString()
                                val time = dataSnapshot.child("userState").child("time").value.toString()

                                if (state == "online") {
                                    holder.userStatus.text = "online"
                                } else if (state == "offline") {
                                    holder.userStatus.text = "Last Seen: $date $time"
                                }
                            } else {
                                holder.userStatus.text = "offline"
                            }

                            holder.itemView.setOnClickListener {
                                val chatIntent = Intent(context, ChatActivity::class.java)
                                chatIntent.putExtra("visit_user_id", usersIDs)
                                chatIntent.putExtra("visit_user_name", retName)
                                chatIntent.putExtra("visit_image", retImage[0])
                                startActivity(chatIntent)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.users_display_layout, parent, false)
                return ChatsViewHolder(view)
            }
        }

        chatsList.adapter = adapter
        adapter.startListening()
    }

    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.users_profile_image)
        val userStatus: TextView = itemView.findViewById(R.id.user_status)
        val userName: TextView = itemView.findViewById(R.id.user_profile_name)
    }
}

