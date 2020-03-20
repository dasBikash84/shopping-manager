package com.dasbikash.exp_man.activities.home

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.home.add_exp.FragmentAddExp
import com.dasbikash.exp_man.activities.home.exp_summary.FragmentExpSummary
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.super_activity.SingleFragmentSuperActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.launch

class ActivityHome : SingleFragmentSuperActivity(),WaitScreenOwner {

    override fun getDefaultFragment(): Fragment =
        FragmentAddExp()

    override fun getLayoutID(): Int = R.layout.activity_home

    override fun getLoneFrameId(): Int = R.id.home_frame

    override fun registerWaitScreen(): ViewGroup = wait_screen

    override fun onResume() {
        super.onResume()
        bottom_Navigation_View.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bmi_add -> {
                    loadFragmentIfNotLoadedAlready(FragmentAddExp::class.java)
                    true
                }
                R.id.bmi_summary -> {
                    loadFragmentIfNotLoadedAlready(FragmentExpSummary::class.java)
                    true
                }
                R.id.bmi_budget -> {
                    loadFragmentIfLoggedIn(FragmentBudget::class.java)
                    true
                }
                R.id.bmi_shopping_list -> {
                    loadFragmentIfLoggedIn(FragmentShoppingList::class.java)
                    true
                }
                R.id.bmi_more -> {
                    loadFragmentIfNotLoadedAlready(FragmentMore::class.java)
                    true
                }
                else -> false
            }
        }
        bottom_Navigation_View.setOnNavigationItemReselectedListener { }
    }

    private fun <T:Fragment> loadFragmentIfLoggedIn(type:Class<T>){
        lifecycleScope.launch {
            if (AuthRepo.checkLogIn(this@ActivityHome)){
                loadFragmentIfNotLoadedAlready(type)
            }else{
                addFragmentClearingBackStack(FragmentLogInLauncher())
            }
        }
    }

    private fun <T:Fragment> loadFragmentIfNotLoadedAlready(type:Class<T>){
        if (getCurrentFragmentType() != type) {
            addFragmentClearingBackStack(type.newInstance())
        }
    }

    fun loadHomeFragment(){
        addFragmentClearingBackStack(getDefaultFragment())
    }
}
