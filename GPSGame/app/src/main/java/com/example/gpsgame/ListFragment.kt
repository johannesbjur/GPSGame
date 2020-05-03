package com.example.gpsgame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.view.*

class ListFragment : Fragment() {

    private lateinit var viewOfLayout: View
    lateinit var activity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_list, container, false)

        viewOfLayout.recyclerView.layoutManager = LinearLayoutManager(this.context)

        activity = context as MainActivity

        val placeItems = activity.placeItems

        val adapter = this.context?.let { PlaceRecyclerAdapter(it, placeItems ) }

        viewOfLayout.recyclerView.adapter = adapter

        return viewOfLayout
    }

}
