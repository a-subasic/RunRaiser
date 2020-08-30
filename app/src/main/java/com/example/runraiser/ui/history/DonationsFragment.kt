package com.example.runraiser.ui.history

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runraiser.Firebase

import com.example.runraiser.R
import com.example.runraiser.TopSpacingItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_donations.*

class DonationsFragment : Fragment() {
    private lateinit var donationsAdapter: DonationsRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_donations, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Firebase.databaseUsers?.child(FirebaseAuth.getInstance().uid.toString())?.child("sumDonationsMoney")?.addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                tv_sum_money_donated.text = snapshot.value.toString() + " kn"
            }
        })
        initRecyclerView()
        addDataSet()
    }

    private fun addDataSet() {
        Log.i(tag, "Fetched donations data")
        donationsAdapter.submitList(HistoryData.myDonationsData)
    }

    private fun initRecyclerView() {
        rv_donations_history.apply {
            layoutManager = LinearLayoutManager(context)
            val topSpacingDecoration = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecoration)
            donationsAdapter = DonationsRecyclerAdapter()
            adapter = donationsAdapter
        }
    }
}
