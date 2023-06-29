package com.example.chatapp

import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAccessorAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChatFragment()
            1 -> GroupsFragment()
            2 -> ContactsFragment()
             3 -> RequestsFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    @Nullable
    override fun getItemId(position: Int): Long {
        // Optionally, you can override this method to assign unique IDs to fragments
        return super.getItemId(position)
    }

    @Nullable
    override fun containsItem(itemId: Long): Boolean {
        // Optionally, you can override this method to check if the adapter contains a fragment with the given ID
        return super.containsItem(itemId)
    }
}
