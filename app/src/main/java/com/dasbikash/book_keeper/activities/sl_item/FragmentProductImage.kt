package com.dasbikash.book_keeper.activities.sl_item

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_image_utils.ImageUtils

import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.utils.rotateIfRequired
import com.dasbikash.book_keeper_repo.ImageRepo
import kotlinx.android.synthetic.main.fragment_product_image_full.*
import kotlinx.coroutines.launch

class FragmentProductImage private constructor(): Fragment() {

    private lateinit var viewModel:ViewModelShoppingListItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_image_full, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(ViewModelShoppingListItem::class.java)

        btn_close_product_image_full.bringToFront()

        runWithContext {
            lifecycleScope.launch {
                ImageRepo.downloadImageFile(it, getImageLoc())?.let {
                    ImageUtils.getBitmapFromFile(it)?.let {
                        iv_product_image_full.setImageBitmap(it.rotateIfRequired())
                    }
                }
            }
        }

        btn_close_product_image_full.setOnClickListener {
            close()
        }
        btn_del_product_image_full.setOnClickListener {
            runWithContext {
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    message = it.getString(R.string.confirm_delete_prompt),
                    doOnPositivePress = {
                        viewModel.removeProductImage(getImageLoc())
                    }
                ))
            }
        }
        view.setOnClickListener { close() }
    }

    private fun close() {
        viewModel.getShoppingListItem().value?.let {
            viewModel.setShoppingListItem(it)
        }
    }

    private fun getImageLoc():String = arguments!!.getString(ARG_IMAGE_LOC)!!

    companion object{
        private const val ARG_IMAGE_LOC =
            "com.dasbikash.book_keeper.activities.sl_item.FragmentProductImage.ARG_IMAGE_LOC"
        fun getInstance(imageLoc:String):FragmentProductImage{
            val arg = Bundle()
            arg.putString(ARG_IMAGE_LOC,imageLoc)
            val fragment = FragmentProductImage()
            fragment.arguments = arg
            return fragment
        }
    }
}
