package com.example.speakinenglish.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.speakinenglish.R
import kotlinx.android.synthetic.main.item_avatar.view.*

class AvatarsAdapter(
    private var avatars: ArrayList<String>,
    private var avatarSelectedListener: AvatarSelectedListener): RecyclerView.Adapter<AvatarsAdapter.AvatarViewHolder>() {

    class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        return AvatarViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_avatar, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        Glide.with(holder.itemView).load(avatars.get(position)).into(holder.itemView.profile_image)
        holder.itemView.setOnClickListener {
            avatarSelectedListener.onAvatarSelected(avatars.get(position))
        }
    }

    override fun getItemCount(): Int {
        return avatars.size
    }
}

interface AvatarSelectedListener {
    fun onAvatarSelected(avatar: String)
}