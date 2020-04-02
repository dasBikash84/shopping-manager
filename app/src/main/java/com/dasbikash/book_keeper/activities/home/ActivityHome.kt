package com.dasbikash.book_keeper.activities.home

import android.os.Bundle
import android.os.PersistableBundle
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.OnceSettableBoolean
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_network_monitor.NetworkStateListener
import com.dasbikash.book_keeper.BuildConfig
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.expense_entry.ActivityExpenseEntry
import com.dasbikash.book_keeper.activities.expense_entry.add_exp.FragmentExpAddEdit
import com.dasbikash.book_keeper.activities.home.exp_summary.FragmentExpBrowser
import com.dasbikash.book_keeper.activities.home.shopping_list.FragmentShoppingList
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityHome : ActivityTemplate() {

    private var dataSynced = OnceSettableBoolean()

    override fun getLayoutID(): Int = R.layout.activity_home
    override fun getLoneFrameId(): Int = R.id.home_frame

    override fun onResume() {
        super.onResume()
        bottom_Navigation_View.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bmi_add -> {
                    loadFragmentIfNotLoadedAlready(FragmentExpAddEdit::class.java)
                    true
                }
                R.id.bmi_home -> {
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
        btn_add_exp_entry.setOnClickListener {
            startActivity(ActivityExpenseEntry.getAddIntent(this))
        }
    }

    private fun dataSyncTask(){
        if (!dataSynced.get()) {
            dataSynced.set()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
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

    private fun <T:FragmentTemplate> loadFragmentIfLoggedIn(type:Class<T>){
        if (AuthRepo.checkLogIn()){
            loadFragmentIfNotLoadedAlready(type)
        }else{
            addFragmentClearingBackStack(FragmentLogInLauncher())
        }
    }

    private fun <T:FragmentTemplate> loadFragmentIfNotLoadedAlready(type:Class<T>){
        if (getCurrentFragmentType() != type) {
            addFragmentClearingBackStack(type.newInstance())
        }
    }

    fun loadHomeFragment(){
        addFragmentClearingBackStack(getDefaultFragment())
    }

    override fun registerDefaultFragment(): FragmentTemplate {
        bottom_Navigation_View.selectedItemId = R.id.bmi_home
        return FragmentExpBrowser()
    }
}
