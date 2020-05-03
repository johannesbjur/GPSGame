package com.example.gpsgame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationgame.PlaceItem
import java.security.AccessControlContext

class PlaceRecyclerAdapter(private val context: Context, private val placeItems: List<PlaceItem> ):
    RecyclerView.Adapter<PlaceRecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

       val itemView = layoutInflater.inflate(R.layout.list_item, parent, false )

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = placeItems.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = placeItems[position]

        holder.textViewTitle.text = item.name
    }



    inner class ViewHolder( itemView: View ): RecyclerView.ViewHolder( itemView ) {

        val textViewTitle = itemView.findViewById<TextView>(R.id.textViewTitle)
    }

}