package com.dasbikash.book_keeper.activities.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.android_network_monitor.initNetworkMonitor
import com.dasbikash.async_manager.AsyncTaskManager
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.ActivityHome
import com.dasbikash.book_keeper.activities.login.ActivityLogin
import com.dasbikash.book_keeper_repo.AuthRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityLauncher : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        initApp()
    }

    private fun initApp() {
        debugLog("initApp")
        initNetworkMonitor()
        AsyncTaskManager.init()
//        LoggerUtils.init(BuildConfig.DEBUG)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        lifecycleScope.launch {
            delay(500L)
            loadRequiredActivity()
        }
    }

    private fun loadRequiredActivity() {
        isLoggedIn().let {
            finish()
            if (it) {
                startActivity(ActivityHome::class.java)
            } else {
                startActivity(ActivityLogin::class.java)
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        return AuthRepo.checkLogIn()
    }
}
