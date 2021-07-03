package com.dasbikash.book_keeper.rv_helpers

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
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.get2DecPoints
import com.dasbikash.book_keeper.utils.getCurrencyStringWithSymbol
import com.dasbikash.book_keeper.utils.getTitleString
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.book_keeper_repo.model.TimeBasedExpenseEntryGroup
import io.reactivex.rxjava3.subjects.PublishSubject

object TimeBasedExpenseEntryGroupDiffCallback: DiffUtil.ItemCallback<TimeBasedExpenseEntryGroup>(){
    override fun areItemsTheSame(oldItem: TimeBasedExpenseEntryGroup, newItem: TimeBasedExpenseEntryGroup) = oldItem.startTime == newItem.startTime
    override fun areContentsTheSame(oldItem: TimeBasedExpenseEntryGroup, newItem: TimeBasedExpenseEntryGroup): Boolean {
        return oldItem==newItem
    }
}

class TimeBasedExpenseEntryGroupAdapter(
            val timePeriodTitleClickEventPublisher: PublishSubject<TimeBasedExpenseEntryGroup>,
            val launchDetailView:(ExpenseEntry)->Unit,
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
            ), timePeriodTitleClickEventPublisher,launchDetailView,editTask,deleteTask,expLiveData, lifecycleOwner
        )
    }

    override fun onBindViewHolder(holder: TimeBasedExpenseEntryGroupHolder, position: Int) {
        getItem(position)?.apply { holder.bind(this)}
    }
}

class TimeBasedExpenseEntryGroupHolder(itemView: View,
                                        val timePeriodTitleClickEventPublisher: PublishSubject<TimeBasedExpenseEntryGroup>,
                                        val launchDetailView:(ExpenseEntry)->Unit,
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

    private val expHolderAdapter = ExpenseEntryAdapter({launchDetailView(it)},{editTask(it)},{deleteTask(it)},{})

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
                    tv_total_expense.text = it.second.sumByDouble { it.totalExpense ?: 0.0 }.getCurrencyStringWithSymbol(itemView.context)
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
        tv_time_period_text.text = TranslatorUtils.getTranslatedDateString(itemView.context,timeBasedExpenseEntryGroup.getTitleString(itemView.context))
        tv_total_expense.text = timeBasedExpenseEntryGroup.totalExpense.getCurrencyStringWithSymbol(itemView.context)
        tv_exp_count.text = timeBasedExpenseEntryGroup.expenseEntryIds.size.toString()
    }
}