package com.dasbikash.exp_man.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.show
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.utils.DateTranslatorUtils
import com.dasbikash.exp_man.utils.checkIfEnglishLanguageSelected
import com.dasbikash.exp_man.utils.getTitleString
import com.dasbikash.exp_man.utils.optimizedString
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import com.dasbikash.exp_man_repo.model.TimeBasedExpenseEntryGroup
import io.reactivex.rxjava3.subjects.PublishSubject

object TimeBasedExpenseEntryGroupDiffCallback: DiffUtil.ItemCallback<TimeBasedExpenseEntryGroup>(){
    override fun areItemsTheSame(oldItem: TimeBasedExpenseEntryGroup, newItem: TimeBasedExpenseEntryGroup) = oldItem.startTime == newItem.startTime
    override fun areContentsTheSame(oldItem: TimeBasedExpenseEntryGroup, newItem: TimeBasedExpenseEntryGroup): Boolean {
        return oldItem==newItem
    }
}

class TimeBasedExpenseEntryGroupAdapter(
            val timePeriodTitleClickEventPublisher: PublishSubject<TimeBasedExpenseEntryGroup>,
            val editTask:(ExpenseEntry)->Unit,
            val deleteTask:(ExpenseEntry)->Unit,
            val expLiveData: LiveData<Pair<TimeBasedExpenseEntryGroup,List<ExpenseEntry>>>,
            val lifecycleOwner: LifecycleOwner) :
    ListAdapter<TimeBasedExpenseEntryGroup, TimeBasedExpenseEntryGroupHolder>(
        TimeBasedExpenseEntryGroupDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeBasedExpenseEntryGroupHolder {
        return TimeBasedExpenseEntryGroupHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_time_wise_exp_list, parent, false
            ), timePeriodTitleClickEventPublisher,editTask,deleteTask,expLiveData, lifecycleOwner
        )
    }

    override fun onBindViewHolder(holder: TimeBasedExpenseEntryGroupHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}

class TimeBasedExpenseEntryGroupHolder(itemView: View,
                                        val timePeriodTitleClickEventPublisher: PublishSubject<TimeBasedExpenseEntryGroup>,
                                        val editTask:(ExpenseEntry)->Unit,
                                        val deleteTask:(ExpenseEntry)->Unit,
                                        val expLiveData: LiveData<Pair<TimeBasedExpenseEntryGroup,List<ExpenseEntry>>>,
                                        val lifecycleOwner: LifecycleOwner) : RecyclerView.ViewHolder(itemView) {
    private val time_period_text_holder: ViewGroup = itemView.findViewById(R.id.time_period_text_holder)
    private val tv_time_period_text: TextView = itemView.findViewById(R.id.tv_time_period_text)
    private val tv_total_expense: TextView = itemView.findViewById(R.id.tv_total_expense)
    private val tv_exp_count: TextView = itemView.findViewById(R.id.tv_exp_count)
    private val rv_time_wise_exp_holder: ViewGroup = itemView.findViewById(R.id.rv_time_wise_exp_holder)
    private val rv_time_wise_exp: RecyclerView = itemView.findViewById(R.id.rv_time_wise_exp)

    private val expHolderAdapter = ExpenseEntryAdapter({editTask(it)},{deleteTask(it)},{})

    private lateinit var timeBasedExpenseEntryGroup: TimeBasedExpenseEntryGroup

    private val observer = object : Observer<Pair<TimeBasedExpenseEntryGroup, List<ExpenseEntry>>> {
        override fun onChanged(data: Pair<TimeBasedExpenseEntryGroup, List<ExpenseEntry>>?) {
            debugLog("onChanged")
            data?.let {
                debugLog("${data.first.startTime}")
                debugLog("${data.second.map { it.id }}")
                if (it.first == timeBasedExpenseEntryGroup) {
                    rv_time_wise_exp_holder.show()
                    expHolderAdapter.submitList(it.second)
                    tv_total_expense.text = it.second.sumByDouble { it.totalExpense ?: 0.0 }.optimizedString(2)
                    tv_exp_count.text = it.second.size.toString()
                }
            }
        }
    }

    init {
        rv_time_wise_exp.adapter = expHolderAdapter
        rv_time_wise_exp_holder.hide()

        time_period_text_holder.setOnClickListener {
            if (!rv_time_wise_exp_holder.isVisible) {
                debugLog("OnClick: ${timeBasedExpenseEntryGroup.timeDuration}")
                if (expHolderAdapter.currentList.isEmpty()) {
                    timePeriodTitleClickEventPublisher.onNext(timeBasedExpenseEntryGroup)
                    expLiveData.observe(lifecycleOwner,observer)
                }else{
                    rv_time_wise_exp_holder.show()
                }
            }else{
                rv_time_wise_exp_holder.hide()
            }
        }

        timePeriodTitleClickEventPublisher.subscribe {
            debugLog("subscribe: own: ${timeBasedExpenseEntryGroup.startTime} got: ${it.startTime}")
            if (timeBasedExpenseEntryGroup != it){
                expHolderAdapter.submitList(emptyList())
                expLiveData.removeObserver(observer)
                rv_time_wise_exp_holder.hide()
            }
        }
    }

    fun bind(timeBasedExpenseEntryGroup: TimeBasedExpenseEntryGroup) {
        this.timeBasedExpenseEntryGroup = timeBasedExpenseEntryGroup
        tv_time_period_text.text = timeBasedExpenseEntryGroup.getTitleString(itemView.context)
                                        .let {
                                            if (checkIfEnglishLanguageSelected()){
                                                it
                                            }else{
                                                DateTranslatorUtils.englishToBanglaDateString(it)
                                            }
                                        }
        tv_total_expense.text = timeBasedExpenseEntryGroup.totalExpense.optimizedString(2)
        tv_exp_count.text = timeBasedExpenseEntryGroup.expenseEntryIds.size.toString()
    }
}