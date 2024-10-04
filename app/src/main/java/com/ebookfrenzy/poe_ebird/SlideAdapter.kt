package com.ebookfrenzy.poe_ebird

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SlideAdapter(val frag: Fragment): FragmentStateAdapter(frag) {
    private val fragments = mutableMapOf<Int, Fragment>()
    override fun getItemCount()=3

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> Achievements() // First fragment
            1 -> Observations()    // Second fragment
            2 -> Feedbacks()     // Third fragment
            else -> throw IllegalArgumentException("Invalid position")
        }
            fragments[position] = fragment // Keep track of the fragments
                return fragment
        }

        // Assign a unique tag to each fragment based on position
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun containsItem(itemId: Long): Boolean {
            return fragments.containsKey(itemId.toInt())
        }

        fun removeFragment(position: Int) {
            val fragment = fragments[position]
            if (fragment != null) {
                val transaction = frag.childFragmentManager.beginTransaction()
                transaction.remove(fragment).commitNowAllowingStateLoss()
                fragments.remove(position)
            }
        }
}