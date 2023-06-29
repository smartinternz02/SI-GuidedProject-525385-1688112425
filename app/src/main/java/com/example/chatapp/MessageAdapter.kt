package com.example.chatapp

import android.content.res.Resources
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(private val userMessagesList: List<Messages>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var mAuth: FirebaseAuth? = null
    private var usersRef: DatabaseReference? = null

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var senderMessageText: TextView = itemView.findViewById(R.id.sender_message_text)
        var receiverMessageText: TextView = itemView.findViewById(R.id.receiver_message_text)
        var receiverProfileImage: CircleImageView = itemView.findViewById(R.id.message_profile_image)


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_messages_layout, parent, false)

        mAuth = FirebaseAuth.getInstance()

        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(messageViewHolder: MessageViewHolder, i: Int) {
        val messageSenderId = mAuth!!.currentUser!!.uid
        val messages = userMessagesList[i]
        val fromUserID = messages.from
        val fromMessageType = messages.type
        usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromUserID)
        usersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    val receiverImage = dataSnapshot.child("image").value.toString()

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image)
                        .into(messageViewHolder.receiverProfileImage)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        messageViewHolder.receiverMessageText.visibility = View.GONE
        messageViewHolder.receiverProfileImage.visibility = View.GONE
        messageViewHolder.senderMessageText.visibility = View.GONE
        if (fromMessageType == "text") {
            if (fromUserID == messageSenderId) {
                messageViewHolder.senderMessageText.visibility = View.VISIBLE
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout)
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK)
                messageViewHolder.senderMessageText.text = messages.message + "\n \n" + messages.time + " - " + messages.date
            } else {
                messageViewHolder.receiverProfileImage.visibility = View.VISIBLE
                messageViewHolder.receiverMessageText.visibility = View.VISIBLE
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.reciver_messages_layout)
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK)
                messageViewHolder.receiverMessageText.text = messages.message + "\n \n" + messages.time + " - " + messages.date
            }
        }

        val spacingDp = 16f // Adjust the spacing as needed
        val spacingPx = convertDpToPixel(spacingDp)

        // Set the spacing between messages
        val layoutParams = messageViewHolder.itemView.layoutParams as RecyclerView.LayoutParams
        if (i== userMessagesList.size - 1) {
            // Last message in the list, add bottom margin
            layoutParams.bottomMargin = spacingPx
        } else {
            layoutParams.bottomMargin = 0
        }

        messageViewHolder.itemView.layoutParams = layoutParams
    }



    override fun getItemCount(): Int {
        return userMessagesList.size
    }

    private fun convertDpToPixel(dp: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

}
