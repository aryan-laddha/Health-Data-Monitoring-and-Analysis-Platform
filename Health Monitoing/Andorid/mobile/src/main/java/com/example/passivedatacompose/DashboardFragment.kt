package com.example.passivedatacompose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the WearableCommunicationFragment into the container
        loadWearableCommunicationFragment()

        // Add other views programmatically if needed
        // For example:
        // addOtherView()
    }

    private fun loadWearableCommunicationFragment() {
        val fragment = WearableCommunicationFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.dashboardContainer, fragment)
        transaction.addToBackStack(null) // Optional: Add this line if you want to enable back navigation
        transaction.commit()
    }

    // Example method to add other views programmatically
    private fun addOtherView() {
        val container = view?.findViewById<FrameLayout>(R.id.dashboardContainer)
        // Add other views to the container programmatically
    }
}
