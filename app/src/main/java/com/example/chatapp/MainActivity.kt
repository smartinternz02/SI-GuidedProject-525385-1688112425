package com.example.chatapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat

import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var myViewPager: ViewPager2
    private lateinit var myTabLayout: TabLayout
    private lateinit var myTabsAccessorAdapter: TabAccessorAdapter
    private var currentUser: FirebaseUser? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var rootRef: DatabaseReference
    private var currentUserID: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser
        rootRef = FirebaseDatabase.getInstance().reference
        currentUserID = mAuth.currentUser?.uid

        mToolbar = findViewById<Toolbar>(R.id.main_page_tool_bar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = "ChatApp"

        myViewPager = findViewById(R.id.main_tabs_pager) as ViewPager2
        myTabsAccessorAdapter = TabAccessorAdapter(supportFragmentManager, lifecycle)
        myViewPager.adapter = myTabsAccessorAdapter

        myTabLayout = findViewById(R.id.main_tabs) as TabLayout
        TabLayoutMediator(myTabLayout, myViewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Chat"

                }
                1 -> tab.text = "Groups"
                2 -> tab.text = "Contacts"

                3 -> tab.text = "Requests"
            }
        }.attach()

    }
    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            sendUserToLoginActivity()
        }
        else
        {
            updateUserStatus("online");
            verifyUserExistence()
        }

    }

    override fun onStop() {
        super.onStop()

        if (currentUser != null) {
            updateUserStatus("offline")
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        if (currentUser != null) {
            updateUserStatus("offline")
        }
    }

    private fun verifyUserExistence() {
        val currentUserID = mAuth.currentUser?.uid

        currentUserID?.let { uid ->
            rootRef.child("Users").child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("name").exists()) {
                        Toast.makeText(this@MainActivity, "Welcome", Toast.LENGTH_SHORT).show()
                    } else {
                        sendUserTosettingsActivity()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled event
                }
            })
        }
    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.main_logout_option -> {

                mAuth.signOut()
                sendUserToLoginActivity()
            }
            R.id.main_settings_option -> sendUserTosettingsActivity()
            R.id.main_create_group_option ->  RequestNewGroup()
            R.id.main_find_friends_option->sendUserToFindFriendsActivity()
        }

        return true
    }

    private fun RequestNewGroup() {
        val builder = AlertDialog.Builder(this@MainActivity, R.style.AlertDialog)
        builder.setTitle("Enter Group Name :")

        val groupNameField = EditText(this@MainActivity)
        groupNameField.hint = "e.g Chat Group"
        builder.setView(groupNameField)

        builder.setPositiveButton("Create") { dialogInterface, _ ->
            val groupName = groupNameField.text.toString()

            if (groupName.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please write Group Name...", Toast.LENGTH_SHORT).show()
            } else {
                createNewGroup(groupName)
            }
        }

        builder.setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.cancel()
        }

        builder.show()
    }


    private fun createNewGroup(groupName: String) {
            rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@MainActivity, "$groupName group is Created Successfully...", Toast.LENGTH_SHORT).show()
                    }
                }
    }




    private fun sendUserToLoginActivity() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(loginIntent)
        finish()
    }

    private fun sendUserTosettingsActivity() {
        val settingsIntent = Intent(this, SettingsActivity::class.java)
        startActivity(settingsIntent)

    }

    private fun sendUserToFindFriendsActivity() {
        val findFriendsIntent = Intent(this, FindFriendsActivity::class.java)

        startActivity(findFriendsIntent)
        finish()
    }
    private fun updateUserStatus(state: String) {
        val saveCurrentTime: String
        val saveCurrentDate: String

        val calendar = Calendar.getInstance()

        val currentDate = SimpleDateFormat("MMM dd, yyyy")
        saveCurrentDate = currentDate.format(calendar.time)

        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)

        val onlineStateMap = HashMap<String, Any>()
        onlineStateMap["time"] = saveCurrentTime
        onlineStateMap["date"] = saveCurrentDate
        onlineStateMap["state"] = state

        currentUserID?.let {
            rootRef.child("Users")
                .child(it)
                .child("userState")
                .updateChildren(onlineStateMap)
        }
    }


}






