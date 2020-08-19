package com.example.runraiser.ui.history

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.example.runraiser.R
import kotlinx.android.synthetic.main.layout_history_card_item.view.*

class HistoryRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<HistoryCard> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SelectGroupViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_history_card_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is SelectGroupViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(HistoryCardList: List<HistoryCard>) {
        items = HistoryCardList
        notifyDataSetChanged()
    }

    class SelectGroupViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {

        private val historyId = itemView.tv_history_card_id

        fun bind(HistoryCard: HistoryCard) {
            historyId.text = HistoryCard.id
//            btnSettings.text = "Edit"
//
//            val requestOptions = RequestOptions()
//                .placeholder(drawable.ic_launcher_background)
//                .error(drawable.ic_launcher_background)
//
//            GlideApp.with(itemView.context)
//                .applyDefaultRequestOptions(requestOptions)
//                .load(GroupCard.profileImage)
//                .into(addGroupsImage)
//
//            if(GroupCard.id == currentGroupId) {
//                container.background = ContextCompat.getDrawable(itemView.context, drawable.selected_group_bg)
//            }
//            else {
//                container.background = ContextCompat.getDrawable(itemView.context, drawable.comment_bg)
//            }
//
//            btnSettings!!.setOnClickListener { v ->
//                val context = v.context
//                val intent = Intent(context, GroupSettingsActivity::class.java)
//                intent.putExtra("GROUP_SETTINGS_ID", GroupCard.id)
//                context.startActivity(intent)
//            }
//
//            itemView.setOnClickListener { v->
//                currentGroupId = GroupCard.id
//                container.background = ContextCompat.getDrawable(itemView.context, R.color.white)
//                val intent = Intent(v.context, MapsActivity::class.java)
//                v.context.startActivity(intent)
//            }
        }
    }
}