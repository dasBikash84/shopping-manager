package com.dasbikash.book_keeper.activities.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.LoggerUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_network_monitor.initNetworkMonitor
import com.dasbikash.async_manager.AsyncTaskManager
import com.dasbikash.book_keeper.BuildConfig
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.ActivityHome
import com.dasbikash.book_keeper.activities.intro.ActivityIntro
import com.dasbikash.book_keeper.activities.login.ActivityLogin
import com.dasbikash.book_keeper.bg_tasks.ShoppingListReminderScheduler
import com.dasbikash.book_keeper.fcm.BookKeeperMessagingService
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.DataSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ActivityLauncher : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        initApp()

        lifecycleScope.launchWhenCreated {
            processIntent()
        }
    }

    private fun initApp() {
        debugLog("initApp")
        initNetworkMonitor()
        AsyncTaskManager.init()
        BookKeeperMessagingService.init(this)
        LoggerUtils.init(BuildConfig.DEBUG)
    }

    suspend fun processIntent() {
        do {
            try {
                NetworkMonitor.isConnected() //to check if net monitor has initialized.
                delay(100L) // delay for network status read
                syncAppDataAndForward(checkForIntent())
                break
            }catch (ex:Throwable){
                ex.printStackTrace()
                delay(50L)
            }
        }while (true)
    }

    private fun checkForIntent():Intent?{
        checkForFcmIntent()?.let {
            return it
        }
        if (isLoggedInIntent()){
            return ActivityHome.getExpenseBrowseIntent(this)
        }
        return null
    }

    private fun checkForFcmIntent() = BookKeeperMessagingService.checkForFcmIntent(this,intent)

    private fun syncAppDataAndForward(intent: Intent?) {
        NetworkMonitor
            .runWithNetwork(this@ActivityLauncher, { dataSyncTask(intent) })
            .let {
                if (!it) {
                    lifecycleScope.launch {
                        loadRequiredActivity(intent,500L)
                    }
                }
            }
    }

    private fun dataSyncTask(intent: Intent?){
        //Wait for sync if intent!=null
        val waitForDataSync:Boolean = intent!=null
        GlobalScope.launch(Dispatchers.IO) {
            //Don't run sync if internal notification
            if (AuthRepo.checkLogIn() &&
                AuthRepo.isVerified() &&
                !BookKeeperMessagingService.checkIfInternalNotification(intent)
            ) {
                try {
                    DataSyncService.syncAppData(this@ActivityLauncher)
                    ShoppingListReminderScheduler.runReminderScheduler(this@ActivityLauncher)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    debugLog("Data sync failure!!")
                }
            }
            if (waitForDataSync) {
                loadRequiredActivity(intent)
            }
        }
        if (!waitForDataSync) {
            loadRequiredActivity(intent,500L)
        }
    }

    private fun loadRequiredActivity(intent: Intent?,delay: Long=0L) {
        runOnMainThread({
            AuthRepo.checkLogIn().let {
                if (it) {
                    if (intent!=null){
                        startActivity(intent)
                    }else {
                        startActivity(ActivityHome::class.java)
                    }
                } else {
                    if (ActivityIntro.checkLangSelectedFlag(this) &&
                            ActivityIntro.checkAppFeaturesShownFlag(this)) {
                        startActivity(ActivityLogin::class.java)
                    }else{
                        startActivity(ActivityIntro::class.java)
                    }
                }
                finish()
            }
        },delay)
    }

    private fun isLoggedInIntent() = intent.hasExtra(EXTRA_LOGGED_IN_MODE)

    companion object {

        private const val EXTRA_LOGGED_IN_MODE =
            "com.dasbikash.book_keeper.activities.launcher.ActivityHome.ActivityLauncher"

        fun getLoggedInIntent(context: Context): Intent {
            val intent = Intent(context.applicationContext, ActivityLauncher::class.java)
            intent.putExtra(EXTRA_LOGGED_IN_MODE, EXTRA_LOGGED_IN_MODE)
            return intent
        }
    }
}
