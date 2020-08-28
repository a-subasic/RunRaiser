package com.example.runraiser.ui.history

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.example.runraiser.GlideApp
import com.example.runraiser.R
import kotlinx.android.synthetic.main.layout_training_card_item.view.*

class TrainingsRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<HistoryCard> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SelectGroupViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_training_card_item, parent, false)
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

        private val mapsScreenshot = itemView.maps_screenshot
        private val duration  = itemView.tv_duration
        private val distance  = itemView.tv_distance
        private var goal  = itemView.tv_goal
        private val avgSpeed  = itemView.tv_avg_speed
        private val moneyRaised = itemView.tv_money_raised
        private val date = itemView.tv_date

        fun bind(historyCard: HistoryCard) {
            duration.text = historyCard.duration
            distance.text = historyCard.distanceKm + " km"
            goal.text = historyCard.kilometers + " km"
            avgSpeed.text = historyCard.avgSpeed + " km/h"
            moneyRaised.text = historyCard.moneyRaised + " kn"
            date.text = historyCard.startDate

            if(historyCard.distanceKm.toDouble() >= historyCard.kilometers.toDouble()) {
                goal.setTextColor(Color.parseColor("#81C784"))
            }
            else {
                goal.setTextColor(Color.parseColor("#E57373"))
            }

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)

            GlideApp.with(itemView.context)
                .applyDefaultRequestOptions(requestOptions)
                .load(historyCard.mapsScreenshotUrl)
                .into(mapsScreenshot)
        }
    }
}