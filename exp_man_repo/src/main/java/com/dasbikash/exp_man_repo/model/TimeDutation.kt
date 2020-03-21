package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep
import com.dasbikash.exp_man_repo.utils.*
import java.util.*

@Keep
enum class TimeDutation {
    DAY,WEEK,MONTH
}

@Keep
data class TimeWiseExpenseEntryGroup(
    val startTime: Date,
    val timeDutation:TimeDutation,
    val expenseEntryIds:List<String>,
    val totalExpense:Double
){
    companion object{
        fun getStartTime(date: Date, timeDutation: TimeDutation):Date{
            return when(timeDutation){
                TimeDutation.DAY -> date.getStart()
                TimeDutation.WEEK -> date.getFirstDayOfWeek().getStart()
                TimeDutation.MONTH -> date.getFirstDayOfMonth().getStart()
            }
        }

        fun getEndTime(date: Date, timeDutation: TimeDutation):Date{
            return when(timeDutation){
                TimeDutation.DAY -> date.getEnd()
                TimeDutation.WEEK -> date.getLastDayOfWeek().getEnd()
                TimeDutation.MONTH -> date.getLastDayOfMonth().getEnd()
            }
        }
    }
}