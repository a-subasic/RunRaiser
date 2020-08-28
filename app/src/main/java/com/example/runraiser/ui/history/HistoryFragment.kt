package com.example.runraiser.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.example.runraiser.R
import com.example.runraiser.TrainingsDataCallback

class HistoryFragment : Fragment() {
    private lateinit var historyViewModel: HistoryViewModel

    private var tvTrainings: TextView? = null
    private var tvDonations: TextView? = null
    private var vpHistory: ViewPager? = null

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

        tvTrainings = view.findViewById(R.id.tv_trainings) as TextView
        tvDonations = view.findViewById(R.id.tv_donations) as TextView
        vpHistory = view.findViewById(R.id.vp_history) as ViewPager?

        initialise()
    }

    private fun initialise() {

        tvTrainings!!.setOnClickListener {
            vpHistory?.setCurrentItem(0, true)
        }

        tvDonations!!.setOnClickListener {
            vpHistory?.setCurrentItem(1, true)
        }

        HistoryData.fetchTrainingsData(object : TrainingsDataCallback {
            override fun onTrainingsDataCallback(myTrainingsData: ArrayList<HistoryCard>) {
                val adapter = HistoryPagerViewAdapter(requireActivity().supportFragmentManager)

                vpHistory?.adapter = adapter

                vpHistory?.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(state: Int) {
                    }

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    }

                    override fun onPageSelected(position: Int) {
                        changeTabs(position)
                    }
                })
            }
        })
    }

    private fun changeTabs(position: Int) {
        if(position == 1) {
            tvDonations?.setTextColor(resources.getColor(R.color.black))
            tvDonations?.textSize = 20F

            tvTrainings?.setTextColor(resources.getColor(R.color.textTabLight))
            tvTrainings?.textSize = 16F
        }
        else {
            tvTrainings?.setTextColor(resources.getColor(R.color.black))
            tvTrainings?.textSize = 20F

            tvDonations?.setTextColor(resources.getColor(R.color.textTabLight))
            tvDonations?.textSize = 16F
        }
    }
}
