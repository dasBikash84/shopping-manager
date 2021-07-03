package com.dasbikash.book_keeper.utils

import android.content.Context
import android.os.Environment
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.async_manager.runSuspended
import com.dasbikash.book_keeper.R
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import java.io.File
import java.util.*

object FileExportUtils {

    private const val APP_DATA_PATH_SP_KEY =
        "com.dasbikash.book_keeper.utils.FileExportUtils.APP_DATA_PATH_SP_KEY"

    private const val APP_DATA_DIR_NAME = "book_keeper"
    private const val DATA_EXPORT_DIR_NAME = "exports"
    private const val DATE_FORMAT_FOR_EXPORT_FILE_NAME = "yyyyMMdd_HHmm"

    suspend fun getAppDataDir(context: Context):File?{
        return runSuspended {
            val spu = SharedPreferenceUtils.getDefaultInstance()
            if (spu.checkIfExists(context, APP_DATA_PATH_SP_KEY)){
                val file = File(spu.getData(context, APP_DATA_PATH_SP_KEY,String::class.java)!!)
                if (!file.exists()){
                    file.mkdirs()
                }
                debugLog("file.exists(): ${file.absolutePath}")
                return@runSuspended file
            }

            val externalFilesDir: File = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
            debugLog("externalFilesDir: ${externalFilesDir.absolutePath}")
            var rootDir:File?= externalFilesDir.parentFile
            do {
                debugLog("rootDir: ${rootDir?.absolutePath}")
                if (rootDir?.parentFile!=null && rootDir.parentFile?.isDirectory ?: false){
                    rootDir = rootDir.parentFile
                    if (rootDir.list()?.contains("Android") ?: false ||
                        rootDir.list()?.contains("Download") ?: false ||
                        rootDir.list()?.contains("Documents") ?: false ){
                        break
                    }
                }else {
                    break
                }
            }while(true)
            if (rootDir!=null){
                debugLog("rootDir!=null: ${rootDir.absolutePath}")
                val appDataDir = File("${rootDir.absolutePath}/$APP_DATA_DIR_NAME")
                debugLog("appDataDir: ${appDataDir.absolutePath}")
                if (!appDataDir.exists()){
                    debugLog("!appDataDir.exists()")
                    appDataDir.mkdir()
                }
                spu.saveDataSync(context,appDataDir.absolutePath, APP_DATA_PATH_SP_KEY)
                debugLog("returning: ${appDataDir.absolutePath}")
                return@runSuspended appDataDir
            }
            debugLog("returning: null")
            return@runSuspended null
        }
    }

    private suspend fun getDataExportDir(context: Context):File?{
        getAppDataDir(context)?.let {
            val exportDir = File("${it.absolutePath}/$DATA_EXPORT_DIR_NAME")
            if (!exportDir.exists()){
                exportDir.mkdir()
            }
            return exportDir
        }
        return null
    }

    private fun getExportFileName(context: Context) =
        context.getString(
            R.string.exp_sum_file_name,
            DateUtils.getTimeString(Date(),DATE_FORMAT_FOR_EXPORT_FILE_NAME)
        )

    suspend fun getExpenseDataExportFile(context: Context):File{
        val exportsDir = getDataExportDir(context)!!
        val fileName = getExportFileName(context)
        return File(exportsDir,fileName)
    }

    fun getFilePath(file: File):String?{
        file.absolutePath.indexOf(APP_DATA_DIR_NAME).let {
            if (it!=-1){
                return file.absolutePath.substring(it)
            }else{
                return null
            }
        }
    }
}