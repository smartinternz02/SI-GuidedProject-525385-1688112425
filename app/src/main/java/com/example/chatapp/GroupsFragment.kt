package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupsFragment : Fragment() {
    private lateinit var groupFragmentView: View
    private lateinit var listView: ListView
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val listOfGroups: ArrayList<String> = ArrayList()

    private lateinit var groupRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false)

        groupRef = FirebaseDatabase.getInstance().getReference("Groups")
        initializeFields()
        retrieveAndDisplayGroups()

        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            val currentGroupName = adapterView.getItemAtPosition(position).toString()
            val groupChatIntent = Intent(context, GroupChatActivity::class.java)
            groupChatIntent.putExtra("groupName", currentGroupName)
            startActivity(groupChatIntent)
        }
        return groupFragmentView
    }

    private fun initializeFields() {
        listView = groupFragmentView.findViewById(R.id.list_view)
        arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOfGroups)
        listView.adapter = arrayAdapter
    }


    private fun retrieveAndDisplayGroups() {
        groupRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val set: MutableSet<String> = HashSet()
                val iterator = dataSnapshot.children.iterator()

                while (iterator.hasNext()) {
                    set.add((iterator.next() as DataSnapshot).key!!)
                }

                listOfGroups.clear()
                listOfGroups.addAll(set)
                arrayAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}
