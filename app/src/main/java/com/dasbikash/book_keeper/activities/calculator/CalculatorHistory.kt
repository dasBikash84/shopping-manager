package com.dasbikash.book_keeper.activities.calculator

import android.content.Context
import androidx.annotation.Keep
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import java.io.Serializable

@Keep
data class CalculatorHistory(
    var leftOperand:Double?=null,
    var rightOperand:Double?=null,
    var result:Double?=null,
    var operation:String?=null,
    var time:Long = System.currentTimeMillis()
): Serializable {
    companion object{
        private const val CAL_HISTORY_SP_KEY = "com.dasbikash.exp_man.activities.calculator.CAL_HISTORY_SP_KEY"

        fun clearHistory(context: Context) =
            SharedPreferenceUtils.getDefaultInstance().removeKey(context, CAL_HISTORY_SP_KEY)

        suspend fun saveHistory(context: Context,
                        leftOperand:Double,
                        rightOperand:Double,
                        result:Double,
                        operation:CalculatorViewModel.Companion.CalculatorTask){
            val history = CalculatorHistory(leftOperand, rightOperand,result,operation.sign)
            val allHistories = mutableListOf<CalculatorHistory>()
            getAllHistories(context)?.let { allHistories.addAll(it) }
            allHistories.add(history)
            SharedPreferenceUtils
                .getDefaultInstance()
                .saveSerializableCollection(context,allHistories, CAL_HISTORY_SP_KEY)
        }

        suspend fun getAllHistories(context: Context):Collection<CalculatorHistory>?{
            return SharedPreferenceUtils
                        .getDefaultInstance()
                        .getSerializableCollectionSuspended(context,CalculatorHistory::class.java,CAL_HISTORY_SP_KEY)?.sortedByDescending { it.time }
        }
    }
}