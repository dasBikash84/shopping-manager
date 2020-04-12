package com.dasbikash.book_keeper.activities.home.account

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.account.connections.FragmentConnections
import com.dasbikash.book_keeper.activities.home.account.profile.FragmentProfile
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import kotlinx.android.synthetic.main.fragment_account.*

class FragmentAccount : FragmentTemplate() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_profile.setOnClickListener { loadProfileFragment() }
        tv_connections.setOnClickListener { loadConnectionsFragment() }
        tv_con_requests.setOnClickListener { loadConnectionRequestsFragment() }

        loadProfileFragment()
    }

    private fun loadProfileFragment(){
        (activity as AppCompatActivity).apply {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_account,
                    FragmentProfile()
                )
                .commit()
            tv_profile.setBackgroundColor(Color.WHITE)
            tv_connections.setBackgroundColor(Color.LTGRAY)
            tv_con_requests.setBackgroundColor(Color.LTGRAY)
        }
    }

    private fun loadConnectionsFragment(){
        (activity as AppCompatActivity).apply {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_account,
                    FragmentConnections()
                )
                .commit()
            tv_connections.setBackgroundColor(Color.WHITE)
            tv_profile.setBackgroundColor(Color.LTGRAY)
            tv_con_requests.setBackgroundColor(Color.LTGRAY)
        }
    }

    private fun loadConnectionRequestsFragment(){
        (activity as AppCompatActivity).apply {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_account, FragmentProfileConnectionRequests())
                .commit()
            tv_con_requests.setBackgroundColor(Color.WHITE)
            tv_profile.setBackgroundColor(Color.LTGRAY)
            tv_connections.setBackgroundColor(Color.LTGRAY)
        }
    }

    override fun getPageTitle(context: Context):String? = context.getString(R.string.bmi_account)

}
