package com.tts.gueststar.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.tts.gueststar.fragments.FirstIntroFragment

class TutorialFragmentAdapter(fm : FragmentManager, val countPage : Int) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return FirstIntroFragment().newInstance(position + 1)
    }

    override fun getCount(): Int {
       return countPage
    }
}