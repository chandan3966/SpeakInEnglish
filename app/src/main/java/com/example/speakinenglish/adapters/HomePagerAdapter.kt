package com.example.speakinenglish.adapters

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePagerAdapter(
    fragmentActivity: FragmentActivity,
    var fragmentList: ArrayList<Fragment>
) :
    FragmentStateAdapter(fragmentActivity) {


    @NonNull
    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun getFragments(): ArrayList<Fragment> {
        return fragmentList
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }
}