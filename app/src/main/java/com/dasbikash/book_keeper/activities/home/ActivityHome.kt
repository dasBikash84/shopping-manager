package com.dasbikash.book_keeper.activities.home

import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.OnceSettableBoolean
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_network_monitor.NetworkStateListener
import com.dasbikash.book_keeper.BuildConfig
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.expense_entry.ActivityExpenseEntry
import com.dasbikash.book_keeper.activities.expense_entry.add_exp.FragmentExpAddEdit
import com.dasbikash.book_keeper.activities.home.account.FragmentAccount
import com.dasbikash.book_keeper.activities.home.exp_summary.FragmentExpBrowser
import com.dasbikash.book_keeper.activities.home.shopping_list.FragmentShoppingList
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.bg_tasks.ShoppingListReminderScheduler
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ConnectionRequestRepo
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
                R.id.bmi_account -> {
                    loadFragmentIfNotLoadedAlready(FragmentAccount::class.java)
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

        bottom_Navigation_View.menu.findItem(R.id.bmi_account).isVisible = AuthRepo.checkLogIn()
    }

    private fun dataSyncTask(){
        if (!dataSynced.get()) {
            dataSynced.set()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    ExpenseRepo.syncData(this@ActivityHome)
                    ShoppingListRepo.syncShoppingListData(this@ActivityHome)
                    ShoppingListRepo.syncSlShareRequestData(this@ActivityHome)
                    ConnectionRequestRepo.syncData(this@ActivityHome)
                    AuthRepo.syncUserData(this@ActivityHome)
                    if (BuildConfig.DEBUG) {
                        runOnUiThread({ showShortSnack("Data sync done!!") })
                    }
                    ShoppingListReminderScheduler.runReminderScheduler(this@ActivityHome)
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
        if (AuthRepo.checkLogIn() && AuthRepo.isVerified()) {
            NetworkMonitor
                .runWithNetwork(this@ActivityHome, { dataSyncTask() })
                .let {
                    if (!it) {
                        NetworkMonitor.addNetworkStateListener(
                            NetworkStateListener.getInstance(
                                doOnConnected = { dataSyncTask() },
                                lifecycleOwner = this@ActivityHome
                            )
                        )
                    } else {
                        debugLog("Settings sync routine ran.")
                    }
                }
        }
    }

    private fun <T:FragmentTemplate> loadFragmentIfLoggedIn(type:Class<T>){
        getWaitForVerificationFragment()?.let {
            addFragmentClearingBackStack(it)
            return
        }
        if (AuthRepo.checkLogIn()){
            loadFragmentIfNotLoadedAlready(type)
        }else{
            addFragmentClearingBackStack(FragmentLogInLauncher.getInstance(type.newInstance().getPageTitle(this)))
        }
    }

    private fun <T:FragmentTemplate> loadFragmentIfNotLoadedAlready(type:Class<T>){
        getWaitForVerificationFragment()?.let {
            addFragmentClearingBackStack(it)
            return
        }
        if (getCurrentFragmentType() != type) {
            addFragmentClearingBackStack(type.newInstance())
        }
    }

    override fun registerDefaultFragment(): FragmentTemplate {
        bottom_Navigation_View.selectedItemId = R.id.bmi_add
        getWaitForVerificationFragment()?.let {
            return it
        }
        return FragmentExpAddEdit()
    }

    private fun getWaitForVerificationFragment():FragmentWaitForVerification?{
        if (!AuthRepo.isVerified()){
            return FragmentWaitForVerification()
        }
        return null
    }

    fun loadHomeFragment(){
        addFragmentClearingBackStack(getDefaultFragment())
    }
}
