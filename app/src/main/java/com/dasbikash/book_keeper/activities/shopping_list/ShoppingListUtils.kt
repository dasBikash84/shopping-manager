package com.dasbikash.book_keeper.activities.shopping_list

import android.content.Context
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_toast_utils.ToastUtils
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.sl_share.ActivityShoppingListShare
import com.dasbikash.book_keeper.rv_helpers.ConnectionUserPreviewForDisplayAdapter
import com.dasbikash.book_keeper.rv_helpers.ConnectionUserPreviewForSlSendAdapter
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ConnectionRequestRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ShoppingListUtils {

    companion object{

        private const val ONLINE_SHARE_SUGGESTION_DISABLE_SP_KEY =
            "com.dasbikash.book_keeper.activities.shopping_list.ShoppingListUtils.ONLINE_SHARE_SUGGESTION_DISABLE_SP_KEY"

        private fun disableOnlineShareSuggestion(context: Context){
            SharedPreferenceUtils.getDefaultInstance()
                .saveDataSync(context,true,ONLINE_SHARE_SUGGESTION_DISABLE_SP_KEY)
        }

        private fun enableOnlineShareSuggestion(context: Context){
            SharedPreferenceUtils.getDefaultInstance()
                .removeKey(context,ONLINE_SHARE_SUGGESTION_DISABLE_SP_KEY)
        }

        private fun onlineShareSuggestionEnabled(context: Context):Boolean{
            return !SharedPreferenceUtils.getDefaultInstance()
                .checkIfExists(context,ONLINE_SHARE_SUGGESTION_DISABLE_SP_KEY)
        }

        fun getShareOptionsMenu(context: Context, shoppingListId: String): List<MenuViewItem> {
            return listOf(
                getSendOptionsMenuItem(context,shoppingListId),
                getOnLineShareOptionsMenuItem(context,shoppingListId),
                getOffLineShareOptionsMenuItem(context,shoppingListId)
            )
        }

        fun getShareOptionsMenu(context: Context, shoppingList: ShoppingList): MenuView {
            return MenuView().apply {
                add(getSendOptionsMenuItem(context,shoppingList.id))
                add(getOnLineShareOptionsMenuItem(context,shoppingList.id))
                add(getOffLineShareOptionsMenuItem(context,shoppingList.id))
                if (!shoppingList.partnerIds.isNullOrEmpty()){
                    add(getPartnerViewMenuItem(context,shoppingList))
                }
                add(getCreateCopyMenuViewItem(context,shoppingList))
            }
        }

        private fun getCreateCopyMenuViewItem(
            context: Context,
            shoppingList: ShoppingList
        ): MenuViewItem {
            return MenuViewItem(
                text = context.getString(R.string.sl_copy),
                task = { createCopy(context, shoppingList)}
            )
        }

        private fun createCopy(context: Context,shoppingList: ShoppingList){
            debugLog("createCopy: $shoppingList")
            val title = "${shoppingList.title!!}_2"
            val editText = EditText(context)
            editText.setText(title)
            editText.hint = context.getString(R.string.name_prompt)
            DialogUtils.showAlertDialog(context, DialogUtils.AlertDialogDetails(
                message = context.getString(R.string.save_copied_sl),
                positiveButtonText = context.getString(R.string.save_text),
                negetiveButtonText = context.getString(R.string.cancel),
                view = editText,
                doOnPositivePress = {
                    GlobalScope.launch {
                        ShoppingListRepo
                            .saveCopiedShoppingList(
                                context, shoppingList.id, editText.text.toString()
                            ).let {
                                if (it != null) {
                                    ToastUtils.showShortToast(context, R.string.sl_copy_saved)
                                    runOnMainThread({
                                        context.startActivity(
                                            ActivityShoppingList.getViewIntent(
                                                context,it.id
                                            )
                                        )
                                    })
                                } else {
                                    ToastUtils.showShortToast(
                                        context,
                                        R.string.sl_copy_save_failure
                                    )
                                }
                            }
                    }
                }
            ))
        }

        private fun getPartnerViewMenuItem(
            context: Context,
            shoppingList: ShoppingList
        ): MenuViewItem {
            return MenuViewItem(
                text = context.getString(R.string.show_partners),
                task = {showSlPartners(context,shoppingList)}
            )
        }

        private fun showSlPartners(context: Context, shoppingList: ShoppingList) {
            GlobalScope.launch {
                val partnerIds = mutableListOf<String>()
                shoppingList.apply {
                    if (userId!=AuthRepo.getUserId()){
                        partnerIds.add(userId!!)
                    }
                    this.partnerIds
                        ?.filter { it!=AuthRepo.getUserId() }
                        ?.let {
                            partnerIds.addAll(it)
                        }
                }
                if (partnerIds.isNotEmpty()){
                    val partners = partnerIds
                                                    .map { AuthRepo.findUserById(context,it) }
                                                    .filter { it!=null }
                                                    .map { it!! }

                    runOnMainThread({showParnerlist(context, partners)})
                }
            }
        }

        private fun showParnerlist(
            context: Context,
            partners: List<User>
        ) {
            val partnerListView = LayoutInflater.from(context).inflate(R.layout.view_lone_rv,null,false)
            val dialog=DialogUtils.createAlertDialog(context,DialogUtils.AlertDialogDetails(
                view = partnerListView,
                negetiveButtonText = ""
            ))
            val rv = partnerListView.findViewById<RecyclerView>(R.id.rv_single)
            val adapter = ConnectionUserPreviewForDisplayAdapter()
            rv.adapter = adapter
            adapter.submitList(partners)
            dialog.show()
        }

        private fun getSendOptionsMenuItem(context: Context, shoppingListId: String): MenuViewItem {
            return MenuViewItem(
                        text = context.getString(R.string.send_shopping_list),
                        task = {showSoppingListSendDialog(context,shoppingListId)
                    }
            )
        }

        private fun showSoppingListSendDialog(context: Context, shoppingListId: String) {
            GlobalScope.launch {
                ShoppingListRepo.findById(context, shoppingListId)?.let {
                    val shoppingList = it
                    val connectedUsers = ConnectionRequestRepo.getAllActiveConnections(context)
                    val usersForShare = connectedUsers
                                                    .filter {
                                                        shoppingList.partnerIds?.contains(it.id) == false &&
                                                                shoppingList.userId != it.id
                                                    }
                    if (connectedUsers.isEmpty()){
                        ToastUtils.showShortToast(context,R.string.no_connected_user)
                    }else if (usersForShare.isEmpty()){
                        ToastUtils.showShortToast(context,R.string.shared_with_all_connected_user)
                    }else{
                        runOnMainThread({
                            launchShoppingListSendDialog(
                                context,shoppingList,usersForShare)
                        })
                    }
                }
            }
        }

        private fun launchShoppingListSendDialog(
            context: Context,
            shoppingList: ShoppingList,
            connectedUsers: List<User>
        ) {
            val userListView = LayoutInflater.from(context).inflate(R.layout.view_lone_rv,null,false)
            val dialog=DialogUtils.createAlertDialog(context,DialogUtils.AlertDialogDetails(
                view = userListView,
                positiveButtonText = "",
                negetiveButtonText = ""
            ))
            val rv = userListView.findViewById<RecyclerView>(R.id.rv_single)
            val adapter = ConnectionUserPreviewForSlSendAdapter({
                dialog.cancel()
                sendShoppingListToUser(context,it,shoppingList)
            })
            rv.adapter = adapter
            adapter.submitList(connectedUsers)
            dialog.show()
        }

        private fun sendShoppingListToUser(context: Context,user: User,shoppingList: ShoppingList){
            debugLog(user)
            debugLog(shoppingList)
            val onlineSlShareReq = OnlineSlShareReq.getInstanceForSend(
                shoppingList,user
            )
            GlobalScope.launch {
                ShoppingListRepo.approveOnlineShareRequest(
                    context, shoppingList, onlineSlShareReq
                )
                ToastUtils.showShortToast(context,context.getString(R.string.shopping_list_send_message,user.displayText()))
            }
        }

        private fun getOffLineShareOptionsMenuItem(context: Context, shoppingListId: String): MenuViewItem {
            return MenuViewItem(
                text = context.getString(R.string.offline_share_text),
                task = {
                        if (onlineShareSuggestionEnabled(context)){
                            val view = LayoutInflater.from(context).inflate(R.layout.view_online_share_suggestion,null,false)
                            val cb = view.findViewById<CheckBox>(R.id.cb_disable_online_share_suggestion)
                            cb.setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked){
                                    disableOnlineShareSuggestion(context)
                                }else{
                                    enableOnlineShareSuggestion(context)
                                }
                            }
                            DialogUtils.showAlertDialog(context, DialogUtils.AlertDialogDetails(
                                view = view,
                                positiveButtonText = context.getString(R.string.online_share_text),
                                negetiveButtonText = context.getString(R.string.offline_share_text),
                                doOnPositivePress = {
                                    context.startActivity(ActivityShoppingListShare.getOnlineShareInstance(context,shoppingListId))
                                },
                                doOnNegetivePress = {
                                    context.startActivity(ActivityShoppingListShare.getOfflineShareInstance(context,shoppingListId))
                                }
                            ))
                        }else{
                            context.startActivity(ActivityShoppingListShare.getOfflineShareInstance(context,shoppingListId))
                        }
                }
            )
        }
        private fun getOnLineShareOptionsMenuItem(context: Context, shoppingListId: String): MenuViewItem {
            return MenuViewItem(
                text = context.getString(R.string.online_share_text),
                task = {
                        GlobalScope.launch {
                            ShoppingListRepo.findById(context, shoppingListId)?.let {
                                runOnMainThread({
//                                    if (it.userId == AuthRepo.getUserId()) {
                                        context.startActivity(
                                            ActivityShoppingListShare.getOnlineShareInstance(
                                                context,
                                                shoppingListId
                                            )
                                        )
//                                    } else {
//                                        ToastUtils.showShortToast(
//                                            context,
//                                            R.string.owner_sl_online_share_message
//                                        )
//                                    }
                                })
                            }
                        }
                }
            )
        }
    }
}