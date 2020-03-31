package com.dasbikash.book_keeper.activities.home

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.OnceSettableBoolean
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.show
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_network_monitor.NetworkStateListener
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.BuildConfig
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.add_exp.FragmentAddExp
import com.dasbikash.book_keeper.activities.home.exp_summary.FragmentExpBrowser
import com.dasbikash.book_keeper.activities.home.shopping_list.FragmentShoppingList
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.SettingsRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.menu_view.attachMenuViewForClick
import com.dasbikash.snackbar_ext.showShortSnack
import com.dasbikash.super_activity.SingleFragmentSuperActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityHome : SingleFragmentSuperActivity(),WaitScreenOwner {

    private var dataSynced = OnceSettableBoolean()

    override fun getDefaultFragment(): Fragment = generateDefaultFragment()

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
        if (!dataSynced.get()) {
            dataSynced.set()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    SettingsRepo.syncSettings(this@ActivityHome)
                    ExpenseRepo.syncData(this@ActivityHome)
                    ShoppingListRepo.syncData(this@ActivityHome)
                    if (BuildConfig.DEBUG) {
                        runOnUiThread({ showShortSnack("Data sync done!!") })
                    }
                } catch (ex: Throwable) {
                    dataSynced = OnceSettableBoolean()
                    ex.printStackTrace()
                    if (BuildConfig.DEBUG) {
                        runOnUiThread({ showShortSnack("Data sync failure!!") })
                    }
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
            showHideOptionsMenu(fragment)
            addFragmentClearingBackStack(fragment){page_title.text = getString((fragment as FragmentHome).getPageTitleId())}
        }
    }

    private fun <T:Fragment> loadFragmentIfNotLoadedAlready(type:Class<T>){
        showWaitScreen()
        if (getCurrentFragmentType() != type) {
            val fragment = type.newInstance()
            showHideOptionsMenu(fragment)
            addFragmentClearingBackStack(fragment){
                page_title.text = getString((fragment as FragmentHome).getPageTitleId())
                hideWaitScreen()
            }
        }
    }

    fun loadHomeFragment(){
        showWaitScreen()
        val fragment = getDefaultFragment()
        showHideOptionsMenu(fragment)
        addFragmentClearingBackStack(fragment){
            page_title.text = getString((fragment as FragmentHome).getPageTitleId())
            hideWaitScreen()
        }
    }

    private fun generateDefaultFragment():Fragment{
        val fragment = FragmentAddExp()
        showHideOptionsMenu(fragment)
        return fragment
    }

    private fun showHideOptionsMenu(fragment: Fragment){
        if (fragment is FragmentHome &&
            fragment.getOptionsMenu(this)!=null){
            btn_options.show()
            btn_options.attachMenuViewForClick(fragment.getOptionsMenu(this)!!)
        }else{
            btn_options.hide()
        }
    }
}
