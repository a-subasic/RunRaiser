package com.example.runraiser.ui.donate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runraiser.Firebase
import com.example.runraiser.OrganizationsDataCallback
import com.example.runraiser.R
import com.example.runraiser.TopSpacingItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_donate.*
import java.util.*
import kotlin.collections.ArrayList

class DonateFragment : Fragment() {

    private lateinit var donateViewModel: DonateViewModel
    private lateinit var organizationsAdapter: OrganizationsRecyclerAdapter

    companion object {
        var fund: Int = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        donateViewModel =
            ViewModelProviders.of(this).get(DonateViewModel::class.java)
        return inflater.inflate(R.layout.fragment_donate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         Firebase.databaseUsers?.child(FirebaseAuth.getInstance().uid.toString())?.child("fund")?.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                tv_fund.text = snapshot.value.toString() + " kn"
                fund = snapshot.value.toString().toInt()
            }
        })

        OrganizationsData.fetchOrganizationsData(object: OrganizationsDataCallback {
            override fun onOrganizationsDataCallback(myOrganizationsData: ArrayList<OrganizationCard>) {
                initRecyclerView()
                addDataSet()
            }
        })
    }

    private fun addDataSet() {
        Log.i(tag, "Fetched organizations data")
        organizationsAdapter.submitList(OrganizationsData.myOrganizationsData)
    }

    private fun initRecyclerView() {
        rv_organizations.apply {
            layoutManager = LinearLayoutManager(context)
            val topSpacingDecoration = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecoration)
            organizationsAdapter = OrganizationsRecyclerAdapter()
            adapter = organizationsAdapter
        }
    }
}