package com.example.runraiser.ui.history

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.runraiser.R
import com.example.runraiser.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_trainings.*

class TrainingsFragment : Fragment() {
    private lateinit var trainingsAdapter: TrainingsRecyclerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trainings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initRecyclerView()
        addDataSet()
    }

    private fun addDataSet() {
        Log.i(tag, "Fetched trainings data")
        trainingsAdapter.submitList(HistoryData.myTrainingsData)
    }

    private fun initRecyclerView() {
        rv_training_history.apply {
            layoutManager = LinearLayoutManager(context)
            val topSpacingDecoration = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecoration)
            trainingsAdapter = TrainingsRecyclerAdapter()
            adapter = trainingsAdapter
        }
    }
}
