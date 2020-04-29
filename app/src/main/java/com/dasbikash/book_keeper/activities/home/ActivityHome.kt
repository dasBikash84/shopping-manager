package com.dasbikash.book_keeper.activities.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.expense_entry.ActivityExpenseEntry
import com.dasbikash.book_keeper.activities.expense_entry.add_exp.FragmentExpAddEdit
import com.dasbikash.book_keeper.activities.home.account.FragmentAccount
import com.dasbikash.book_keeper.activities.home.exp_summary.FragmentExpBrowser
import com.dasbikash.book_keeper.activities.home.shopping_list.FragmentShoppingList
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.BookKeeperRepo
import com.dasbikash.book_keeper_repo.ExpenseRepo
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.launch

class ActivityHome : ActivityTemplate() {

//    private var dataSynced = OnceSettableBoolean()

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
                R.id.bmi_exp_browse -> {
                    loadFragmentIfNotLoadedAlready(FragmentExpBrowser::class.java)
                    true
                }
                /*R.id.bmi_budget -> {
                    loadFragmentIfLoggedIn(FragmentBudget::class.java)
                    true
                }*/
                R.id.bmi_note_pad -> {
                    loadFragmentIfLoggedIn(FragmentNotePad::class.java)
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

        btn_add_exp_entry.setOnClickListener {
            startActivity(ActivityExpenseEntry.getAddIntent(this))
        }

        runGuestDataImporter()
    }

    private fun runGuestDataImporter() {
        if (BookKeeperRepo.isGuestDataImportEnabled(this) && AuthRepo.isVerified()) {
            lifecycleScope.launch {
                ExpenseRepo.getGuestData(this@ActivityHome).let {
                    if (it.isNotEmpty()) {

                        val dialogView = LayoutInflater.from(this@ActivityHome).inflate(R.layout.view_guest_entry_import_dialog,null,false)
                        val checkBox = dialogView.findViewById<CheckBox>(R.id.cb_never_show)
                        val btnCancel = dialogView.findViewById<Button>(R.id.btn_launch_import_cancel)
                        val btnLaunchImportWindow = dialogView.findViewById<Button>(R.id.btn_launch_import_window)

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

                        btnCancel.setOnClickListener {
                            dialog.dismiss()
                        }

                        btnLaunchImportWindow.setOnClickListener {
                            dialog.dismiss()
                            addFragment(FragmentHandleGuestData())
                        }
                    }
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
        val fragment = when{
            isProfileIntent() -> {
                bottom_Navigation_View.selectedItemId = R.id.bmi_account
                FragmentAccount()
            }
            isConnectionIntent() -> {
                bottom_Navigation_View.selectedItemId = R.id.bmi_account
                FragmentAccount.getConnectionModeInstance()
            }
            isShoppingListIntent() -> {
                bottom_Navigation_View.selectedItemId = R.id.bmi_shopping_list
                FragmentShoppingList.getNewShoppingListInstance(getShoppingListIdExtra())
            }
            isShoppingListRequestIntent() -> {
                bottom_Navigation_View.selectedItemId = R.id.bmi_shopping_list
                FragmentShoppingList.getShoppingListRequestModeInstance()
            }
            isExpenseBrowseIntent() -> {
                bottom_Navigation_View.selectedItemId = R.id.bmi_exp_browse
                FragmentExpBrowser()
            }
            else -> {
                bottom_Navigation_View.selectedItemId = R.id.bmi_exp_browse
                FragmentExpBrowser()
            }
        }

        getWaitForVerificationFragment()?.let {
            return it
        }

        return fragment
    }

    private fun getWaitForVerificationFragment():FragmentWaitForVerification?{
        if (AuthRepo.checkLogIn() && !AuthRepo.isVerified()){
            return FragmentWaitForVerification()
        }
        return null
    }

    fun loadExpAddFragment(){
        addFragmentClearingBackStack(FragmentExpAddEdit())
    }

    private fun isProfileIntent() = intent.hasExtra(EXTRA_PROFILE_MODE)
    private fun isConnectionIntent() = intent.hasExtra(EXTRA_CONNECTION_MODE)
    private fun isShoppingListIntent() = intent.hasExtra(EXTRA_SHOPPING_LIST_MODE)
    private fun isShoppingListRequestIntent() = intent.hasExtra(EXTRA_SHOPPING_LIST_REQUEST_MODE)
    private fun isExpenseBrowseIntent() = intent.hasExtra(EXTRA_EXP_BROWSE_MODE)
    private fun getShoppingListIdExtra() = intent.getSerializableExtra(EXTRA_SHOPPING_LIST_ID) as String?

    companion object{

        private const val EXTRA_EXP_BROWSE_MODE = "com.dasbikash.book_keeper.activities.home.ActivityHome.EXTRA_EXP_BROWSE_MODE"
        private const val EXTRA_PROFILE_MODE = "com.dasbikash.book_keeper.activities.home.ActivityHome.EXTRA_PROFILE_MODE"
        private const val EXTRA_CONNECTION_MODE = "com.dasbikash.book_keeper.activities.home.ActivityHome.EXTRA_CONNECTION_MODE"
        private const val EXTRA_SHOPPING_LIST_MODE = "com.dasbikash.book_keeper.activities.home.ActivityHome.EXTRA_SHOPPING_LIST_MODE"
        private const val EXTRA_SHOPPING_LIST_ID = "com.dasbikash.book_keeper.activities.home.ActivityHome.EXTRA_SHOPPING_LIST_ID"
        private const val EXTRA_SHOPPING_LIST_REQUEST_MODE = "com.dasbikash.book_keeper.activities.home.ActivityHome.EXTRA_SHOPPING_LIST_REQUEST_MODE"

        private fun getIntent(context: Context): Intent = Intent(context.applicationContext,ActivityHome::class.java)

        fun getProfileIntent(context: Context): Intent {
            val intent = getIntent(context)
            intent.putExtra(EXTRA_PROFILE_MODE,EXTRA_PROFILE_MODE)
            return intent
        }

        fun getConnectionIntent(context: Context): Intent {
            val intent = getIntent(context)
            intent.putExtra(EXTRA_CONNECTION_MODE,EXTRA_CONNECTION_MODE)
            return intent
        }

        fun getShoppingListIntent(context: Context,shoppingListId:String?): Intent {
            val intent = getIntent(context)
            intent.putExtra(EXTRA_SHOPPING_LIST_MODE,EXTRA_SHOPPING_LIST_MODE)
            intent.putExtra(EXTRA_SHOPPING_LIST_ID,shoppingListId)
            return intent
        }

        fun getShoppingListRequestIntent(context: Context): Intent {
            val intent = getIntent(context)
            intent.putExtra(EXTRA_SHOPPING_LIST_REQUEST_MODE,EXTRA_SHOPPING_LIST_REQUEST_MODE)
            return intent
        }

        fun getExpenseBrowseIntent(context: Context): Intent {
            val intent = getIntent(context)
            intent.putExtra(EXTRA_EXP_BROWSE_MODE,EXTRA_EXP_BROWSE_MODE)
            return intent
        }
    }
}
