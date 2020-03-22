package com.dasbikash.exp_man.activities.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.LoggerUtils
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_network_monitor.NetworkStateListener
import com.dasbikash.android_network_monitor.initNetworkMonitor
import com.dasbikash.exp_man.BuildConfig
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.home.ActivityHome
import com.dasbikash.exp_man.activities.login.ActivityLogin
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.exp_man_repo.SettingsRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        lifecycleScope.launch {
            delay(500L)
            if (SettingsRepo.getAllExpenseCategories(this@ActivityLauncher).isEmpty() ||
                    SettingsRepo.getAllUoms(this@ActivityLauncher).isEmpty()){
                NetworkMonitor
                    .runWithNetwork(this@ActivityLauncher,{loadSettingsAndJump()})
                    .let {
                        if (!it){
                            NetworkMonitor.addNetworkStateListener(NetworkStateListener.getInstance(
                                doOnConnected = {loadSettingsAndJump()},lifecycleOwner = this@ActivityLauncher))
                        }
                    }
            }else{
                loadRequiredActivity()
            }
        }
    }

    private fun loadSettingsAndJump() {
        lifecycleScope.launch {
            SettingsRepo.syncSettings(this@ActivityLauncher)
            loadRequiredActivity()
        }
    }

    private suspend fun loadRequiredActivity() {
        isLoggedIn().let {
            finish()
            if (it) {
                startActivity(ActivityHome::class.java)
            } else {
                startActivity(ActivityLogin::class.java)
            }
        }
    }

    private suspend fun isLoggedIn(): Boolean {
        return AuthRepo.checkLogIn(this)
    }
}
