package com.dasbikash.exp_man.activities.home

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_network_monitor.NetworkStateListener
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.exp_man.BuildConfig
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.home.add_exp.FragmentAddExp
import com.dasbikash.exp_man.activities.home.exp_summary.FragmentExpBrowser
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.exp_man_repo.ExpenseRepo
import com.dasbikash.exp_man_repo.SettingsRepo
import com.dasbikash.snackbar_ext.showShortSnack
import com.dasbikash.super_activity.SingleFragmentSuperActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.Dispatchers
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
                    loadFragmentIfNotLoadedAlready(FragmentAddExp::class.java)
                    true
                }
                R.id.bmi_summary -> {
                    loadFragmentIfNotLoadedAlready(FragmentExpBrowser::class.java)
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

        syncAppData()
    }

    private fun dataSyncTask(){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                SettingsRepo.syncSettings(this@ActivityHome)
                ExpenseRepo.syncData(this@ActivityHome)
            } catch (ex: Throwable) {
                ex.printStackTrace()
                if (BuildConfig.DEBUG) {
                    runOnUiThread({ showShortSnack("Data sync failure!!") })
                }
            }
        }
    }

    private fun syncAppData() {
        NetworkMonitor
            .runWithNetwork(this,{dataSyncTask()})
            .let {
                  if (!it){
                      NetworkMonitor.addNetworkStateListener(
                          NetworkStateListener.getInstance(
                              doOnConnected = {dataSyncTask()},
                              lifecycleOwner = this
                          )
                      )
                  }else{
                      debugLog("Settings sync routine ran.")
                  }
                }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        page_title.text = getString(R.string.add_expense_title)
    }

    private fun <T:Fragment> loadFragmentIfLoggedIn(type:Class<T>){
        if (AuthRepo.checkLogIn()){
            loadFragmentIfNotLoadedAlready(type)
        }else{
            val fragment = FragmentLogInLauncher()
            addFragmentClearingBackStack(fragment){page_title.text = getString((fragment as FragmentHome).getPageTitleId())}
        }
    }

    private fun <T:Fragment> loadFragmentIfNotLoadedAlready(type:Class<T>){
        showWaitScreen()
        if (getCurrentFragmentType() != type) {
            val fragment = type.newInstance()
            addFragmentClearingBackStack(fragment){
                page_title.text = getString((fragment as FragmentHome).getPageTitleId())
                hideWaitScreen()
            }
        }
    }

    fun loadHomeFragment(){
        showWaitScreen()
        val fragment = getDefaultFragment()
        addFragmentClearingBackStack(fragment){
            page_title.text = getString((fragment as FragmentHome).getPageTitleId())
            hideWaitScreen()
        }
    }
}
