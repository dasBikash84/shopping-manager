package com.dasbikash.book_keeper.activities.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.OnceSettableBoolean
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_network_monitor.initNetworkMonitor
import com.dasbikash.async_manager.AsyncTaskManager
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.ActivityHome
import com.dasbikash.book_keeper.activities.login.ActivityLogin
import com.dasbikash.book_keeper.bg_tasks.ShoppingListReminderScheduler
import com.dasbikash.book_keeper.fcm.BookKeeperMessagingService
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ConnectionRequestRepo
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import kotlinx.coroutines.*

class ActivityLauncher : AppCompatActivity() {

    private val dataSyncRunning = OnceSettableBoolean()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        initApp()
    }

    private fun initApp() {
        debugLog("initApp")
        initNetworkMonitor()
        AsyncTaskManager.init()
        BookKeeperMessagingService.init(this)
//        LoggerUtils.init(BuildConfig.DEBUG)
    }

    override fun onResume() {
        super.onResume()
        if (!dataSyncRunning.get()) {
            lifecycleScope.launch {
                do {
                    try {
                        NetworkMonitor.isConnected() //to check if net monitor has initialized.
                        delay(100L) // delay for network status read
                        syncAppDataAndForward()
                        break
                    }catch (ex:Throwable){
                        ex.printStackTrace()
                        delay(50L)
                    }
                }while (true)
            }
        }
    }

    private fun loadRequiredActivity(delay: Long=0L) {
        runOnMainThread({
            isLoggedIn().let {
                if (it) {
                    startActivity(ActivityHome::class.java)
                } else {
                    startActivity(ActivityLogin::class.java)
                }
                finish()
            }
        },delay)
    }

    private fun isLoggedIn(): Boolean {
        return AuthRepo.checkLogIn()
    }

    private fun syncAppDataAndForward() {
        var waitForSync:Boolean = false
        NetworkMonitor
            .runWithNetwork(this@ActivityLauncher, { dataSyncTask(waitForSync) })
            .let {
                if (!it) {
                    lifecycleScope.launch {
                        loadRequiredActivity(500L)
                    }
                }
            }
    }

    private fun dataSyncTask(waitForSync:Boolean){
        dataSyncRunning.set()
        GlobalScope.launch(Dispatchers.IO) {
            if (AuthRepo.checkLogIn() && AuthRepo.isVerified()) {
                try {
                    debugLog("starting Data sync!!")
                    ExpenseRepo.syncData(this@ActivityLauncher)
                    ShoppingListRepo.syncShoppingListData(this@ActivityLauncher)
                    ShoppingListRepo.syncSlShareRequestData(this@ActivityLauncher)
                    ConnectionRequestRepo.syncData(this@ActivityLauncher)
                    AuthRepo.syncUserData(this@ActivityLauncher)
                    ShoppingListReminderScheduler.runReminderScheduler(this@ActivityLauncher)
                    debugLog("Data sync done!!")
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    debugLog("Data sync failure!!")
                }
            }
            if (waitForSync) {
                loadRequiredActivity()
            }
        }
        if (!waitForSync) {
            loadRequiredActivity(500L)
        }
    }
}
