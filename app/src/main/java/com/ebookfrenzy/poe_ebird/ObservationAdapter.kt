package com.ebookfrenzy.poe_ebird

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ObservationAdapter(
    private val recyclerData: List<ObservationRecyclerData>
) : RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder>() {

    class ObservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val birdImage: ImageView = itemView.findViewById(R.id.bird_image)
        val birdName: TextView = itemView.findViewById(R.id.bird_name)
        val birdLocation: TextView = itemView.findViewById(R.id.bird_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item, parent, false)
        return ObservationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        val currentItem = recyclerData[position]

        holder.birdName.text = currentItem.name
        holder.birdLocation.text = currentItem.location

        // Assuming the image is a URL, you can use libraries like Glide or Picasso to load it
        Glide.with(holder.birdImage.context)
            .load(currentItem.image)
            .error(R.drawable.noimageicon)
            .into(holder.birdImage)

    }

    override fun getItemCount() = recyclerData.size
}
