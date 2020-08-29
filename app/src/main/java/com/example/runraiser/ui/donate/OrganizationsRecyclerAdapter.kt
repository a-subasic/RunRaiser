package com.example.runraiser.ui.donate


import android.app.AlertDialog
import android.os.Build
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.example.runraiser.Firebase
import com.example.runraiser.GlideApp
import com.example.runraiser.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.donation_dialog.view.*
import kotlinx.android.synthetic.main.layout_organization_card_item.view.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


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
        private val select = itemView.donate_btn

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

            select.setOnClickListener {
                val mDialogView = LayoutInflater.from(itemView.context).inflate(R.layout.donation_dialog, null)
                val mBuilder = AlertDialog.Builder(itemView.context)
                    .setView(mDialogView)
                    .setTitle("Enter the amount of money you want to donate:")

                val  mAlertDialog = mBuilder.show()

                mDialogView.et_donation_money.setText(DonateFragment.fund.toString())
                mDialogView.et_donation_money.filters =
                    arrayOf<InputFilter>(InputFilterMinMax("1", DonateFragment.fund.toString()))

                mDialogView.ok_btn.setOnClickListener {
                    if(mDialogView.et_donation_money.text.isNotEmpty()) {
                        val donationMoney = mDialogView.et_donation_money.text.toString().toInt()
                        val newFund = DonateFragment.fund - donationMoney
                        val ref =  Firebase.databaseUsers?.child(FirebaseAuth.getInstance().uid!!)
                       ref?.child("sumDonationsMoney")
                            ?.addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {
                                }
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val sum = snapshot.value.toString().toInt() + donationMoney
                                   ref.child("sumDonationsMoney").setValue(sum)
                                }
                            })
                        ref?.child("fund")?.setValue(newFund)

                        Toast.makeText(itemView.context, "You donated ${donationMoney} kn to ${organizationCard.name}! :D", Toast.LENGTH_SHORT).show()

                        val donationId = UUID.randomUUID().toString().replace("-", "").toUpperCase(
                            Locale.ROOT)

                        Firebase.databaseDonations?.child(donationId)?.child("organizationName")?.setValue(organizationCard.name)
                        Firebase.databaseDonations?.child(donationId)?.child("moneyDonated")?.setValue(donationMoney)
                        Firebase.databaseDonations?.child(donationId)?.child("organizationImage")?.setValue(organizationCard.imageUrl)

                        var startDate: String?
                        startDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val current = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                            current.format(formatter)
                        } else {
                            val date = Date()
                            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
                            formatter.format(date)
                        }

                        Firebase.databaseDonations?.child(donationId)?.child("date")?.setValue(startDate)
                        Firebase.databaseDonations?.child(donationId)?.child("userId")?.setValue(FirebaseAuth.getInstance().uid!!)
                        mAlertDialog.dismiss()
                    }
                    else {
                        mDialogView.et_donation_money.error
                        mDialogView.et_donation_money.requestFocus()
                    }
                }

                mDialogView.cancel_btn.setOnClickListener {
                    mAlertDialog.dismiss()
                }

            }
        }
    }
}