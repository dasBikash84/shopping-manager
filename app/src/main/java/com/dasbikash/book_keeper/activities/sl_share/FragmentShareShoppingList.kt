package com.dasbikash.book_keeper.activities.sl_share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.lifecycle.lifecycleScope
import com.dasbikash.pop_up_message.DialogUtils
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import kotlinx.android.synthetic.main.fragment_share_shopping_list.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode

abstract class FragmentShareShoppingList:FragmentTemplate(),WaitScreenOwner {
    private lateinit var shoppingList: ShoppingList

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_share_shopping_list, container, false)
    }


    @CallSuper
    override fun getPageTitle(context: Context): String? {
        return getTitle(context)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_done_offline_sharing.setOnClickListener {
            activity?.finish()
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        runWithContext {
            showWaitScreen()
            lifecycleScope.launch {
                if (!::shoppingList.isInitialized) {
                    shoppingList = ShoppingListRepo.findInLocalById(it,getShoppingListId())!!
                }
                val shoppingListItems = mutableListOf<ShoppingListItem>()
                shoppingList.shoppingListItemIds?.asSequence()?.forEach {
                    ShoppingListRepo.findShoppingListItemById(context!!,it)!!.let {
                        shoppingListItems.add(it)
                    }
                }
                shoppingList.shoppingListItems = shoppingListItems.toList()
                try {
                    getDataForQrCode(shoppingList).let {
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

    override fun registerWaitScreen(): ViewGroup = wait_screen

    protected abstract fun getShoppingListId():String
    protected abstract suspend fun getDataForQrCode(shoppingList: ShoppingList):String
    protected abstract fun getTitle(context: Context): String
}