package com.dasbikash.exp_man.activities.home

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.exp_man.R
import com.dasbikash.super_activity.SingleFragmentSuperActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityHome : SingleFragmentSuperActivity(),WaitScreenOwner {

    override fun getDefaultFragment(): Fragment = FragmentAddExp()

    override fun getLayoutID(): Int = R.layout.activity_home

    override fun getLoneFrameId(): Int = R.id.home_frame

    override fun registerWaitScreen(): ViewGroup = wait_screen

    override fun onResume() {
        super.onResume()
        bottom_Navigation_View.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bmi_add -> {
                    if (getCurrentFragmentType() != FragmentAddExp::class.java) {
                        addFragmentClearingBackStack(FragmentAddExp())
                    }
                    true
                }
                R.id.bmi_summary -> {
                    if (getCurrentFragmentType() != FragmentExpSummary::class.java) {
                        addFragmentClearingBackStack(FragmentExpSummary())
                    }
                    true
                }
                R.id.bmi_budget -> {
                    if (getCurrentFragmentType() != FragmentBudget::class.java) {
                        addFragmentClearingBackStack(FragmentBudget())
                    }
                    true
                }
                R.id.bmi_shopping_list -> {
                    if (getCurrentFragmentType() != FragmentShoppingList::class.java) {
                        addFragmentClearingBackStack(FragmentShoppingList())
                    }
                    true
                }
                R.id.bmi_more -> {
                    if (getCurrentFragmentType() != FragmentMore::class.java) {
                        addFragmentClearingBackStack(FragmentMore())
                    }
                    true
                }
                else -> false
            }
        }
        bottom_Navigation_View.setOnNavigationItemReselectedListener { }
    }

    companion object{
        private const val EXTRA_GUEST = "com.dasbikash.exp_man.activities.home.ActivityHome.EXTRA_GUEST"
        private const val EXTRA_USER = "com.dasbikash.exp_man.activities.home.ActivityHome.EXTRA_USER"

        fun getGuestInstance(context: Context):Intent{
            val intent = Intent(context.applicationContext,
                ActivityHome::class.java)
            intent.putExtra(
                EXTRA_GUEST,
                EXTRA_GUEST
            )
            return intent
        }

        fun getUserInstance(context: Context):Intent{
            val intent = Intent(context.applicationContext,
                ActivityHome::class.java)
            intent.putExtra(
                EXTRA_USER,
                EXTRA_USER
            )
            return intent
        }
    }
}
