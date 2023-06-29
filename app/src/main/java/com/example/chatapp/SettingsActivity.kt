package com.example.chatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import androidx.appcompat.widget.Toolbar

import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.squareup.picasso.Picasso




class SettingsActivity : AppCompatActivity() {
    private lateinit var updateAccountSettings: Button
    private lateinit var userName: EditText
    private lateinit var userStatus: EditText
    private lateinit var userProfileImage: CircleImageView



    private lateinit var currentUserID: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var rootRef: DatabaseReference

    private var GalleryPick: Int = 1
    private var UserProfileImagesRef: StorageReference? = null
    private var loadingBar: ProgressDialog? = null
    private var SettingsToolBar: Toolbar? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initializeFields()

        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        rootRef = FirebaseDatabase.getInstance().reference

        UserProfileImagesRef = FirebaseStorage.getInstance().reference.child("Profile Images")


        updateAccountSettings.setOnClickListener {
            updateSettings()
        }

        retrieveUserInfo()
        userName.visibility = View.INVISIBLE



        userProfileImage.setOnClickListener {
            val galleryIntent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                type = "image/*"
            }
            startActivityForResult(galleryIntent, GalleryPick)
        }








    }


    private fun initializeFields() {
        updateAccountSettings = findViewById(R.id.update_settings_button)
        userName = findViewById(R.id.set_user_name)
        userStatus = findViewById(R.id.set_profile_status)
        userProfileImage = findViewById(R.id.set_profile_image)


        loadingBar = ProgressDialog(this)

        SettingsToolBar = findViewById(R.id.settings_toolbar) as Toolbar
        setSupportActionBar(SettingsToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.title = "Account Settings"


    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data

            CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {
                loadingBar?.setTitle("Set Profile Image")
                loadingBar?.setMessage("Please wait, your profile image is updating...")
                loadingBar?.setCanceledOnTouchOutside(false)
                loadingBar?.show()

                val resultUri: Uri? = result.uri

                val filePath: StorageReference? = UserProfileImagesRef?.child("$currentUserID.jpg")

                filePath?.putFile(resultUri!!)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@SettingsActivity, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show()

                           // filePath.downloadUrl.addOnSuccessListener { uri ->
                              ///  val downloadedUrl = uri.toString()
                            val downloadedUrl: String? = task.result?.metadata?.reference?.downloadUrl?.toString()

                            rootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadedUrl)
                                    .addOnCompleteListener { innerTask ->
                                        if (innerTask.isSuccessful) {
                                            Toast.makeText(
                                                this@SettingsActivity,
                                                "Image saved in Database Successfully...",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            loadingBar?.dismiss()
                                        } else {
                                            val message: String? = innerTask.exception?.message
                                            Toast.makeText(
                                                this@SettingsActivity,
                                                "Error: $message",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            loadingBar?.dismiss()
                                        }

                            }
                        } else {
                            val message: String? = task.exception?.message
                            Toast.makeText(this@SettingsActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                            loadingBar?.dismiss()
                        }
                    }
            }
        }
    }




    private fun updateSettings() {
        val setUserName = userName.text.toString()
        val setStatus = userStatus.text.toString()
        if (setUserName.isEmpty()) {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show()
        } else if (setStatus.isEmpty()) {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show()
        } else {
            val profileMap = HashMap<String, Any>()
            profileMap["uid"] = currentUserID
            profileMap["name"] = setUserName
            profileMap["status"] = setStatus
            rootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        SendUserToMainActivity()
                        Toast.makeText(this@SettingsActivity, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show()
                    } else {
                        val message = task.exception.toString()
                        Toast.makeText(this@SettingsActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }




    private fun retrieveUserInfo() {
        rootRef.child("Users").child(currentUserID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")) {
                        val retrieveUserName = dataSnapshot.child("name").getValue().toString()
                        val retrievesStatus = dataSnapshot.child("status").getValue().toString()
                        val retrieveProfileImage = dataSnapshot.child("image").value.toString()

                        userName.setText(retrieveUserName)
                        userStatus.setText(retrievesStatus)
                       // Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        Picasso.get()
                            .load(retrieveProfileImage)
   // Set placeholder image resource
                            .into(userProfileImage)
                   
                    } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                        val retrieveUserName = dataSnapshot.child("name").getValue().toString()
                        val retrievesStatus = dataSnapshot.child("status").getValue().toString()

                        userName.setText(retrieveUserName)
                        userStatus.setText(retrievesStatus)
                    } else {
                        userName.visibility = View.VISIBLE
                        Toast.makeText(this@SettingsActivity, "Please set & update your profile information...", Toast.LENGTH_SHORT).show()
                    }
                }




                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled event
                }
            })
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

}