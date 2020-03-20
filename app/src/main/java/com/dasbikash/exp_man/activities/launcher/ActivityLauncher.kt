package com.dasbikash.exp_man.activities.launcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.LoggerUtils
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_network_monitor.initNetworkMonitor
import com.dasbikash.async_manager.runSuspended
import com.dasbikash.exp_man.BuildConfig
import com.dasbikash.exp_man.activities.home.ActivityHome
import com.dasbikash.exp_man.activities.login.ActivityLogin
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.exp_man_repo.SettingsRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ActivityLauncher : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        initApp()
    }

    private fun initApp() {
        initNetworkMonitor()
        LoggerUtils.init(BuildConfig.DEBUG)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            do {
                delay(100L)
                if (!NetworkMonitor.isConnected()){
                    NetworkMonitor.showNoInternetToast(this@ActivityLauncher)
                }else{
                    break
                }
            }while (true)

            SettingsRepo.syncSettings(this@ActivityLauncher)

            isLoggedIn().let {
                delay(500)
                if (it) {
                    loadUserActivity()
                } else {
                    loadLoginActivity()
                }
            }
        }
    }

    private fun loadLoginActivity() {
        finish()
        startActivity(ActivityLogin::class.java)
    }

    private fun loadUserActivity() {
        finish()
        startActivity(
            ActivityHome.getUserInstance(
                this
            )
        )
    }

    private suspend fun isLoggedIn(): Boolean {
        return AuthRepo.checkLogIn(this)
    }
}
