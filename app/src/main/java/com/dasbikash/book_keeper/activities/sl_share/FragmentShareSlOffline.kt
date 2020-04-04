package com.dasbikash.book_keeper.activities.sl_share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_share_sl_offline.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode

class FragmentShareSlOffline:FragmentTemplate(),WaitScreenOwner {
    private lateinit var shoppingList: ShoppingList

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_share_sl_offline, container, false)
    }

    override fun getPageTitle(context: Context): String? {
        return context.getString(R.string.offline_share_page_title)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_done_offline_sharing.setOnClickListener {
//            runWithContext {
//                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
//                    message = it.getString(R.string.done_prompt),
//                    doOnPositivePress = {
                        activity?.finish()
//                    }
//                ))
//            }
        }
    }

    override fun onResume() {
        super.onResume()
        runWithContext {
            showWaitScreen()
            lifecycleScope.launch {
                if (!::shoppingList.isInitialized) {
                    shoppingList = ShoppingListRepo.findById(it,getShoppingListId())!!
                }
                val shoppingListItems = mutableListOf<ShoppingListItem>()
                shoppingList.shoppingListItemIds?.asSequence()?.forEach {
                    ShoppingListRepo.findShoppingListItemById(context!!,it)!!.let {
                        shoppingListItems.add(it)
                    }
                }
                shoppingList.shoppingListItems = shoppingListItems.toList()
                try {
                    SlToQr.getPayloadForOfflineSharing(shoppingList).let {
                        QRCode.from(it).let {
                            iv_sl_qr_code.setImageBitmap(it.bitmap())
                        }
                    }
                    hideWaitScreen()
                }catch (ex:Throwable){
                    ex.printStackTrace()
                    DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                        message = it.getString(R.string.share_online_instead_offline,shoppingList.title),
                        positiveButtonText = it.getString(R.string.yes),
                        negetiveButtonText = it.getString(R.string.no),
                        doOnPositivePress = {
                            (activity as ActivityShoppingListShare?)?.addFragmentClearingBackStack(
                                FragmentShareSlOnline.getInstance(getShoppingListId())
                            )
                        },
                        doOnNegetivePress = {
                            activity?.finish()
                        }
                    ))
                }
            }
        }
    }

    private fun getShoppingListId():String = arguments!!.getString(ARG_SL_ID)!!
    override fun registerWaitScreen(): ViewGroup = wait_screen

    companion object{
        private const val ARG_SL_ID =
            "com.dasbikash.book_keeper.activities.sl_share.FragmentShareSlOffline.ARG_SL_ID"

        fun getInstance(shoppingListId:String):FragmentShareSlOffline{
            val arg = Bundle()
            arg.putString(ARG_SL_ID,shoppingListId)
            val fragment = FragmentShareSlOffline()
            fragment.arguments = arg
            return fragment
        }
    }
}