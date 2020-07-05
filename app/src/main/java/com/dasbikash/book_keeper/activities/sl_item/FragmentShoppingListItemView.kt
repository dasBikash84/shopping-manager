package com.dasbikash.book_keeper.activities.sl_item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.show
import com.dasbikash.android_image_utils.ImageUtils
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.StringListAdapter
import com.dasbikash.book_keeper.utils.getCurrencyStringWithSymbol
import com.dasbikash.book_keeper.utils.rotateIfRequired
import com.dasbikash.book_keeper_repo.ImageRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import kotlinx.android.synthetic.main.fragment_shopping_list_item_add_edit.rv_sli_brand_sug
import kotlinx.android.synthetic.main.fragment_shopping_list_item_add_edit.rv_sli_images
import kotlinx.android.synthetic.main.fragment_shopping_list_item_add_edit.sli_brand_sug_holder
import kotlinx.android.synthetic.main.fragment_shopping_list_item_add_edit.vp_product_image
import kotlinx.android.synthetic.main.fragment_shopping_list_item_view.*
import kotlinx.coroutines.launch

class FragmentShoppingListItemView:FragmentTemplate() {

    private lateinit var viewModel:ViewModelShoppingListItem
    private lateinit var shoppingListItem:ShoppingListItem

    private val imageListAdapter = StringListAdapter({view,text->
        runWithContext {
            lifecycleScope.launch {
                ImageRepo.downloadImageFile(it, text,doOnDownload = {
                    ImageUtils.getBitmapFromFile(it)?.let {
                        (view as ImageView).setImageBitmap(it.rotateIfRequired())
                    }
                })
            }
        }
    },{doOnProductImageClick(it)},R.layout.view_single_preview_image)

    private fun doOnProductImageClick(url: String) {
        debugLog(url)
        val fragmentManager: FragmentManager = activity!!.getSupportFragmentManager()
        vp_product_image.adapter = object : FragmentStatePagerAdapter(fragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getItem(position: Int): Fragment {
                return FragmentProductImage.getViewInstance(shoppingListItem.images!!.get(position))
            }
            override fun getCount(): Int = shoppingListItem.images?.size ?: 0
        }
        vp_product_image.setCurrentItem(shoppingListItem.images!!.indexOf(url),true)
        vp_product_image.bringToFront()
        vp_product_image.show()
    }

    private val brandSuggestionsAdapter = StringListAdapter({view, text ->
        (view as TextView).text = text
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.fragment_shopping_list_item_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_sli_images.adapter = imageListAdapter
        rv_sli_brand_sug.adapter = brandSuggestionsAdapter
        viewModel = ViewModelProviders.of(activity!!).get(ViewModelShoppingListItem::class.java)

        viewModel.getShoppingListItem().observe(this,object : Observer<ShoppingListItem> {
            override fun onChanged(item: ShoppingListItem?) {
                item?.let {
                    shoppingListItem = it
                    refreshView()
                }
            }
        })

        runWithContext {
            lifecycleScope.launch {
                ShoppingListRepo.findShoppingListItemById(it,getShoppingListItemId())?.let {
                    viewModel.setShoppingListItem(it)
                }
            }
        }
    }

    private fun refreshView() {

        if (::shoppingListItem.isInitialized) {
            debugLog(shoppingListItem)
            vp_product_image.hide()
            shoppingListItem.name?.let {
                (activity as ActivityShoppingListItem?)?.setTitle(it)
            }
            shoppingListItem.categoryId.let {
                tv_sli_category.text = resources.getStringArray(R.array.expense_categories).get(it)
            }

            shoppingListItem.details?.let { tv_sli_details.setText(it) }

            tv_sli_price_range.text = getString(
                                            R.string.sli_price_range,
                                            shoppingListItem.minUnitPrice?.getCurrencyStringWithSymbol(context!!) ?: "",
                                            shoppingListItem.maxUnitPrice?.getCurrencyStringWithSymbol(context!!) ?: "")
            tv_sli_qty.text = getString(
                                        R.string.sli_qty_text,
                                        shoppingListItem.qty.toString(), resources.getStringArray(R.array.uoms).get(shoppingListItem.uom))

            (shoppingListItem.images ?: emptyList()).let {
                imageListAdapter.submitList(it)
            }

            (shoppingListItem.brandNameSuggestions ?: emptyList()).let {
                if (!it.isEmpty()){
                    brandSuggestionsAdapter.submitList(it)
                    sli_brand_sug_holder.show()
                }else{
                    sli_brand_sug_holder.hide()
                }
            }
        }
    }

    private fun getShoppingListItemId():String = arguments!!.getString(ARG_SHOPPING_LIST_ITEM_ID)!!

    companion object {
        private const val ARG_SHOPPING_LIST_ITEM_ID =
            "com.dasbikash.book_keeper.activities.sl_item.FragmentShoppingListItemView.ARG_SHOPPING_LIST_ITEM_ID"

        fun getInstance(shoppingListItemId: String): FragmentShoppingListItemView {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ITEM_ID, shoppingListItemId)
            val fragment = FragmentShoppingListItemView()
            fragment.arguments = arg
            return fragment
        }
    }

}