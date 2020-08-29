package com.example.runraiser.ui.history


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.example.runraiser.GlideApp
import com.example.runraiser.R
import kotlinx.android.synthetic.main.layout_donation_card_item.view.*

class DonationsRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<DonationCard> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SelectGroupViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_donation_card_item, parent, false)
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

    fun submitList(DonationCardList: List<DonationCard>) {
        items = DonationCardList
        notifyDataSetChanged()
    }

    class SelectGroupViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {

        private val donationImage = itemView.orgImage
        private val donationName = itemView.orgName
        private val donationMoney = itemView.tv_donation_money
        private val donationDate = itemView.tv_donation_date

        fun bind(donationCard: DonationCard) {
            println(donationCard)
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)

            GlideApp.with(itemView.context)
                .applyDefaultRequestOptions(requestOptions)
                .load(donationCard.organizationImage)
                .into(donationImage)

            donationName.text = donationCard.organizationName
            donationMoney.text = donationCard.moneyDonated + " kn"
            donationDate.text = donationCard.date.substring(0, 10)
        }
    }
}