package com.dasbikash.book_keeper.activities.launcher

import android.content.Intent
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
import com.dasbikash.book_keeper_repo.*
import kotlinx.coroutines.*

class ActivityLauncher : AppCompatActivity() {

    private val dataSyncRunning = OnceSettableBoolean()

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
//        LoggerUtils.init(BuildConfig.DEBUG)
    }

    fun processIntent() {

        val fcmSubject = getFcmSubject()?.apply {
            debugLog("FCM subject: $this")
        }

        val fcmKey = getFcmKey()?.apply {
            debugLog("FCM key: $this")
        }

        val intent: Intent? = fcmSubject?.let {
            BookKeeperMessagingService.resolveIntent(this,it,fcmKey)
        }

//        if (!dataSyncRunning.get()) {
            lifecycleScope.launch {
                do {
                    try {
                        NetworkMonitor.isConnected() //to check if net monitor has initialized.
                        delay(100L) // delay for network status read
                        syncAppDataAndForward(intent)
                        break
                    }catch (ex:Throwable){
                        ex.printStackTrace()
                        delay(50L)
                    }
                }while (true)
            }
//        }
    }

    private fun loadRequiredActivity(intent: Intent?,delay: Long=0L) {
        runOnMainThread({
            isLoggedIn().let {
                if (it) {
                    if (intent!=null){
                        startActivity(intent)
                    }else {
                        startActivity(ActivityHome::class.java)
                    }
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
        val waitForSync:Boolean = intent!=null
        dataSyncRunning.set()
        GlobalScope.launch(Dispatchers.IO) {
            if (AuthRepo.checkLogIn() && AuthRepo.isVerified()) {
                try {
                    DataSyncService.syncAppData(this@ActivityLauncher)
                    ShoppingListReminderScheduler.runReminderScheduler(this@ActivityLauncher)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    debugLog("Data sync failure!!")
                }
            }
            if (waitForSync) {
                loadRequiredActivity(intent)
            }
        }
        if (!waitForSync) {
            loadRequiredActivity(null,500L)
        }
    }

    private fun getFcmSubject():String? = intent?.getStringExtra(BookKeeperMessagingService.KEY_FCM_SUBJECT)
    private fun getFcmKey():String? = intent?.getStringExtra(BookKeeperMessagingService.KEY_FCM_KEY)
}
