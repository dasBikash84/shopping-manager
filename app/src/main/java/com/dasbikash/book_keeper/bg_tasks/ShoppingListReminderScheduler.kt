package com.dasbikash.book_keeper.bg_tasks

import android.content.Context
import androidx.work.*
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.notification_utils.NotificationUtils
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit

internal object ShoppingListReminderScheduler {
    private val REMINDER_GEN_TASK_ID_SP_KEY =
        "com.dasbikash.book_keeper_repo.bg_tasks.ShoppingListReminderScheduler.REMINDER_GEN_TASK_ID_SP_KEY"

    fun runReminderScheduler(context: Context){
        debugLog("runReminderScheduler")
        GlobalScope.launch {
            cancelCurrentTask(context)
            AuthRepo.getUser(context)?.let {
                debugLog("runReminderScheduler: user: $it")
                debugLog("before findAllWithReminder")
                ShoppingListRepo
                    .findAllWithReminder(context,it)
                    .map {
                        debugLog(it)
                        ShoppingListRepo.calculateNextReminderTime(context,it).apply {
                            debugLog(this ?: "calculateNextReminderTime returned null")
                        }
                    }
                    .filter { it!=null }
                    .map { it!! }
                    .let{
                        //We want to calculate next reminder time
                        debugLog("all calculateNextReminderTime")
                        it.asSequence().forEach { debugLog(it)}
                        if (it.isNotEmpty()){
                            it.sorted().first().let {
                                debugLog("First: calculateNextReminderTime: $it")
                                scheduleNextTask(context,it)
                            }
                        }
                    }
            }
        }
    }

    private suspend fun scheduleNextTask(context: Context, date: Date) {
        debugLog("scheduleNextTask: $date")
        val constraints = Constraints.Builder().build()
        var workRequestBuilder =
            OneTimeWorkRequestBuilder<SlReminderGenWork>()
                .setConstraints(constraints)
        if (date.time > System.currentTimeMillis()){
            val delay = date.time - System.currentTimeMillis()
            debugLog("scheduleNextTask: delay: $delay")
            workRequestBuilder = workRequestBuilder.setInitialDelay(delay, TimeUnit.MILLISECONDS)
        }
        val workRequest = workRequestBuilder.build()
        WorkManager.getInstance(context).enqueue(workRequest)
        saveCurrentTaskId(context,workRequest.id.toString())
    }

    private suspend fun getCurrentTaskId(context: Context):String?{
        return SharedPreferenceUtils.getDefaultInstance()
                    .getDataSuspended(context,REMINDER_GEN_TASK_ID_SP_KEY,String::class.java)
    }
    private suspend fun saveCurrentTaskId(context: Context,taskId:String){
        return SharedPreferenceUtils.getDefaultInstance()
                    .saveDataSuspended(context,taskId,REMINDER_GEN_TASK_ID_SP_KEY)
    }

    private fun clearCurrentTaskId(context: Context){
        return SharedPreferenceUtils.getDefaultInstance()
                    .removeKey(context,REMINDER_GEN_TASK_ID_SP_KEY)
    }

    private suspend fun cancelCurrentTask(context: Context){
        debugLog("cancelCurrentTask")
        getCurrentTaskId(context)?.let {
            val workUuid = UUID.fromString(it)
            WorkManager.getInstance(context).cancelWorkById(workUuid)
            clearCurrentTaskId(context)
        }
    }
}

class SlReminderGenWork(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        debugLog("doWork")
        return runBlocking {
           AuthRepo.getUser(applicationContext)?.let {
               debugLog("doWork: user: $it")
               debugLog("before findAllWithReminder")
               ShoppingListRepo
                   .findAllWithReminder(applicationContext, it)
                   .forEach {
                       debugLog("Inside foreach: $it")
                       ShoppingListRepo.calculateNextReminderTime(applicationContext, it).apply {
                           debugLog("calculateNextReminderTime: $this")
                           if (this!=null) {
                               debugLog("System.currentTimeMillis: ${System.currentTimeMillis()}")
                               if (time < System.currentTimeMillis()) {
                                   generateReminder(it)
                               }
                           }
                       }
                   }
           }
            ShoppingListReminderScheduler.runReminderScheduler(applicationContext)
            return@runBlocking Result.success()
        }
    }

    private suspend fun generateReminder(shoppingList: ShoppingList) {
        debugLog("generateReminder: $shoppingList")
        NotificationUtils.generateNotification(
            applicationContext,
            applicationContext.getString(R.string.shopping_reminder),
            applicationContext.getString(R.string.shopping_reminder_content,shoppingList.title),
            ActivityShoppingList.getViewIntentForNotification(applicationContext,shoppingList.id),
            R.mipmap.ic_launcher
        )
        ShoppingListRepo.logShoppingReminder(applicationContext,shoppingList)
    }
}