package com.dasbikash.book_keeper.activities.home.account.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.rv_helpers.EventNotificationAdapter
import com.dasbikash.book_keeper.rv_helpers.EventNotificationRemoveCallback
import com.dasbikash.book_keeper_repo.EventNotificationRepo
import com.dasbikash.book_keeper_repo.model.EventNotification
import kotlinx.android.synthetic.main.fragment_event_notification.*
import kotlinx.coroutines.launch

class FragmentEventNotification : Fragment() {

    private val eventNotificationAdapter = EventNotificationAdapter({doOnClick(it)})

    private fun doOnClick(eventNotification: EventNotification) {
        debugLog(eventNotification)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_event_notification.adapter = eventNotificationAdapter
        runWithContext {
            EventNotificationRepo
                .findAllLd(it)
                .observe(this,object : Observer<List<EventNotification>>{
                    override fun onChanged(list: List<EventNotification>?) {
                        (list ?: emptyList()).let {
                            eventNotificationAdapter.submitList(it.sortedByDescending{it.created})
                        }
                    }
                })
        }

        ItemTouchHelper(EventNotificationRemoveCallback({removeEventNotification(it)})).attachToRecyclerView(rv_event_notification)
    }

    private fun removeEventNotification(eventNotification: EventNotification) {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.confirm_delete_prompt),
                doOnPositivePress = {
                    lifecycleScope.launch { EventNotificationRepo.delete(it,eventNotification) }
                },
                doOnNegetivePress = {
                    eventNotificationAdapter.notifyDataSetChanged()
                },
                isCancelable = false
            ))
        }
    }

}
