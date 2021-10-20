package com.mobileapplike.teamtrack.ui.fragments


import androidx.fragment.app.Fragment
import com.mobileapplike.teamtrack.ui.MainActivity
import com.mobileapplike.teamtrack.ui.fragments.map.MapFragment
import com.mobileapplike.teamtrack.ui.fragments.mygroupforeman.MyGroupForemansFragment

abstract class BaseFragment : Fragment() {



    override fun onResume() {
        super.onResume()
        if (this is MapFragment ) {
            (activity as MainActivity).setDrawerEnabled(true);
        } else {
            (activity as MainActivity).setDrawerEnabled(false);
        }
    }
}