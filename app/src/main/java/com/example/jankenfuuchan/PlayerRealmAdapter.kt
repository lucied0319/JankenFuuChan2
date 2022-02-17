package com.example.jankenfuuchan

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.jankenfuuchan.databinding.CardLayoutBinding
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class PlayerRealmAdapter( data : OrderedRealmCollection<Player>,autoUpdate : Boolean) :
    RealmRecyclerViewAdapter<Player,PlayerRealmAdapter.PlayerViewHolder>(data,autoUpdate) {

    inner class PlayerViewHolder(val binding : CardLayoutBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CardLayoutBinding.inflate(layoutInflater,parent,false)

        //ホルダー作成

        var holder = PlayerViewHolder(binding)

        ////短くサイクルビューをタップした時のリスナーをセット

        holder.itemView.setOnClickListener{
            val position = holder.adapterPosition
            data?.let{(parent.context as MakePlayer).onRecycleViewClick(position,it.get(position))}

        }
        ////長くリサイクルビューをタップした時のリスナーをセット

        holder.itemView.setOnLongClickListener{
            val position = holder.adapterPosition
            data?.run {
                val dialog = AlertDeleteDialog.newInstance(position, this.get(position).name)
                dialog.show((parent.context as MakePlayer).supportFragmentManager,"削除")
            }
            return@setOnLongClickListener true
        }
        return holder
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = data?.get(position)
        holder.binding.apply {
            player?.run {
                textNumber.text = "${position + 1}"
                textName.text = name
            }
        }
    }
}