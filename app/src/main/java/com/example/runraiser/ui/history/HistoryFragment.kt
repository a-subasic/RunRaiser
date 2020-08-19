package com.example.runraiser.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runraiser.R
import com.example.runraiser.TopSpacingItemDecoration
import com.example.runraiser.TrainingsDataCallback
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment : Fragment() {
    private lateinit var historyAdapter: HistoryRecyclerAdapter
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel =
            ViewModelProviders.of(this).get(HistoryViewModel::class.java)
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialise()
    }

    private fun initialise() {
        HistoryData.fetchTrainingsData(object : TrainingsDataCallback {
            override fun onTrainingsDataCallback(myTrainingsData: ArrayList<HistoryCard>) {
                initRecyclerView()
                addDataSet()
            }
        })
    }

    private fun addDataSet() {
        Log.i(tag, "Fetched trainings data: " + HistoryData.myTrainingsData)
        historyAdapter.submitList(HistoryData.myTrainingsData)
    }

    private fun initRecyclerView() {
        rv_training_history.apply {
            layoutManager = LinearLayoutManager(context)
            val topSpacingDecoration = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecoration)
            historyAdapter = HistoryRecyclerAdapter()
            adapter = historyAdapter
        }
    }
}
