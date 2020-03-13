package com.dasbikash.exp_man.activities.launcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.async_manager.runSuspended
import com.dasbikash.exp_man.activities.home.ActivityHome
import com.dasbikash.exp_man.activities.login.ActivityLogin
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man_repo.LoginRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityLauncher : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            isLoggedIn().let {
                delay(500)
                if (it) {
                    loadUserActivity()
                } else {
                    loadGuestActivity()
                }
            }
        }
    }

    private fun loadGuestActivity() {
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
        return runSuspended { LoginRepo.checkLogIn(this)}
    }
}
