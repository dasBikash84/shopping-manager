package com.dasbikash.book_keeper.activities.shopping_list

import android.content.Context
import android.view.LayoutInflater
import android.widget.CheckBox
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_toast_utils.ToastUtils
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.sl_share.ActivityShoppingListShare
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

        fun getShareOptionsMenu(context: Context, shoppingListId: String): MenuView {
            return MenuView().apply {
                add(getOnLineShareOptionsMenuItem(context,shoppingListId))
                add(getOffLineShareOptionsMenuItem(context,shoppingListId))
            }
        }

        private fun getOffLineShareOptionsMenuItem(context: Context, shoppingListId: String): MenuViewItem {
            return MenuViewItem(
                text = context.getString(R.string.offline_share_text),
                task = {
                        if (onlineShareSuggestionEnabled(context)){
                            val view = LayoutInflater.from(context).inflate(R.layout.view_online_share_suggestion,null,false)
                            val cb = view.findViewById<CheckBox>(R.id.cb_disable_online_share_suggestion)
                            cb.setOnCheckedChangeListener { buttonView, isChecked ->
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
                            val shoppingList = ShoppingListRepo.findById(context, shoppingListId)?.let {
                                runOnMainThread({
                                    if (it.userId == AuthRepo.getUserId()) {
                                        context.startActivity(
                                            ActivityShoppingListShare.getOnlineShareInstance(
                                                context,
                                                shoppingListId
                                            )
                                        )
                                    } else {
                                        ToastUtils.showShortToast(
                                            context,
                                            R.string.owner_sl_online_share_message
                                        )
                                    }
                                })
                            }
                        }
                }
            )
        }
    }
}