package com.example.runraiser.ui.donate


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.example.runraiser.GlideApp
import com.example.runraiser.R
import kotlinx.android.synthetic.main.layout_organization_card_item.view.*

class OrganizationsRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<OrganizationCard> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SelectGroupViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_organization_card_item, parent, false)
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

    fun submitList(HistoryCardList: List<OrganizationCard>) {
        items = HistoryCardList
        notifyDataSetChanged()
    }

    class SelectGroupViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {

        private val image = itemView.image
        private val name  = itemView.tv_name
        private val address  = itemView.tv_address
        private var phone = itemView.tv_phone
        private val email  = itemView.tv_email
        private val web = itemView.tv_web
        private val description = itemView.tv_description

        fun bind(organizationCard: OrganizationCard) {
            name.text = organizationCard.name
            address.text = organizationCard.address
            phone.text = organizationCard.phone
            email.text = organizationCard.email
            web.text = organizationCard.web
            description.text = organizationCard.description

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)

            GlideApp.with(itemView.context)
                .applyDefaultRequestOptions(requestOptions)
                .load(organizationCard.imageUrl)
                .into(image)
        }
    }
}