package com.dasbikash.book_keeper.activities.home

import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.OnceSettableBoolean
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_network_monitor.NetworkStateListener
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.expense_entry.ActivityExpenseEntry
import com.dasbikash.book_keeper.activities.expense_entry.add_exp.FragmentExpAddEdit
import com.dasbikash.book_keeper.activities.home.account.FragmentAccount
import com.dasbikash.book_keeper.activities.home.exp_summary.FragmentExpBrowser
import com.dasbikash.book_keeper.activities.home.shopping_list.FragmentShoppingList
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.bg_tasks.ShoppingListReminderScheduler
import com.dasbikash.book_keeper_repo.*
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
                    loadFragmentIfLoggedIn(FragmentAccount::class.java)
                    true
                }
                else -> false
            }
        }
        bottom_Navigation_View.setOnNavigationItemReselectedListener { }


        lifecycleScope.launch { syncAppData()}
        btn_add_exp_entry.setOnClickListener {
            startActivity(ActivityExpenseEntry.getAddIntent(this))
        }
    }

    private fun dataSyncTask(){
        if (!dataSynced.get()) {
            dataSynced.set()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    runGuestDataImporter()
                    ExpenseRepo.syncData(this@ActivityHome)
                    ShoppingListRepo.syncShoppingListData(this@ActivityHome)
                    ShoppingListRepo.syncSlShareRequestData(this@ActivityHome)
                    ConnectionRequestRepo.syncData(this@ActivityHome)
                    AuthRepo.syncUserData(this@ActivityHome)
                    ShoppingListReminderScheduler.runReminderScheduler(this@ActivityHome)
                    debugLog("Data sync done!!")
                } catch (ex: Throwable) {
                    dataSynced = OnceSettableBoolean()
                    ex.printStackTrace()
                    debugLog("Data sync failure!!")
                }
            }
        }
    }

    private fun runGuestDataImporter() {
        if (BookKeeperRepo.isGuestDataImportEnabled(this)) {
            lifecycleScope.launch {
                ExpenseRepo.getGuestData(this@ActivityHome).let {
                    val guestEntries= it
                    if (it.isNotEmpty()) {

                        val dialogView = LayoutInflater.from(this@ActivityHome).inflate(R.layout.view_guest_entry_import_dialog,null,false)
                        val checkBox = dialogView.findViewById<CheckBox>(R.id.cb_never_show)
                        val btn_delete_guest_exp_entry = dialogView.findViewById<Button>(R.id.btn_delete_guest_exp_entry)
                        val btn_launch_import_window = dialogView.findViewById<Button>(R.id.btn_launch_import_window)

                        checkBox.setOnCheckedChangeListener({ buttonView, isChecked ->
                            if (isChecked) {
                                BookKeeperRepo.disableGuestDataImport(this@ActivityHome)
                            } else {
                                BookKeeperRepo.enableGuestDataImport(this@ActivityHome)
                            }
                        })

                        val dialog= DialogUtils.showAlertDialog(
                            this@ActivityHome, DialogUtils.AlertDialogDetails(
                                positiveButtonText = "",
                                neutralButtonText = "",
                                negetiveButtonText = "",
                                view = dialogView
                            )
                        )

                        btn_delete_guest_exp_entry.setOnClickListener {
                            dialog.dismiss()
                            guestEntries.asSequence().forEach {
                                lifecycleScope.launch {
                                    ExpenseRepo.delete(this@ActivityHome, it)
                                }
                            }
                        }

                        btn_launch_import_window.setOnClickListener {
                            dialog.dismiss()
                            addFragment(FragmentHandleGuestData())
                        }
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
        if (AuthRepo.checkLogIn() && !AuthRepo.isVerified()){
            return FragmentWaitForVerification()
        }
        return null
    }

    fun loadHomeFragment(){
        addFragmentClearingBackStack(getDefaultFragment())
    }
}
