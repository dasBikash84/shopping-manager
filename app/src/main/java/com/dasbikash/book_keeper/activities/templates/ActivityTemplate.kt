package com.dasbikash.book_keeper.activities.templates

import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.show
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.menu_view.attachMenuViewForClick
import com.dasbikash.super_activity.SingleFragmentSuperActivity
import kotlinx.android.synthetic.main.activity_template.*
import kotlinx.android.synthetic.main.view_wait_screen.*

abstract class ActivityTemplate: SingleFragmentSuperActivity(),WaitScreenOwner {

    override fun getDefaultFragment(): FragmentTemplate = getDefaultChildInstance()

    private fun getDefaultChildInstance(): FragmentTemplate {
        val fragmentTemplate =  registerDefaultFragment()
        processTitle(fragmentTemplate)
        return fragmentTemplate
    }

    private fun processTitle(fragmentTemplate: FragmentTemplate) {
        showWaitScreen()
        if (fragmentTemplate.hidePageTitle()){
            title_holder.hide()
        }else {
            setPageTitle(
                fragmentTemplate.getPageTitle(this) ?: getDefaultTitle()
                ?: getString(R.string.app_name)
            )
            title_holder.show()
        }
        fragmentTemplate.getOptionsMenu(this).let {
            if (it != null) {
                btn_options.attachMenuViewForClick(it)
                btn_options.show()
            } else {
                btn_options.hide()
            }
        }
        hideWaitScreen()
    }

    override fun getFragmentFromBackStack(): Fragment? {
        val fragment = super.getFragmentFromBackStack()
        (fragment as FragmentTemplate?)?.let {
            processTitle(it)
        }
        return fragment
    }

    override fun getLayoutID(): Int = R.layout.activity_template
    override fun getLoneFrameId(): Int = R.id.lone_frame
    override fun registerWaitScreen(): ViewGroup = wait_screen

    protected fun setPageTitle(title:String) = page_title.setText(title)

    protected open fun getDefaultTitle():String? = null
    abstract fun registerDefaultFragment(): FragmentTemplate

    @CallSuper
    override fun addFragment(fragment: Fragment, doOnFragmentLoad: (() -> Unit)?) {
        showWaitScreen()
        super.addFragment(fragment, {
            doOnFragmentLoad?.invoke()
            processTitle(fragment as FragmentTemplate)
            hideWaitScreen()
        })
    }

    @CallSuper
    override fun addFragmentClearingBackStack(fragment: Fragment, doOnFragmentLoad: (() -> Unit)?) {
        showWaitScreen()
        super.addFragmentClearingBackStack(fragment,  {
            doOnFragmentLoad?.invoke()
            processTitle(fragment as FragmentTemplate)
            hideWaitScreen()
        })
    }

    override fun onBackPressed() {
        (getCurrentFragment() as FragmentTemplate?)?.let {
            if (it.getExitPrompt() == null){
                super.onBackPressed()
            }else{
                DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
                    message = it.getExitPrompt()!!,
                    doOnPositivePress = {
                        super.onBackPressed()
                    }
                ))
            }
        }
    }
}