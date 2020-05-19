package com.example.gpsgame

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    private lateinit var viewOfLayout: View
    private lateinit var activity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity = context as MainActivity

        viewOfLayout = inflater.inflate(R.layout.fragment_settings, container, false)
        viewOfLayout.settingsBackBtn?.setOnClickListener {

            activity.goToProfile()
        }


        return viewOfLayout
    }


}
