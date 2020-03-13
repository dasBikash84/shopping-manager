package com.dasbikash.exp_man.utils

import android.content.Context
import com.dasbikash.android_network_monitor.NetworkMonitor

class AsyncUtils {
    companion object{
        fun <T> runWithNetwork(task:()->T?,context: Context):Boolean{
            if (NetworkMonitor.isConnected()){
                task()
                return true
            }else{
                NetworkMonitor.showNoInternetToastAnyWay(context)
                return false
            }
        }
    }
}