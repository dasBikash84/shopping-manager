package com.dasbikash.book_keeper_repo.bg_tasks

import android.content.Context
import androidx.work.*
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.ImageRepo
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

internal object ImageUploaderTaskUtils {

    private const val PENDING_IMAGE_UPLOAD_WORK_ID_SP_KEY =
        "com.dasbikash.book_keeper_repo.bg_tasks.ImageUploaderTaskUtils.PENDING_IMAGE_UPLOAD_WORK_ID_SP_KEY"

    fun initTask(context: Context){
        if (!checkIfWorkScheduled(context)){
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest =
                OneTimeWorkRequestBuilder<PendingImageUploadWork>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                        TimeUnit.MILLISECONDS
                    ).build()
            WorkManager.getInstance(context).enqueue(workRequest)
            GlobalScope.launch { saveWorkId(context,workRequest.id.toString())}
        }
    }

    private suspend fun saveWorkId(context: Context,workId:String){
        SharedPreferenceUtils.getDefaultInstance()
            .saveDataSuspended(context,workId, PENDING_IMAGE_UPLOAD_WORK_ID_SP_KEY)
    }

    fun clearWorkId(context: Context){
        SharedPreferenceUtils.getDefaultInstance().removeKey(context,
            PENDING_IMAGE_UPLOAD_WORK_ID_SP_KEY)
    }

    private fun checkIfWorkScheduled(context: Context):Boolean{
        return SharedPreferenceUtils.getDefaultInstance().checkIfExists(context,
            PENDING_IMAGE_UPLOAD_WORK_ID_SP_KEY)
    }
}


class PendingImageUploadWork(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        return runBlocking {
            ImageRepo
                .getAllPendingUploadInfo(applicationContext,System.currentTimeMillis()- MODIFIED_DELAY_THRESHOLD)
                .asSequence()
                .forEach {
                    debugLog(it)
                    ImageRepo.scheduleImageUpload(null,it,applicationContext)
                }
            ImageRepo.getPendingImageUploadCount(applicationContext).let {
                debugLog("PendingImageUploadCount: $it")
                if (it==0) {
                    ImageUploaderTaskUtils.clearWorkId(applicationContext)
                    debugLog("PendingImageUploadWork: Success")
                    Result.success()
                }else {
                    debugLog("PendingImageUploadWork: Retry")
                    Result.retry()
                }
            }
        }
    }
    companion object{
        private const val MODIFIED_DELAY_THRESHOLD = 15*DateUtils.MINUTE_IN_MS
    }
}