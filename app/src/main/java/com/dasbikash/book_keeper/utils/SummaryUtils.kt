package com.dasbikash.book_keeper.utils

import android.content.Context
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ExpenseRepo
import java.util.*

object SummaryUtils {

    suspend fun getExpenseSummaryText(context: Context,startTime:Date,endTime:Date):String{
        val user = AuthRepo.getUser(context)!!
        val expenseEntries = ExpenseRepo.findByPeriod(context,startTime,endTime)

        val startPeriodText = startTime.getTimeString(context.getString(R.string.exp_entry_time_format))
        val endPeriodText = endTime.getTimeString(context.getString(R.string.exp_entry_time_format))

        val summaryPayload = StringBuilder(context.getString(R.string.exp_sum_page_title))
        summaryPayload.append(context.getString(R.string.exp_sum_user_name,user.displayText()))
        summaryPayload.append(context.getString(R.string.exp_sum_period,startPeriodText,endPeriodText))
        summaryPayload.append(context.getString(R.string.exp_sum_amount,expenseEntries.sumByDouble { it.totalExpense ?: 0.0 }))
        summaryPayload.append(context.getString(R.string.exp_sum_exp_count,expenseEntries.size))

        summaryPayload.append(context.getString(R.string.exp_sum_table_title))

        var serial = 0
        expenseEntries.asSequence().forEach {
            val expenseEntry = it
            serial++
            if (expenseEntry.expenseItems?.size == 0){
                summaryPayload.append(
                    context.getString(
                        R.string.exp_sum_table_row_no_product,
                        serial,
                        getDoubleQuotedString(context.resources.getStringArray(R.array.expense_categories).get(expenseEntry.categoryId)),
                        getDoubleQuotedString(expenseEntry.time?.getTimeString(context.getString(R.string.exp_entry_time_format))),
                        getDoubleQuotedString(expenseEntry.details),
                        expenseEntry.totalExpense ?: 0.0,
                        expenseEntry.taxVat
                    )
                )
            }else{
                var productSerial = 0
                expenseEntry.expenseItems?.asSequence()?.forEach {
                    productSerial++
                    if (productSerial==1){
                        summaryPayload.append(
                            context.getString(
                                R.string.exp_sum_table_row_with_product,
                                serial,
                                getDoubleQuotedString(context.resources.getStringArray(R.array.expense_categories).get(expenseEntry.categoryId)),
                                getDoubleQuotedString(expenseEntry.time?.getTimeString(context.getString(R.string.exp_entry_time_format))),
                                getDoubleQuotedString(expenseEntry.details),
                                expenseEntry.totalExpense ?: 0.0,
                                expenseEntry.taxVat,
                                getDoubleQuotedString(it.name),
                                getDoubleQuotedString(it.brandName),
                                it.unitPrice,
                                it.qty,
                                it.unitPrice*it.qty,
                                getDoubleQuotedString(context.resources.getStringArray(R.array.uoms).get(it.uom))
                            )
                        )
                    }else{
                        summaryPayload.append(
                            context.getString(
                                R.string.exp_sum_table_row_only_product,
                                getDoubleQuotedString(it.name),
                                getDoubleQuotedString(it.brandName),
                                it.unitPrice,
                                it.qty,
                                it.unitPrice*it.qty,
                                getDoubleQuotedString(context.resources.getStringArray(R.array.uoms).get(it.uom))
                            )
                        )
                    }
                }
            }
        }
        return summaryPayload.toString()
    }

    private fun getDoubleQuotedString(string: String?):String{
        string?.let {
            return "\"$it\""
        }
        return ""
    }
}