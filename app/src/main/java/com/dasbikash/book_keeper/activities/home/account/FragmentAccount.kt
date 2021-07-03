package com.dasbikash.book_keeper.activities.home.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.account.connections.FragmentConnections
import com.dasbikash.book_keeper.activities.home.account.events.FragmentEventNotification
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

        viewPager.adapter = AccountPagerAdapter(activity!!.supportFragmentManager, mapOf(
            Pair(getString(R.string.profile_text),FragmentProfile()),
            Pair(getString(R.string.connections_text),FragmentConnections()),
            Pair(getString(R.string.events_text),FragmentEventNotification())
        ))

        tabLayout.setupWithViewPager(viewPager)

        when(isConnectionModeInstance()) {
            true -> {viewPager.setCurrentItem(1)}
            else -> {}
        }
    }

    override fun getPageTitle(context: Context):String? = context.getString(R.string.bmi_account)

    private fun isConnectionModeInstance() = arguments?.containsKey(ARG_CONNECTION_MODE) == true

    companion object{
        private const val ARG_CONNECTION_MODE = "com.dasbikash.book_keeper.activities.home.account.FragmentAccount.ARG_CONNECTION_MODE"
        fun getConnectionModeInstance():FragmentAccount{
            val arg = Bundle()
            arg.putSerializable(ARG_CONNECTION_MODE,ARG_CONNECTION_MODE)
            val fragment = FragmentAccount()
            fragment.arguments = arg
            return fragment
        }
    }

}

class AccountPagerAdapter(fm: FragmentManager, data: Map<String, Fragment>) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments:List<Fragment>
    private val titles:List<String>

    init {
        val keys = mutableListOf<String>()
        val frags = mutableListOf<Fragment>()
        data.keys.forEach{
            keys.add(it)
            frags.add(data.get(it)!!)
        }
        fragments = frags.toList()
        titles = keys.toList()
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int = fragments.count()

    override fun getPageTitle(position: Int): CharSequence? = titles[position]
}