package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import com.dasbikash.book_keeper_repo.utils.*
import java.util.*

@Keep
data class TimeBasedExpenseEntryGroup(
    val startTime: Date,
    val timeDuration: TimeDuration,
    val expenseEntryIds:List<String>,
    val totalExpense:Double
){
    companion object{
        fun getStartEndTime(date: Date,timeDuration: TimeDuration) =
            Pair(getStartTime(date, timeDuration),getEndTime(date, timeDuration))

        private fun getStartTime(date: Date, timeDuration: TimeDuration): Date {
            return when(timeDuration){
                TimeDuration.DAY -> date.getStart()
                TimeDuration.WEEK -> date.getFirstDayOfWeek().getStart()
                TimeDuration.MONTH -> date.getFirstDayOfMonth().getStart()
            }
        }

        private fun getEndTime(date: Date, timeDuration: TimeDuration): Date {
            return when(timeDuration){
                TimeDuration.DAY -> date.getEnd()
                TimeDuration.WEEK -> date.getLastDayOfWeek().getEnd()
                TimeDuration.MONTH -> date.getLastDayOfMonth().getEnd()
            }
        }
    }
}

@Keep
enum class TimeDuration {
    DAY,WEEK,MONTH
}