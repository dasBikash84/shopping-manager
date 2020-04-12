package com.dasbikash.book_keeper.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_extensions.show
import com.dasbikash.android_image_utils.ImageUtils
import com.dasbikash.async_manager.AsyncTaskManager
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper_repo.ImageRepo
import com.dasbikash.book_keeper_repo.model.User
import kotlinx.android.synthetic.main.view_searched_user_preview.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object UserDiffCallback: DiffUtil.ItemCallback<User>(){
    override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem==newItem
    }
}

class SearchedUserAdapter(private val addUserAction:((User)->Unit)?=null):ListAdapter<User, SearchedUserPreviewHolder>(UserDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchedUserPreviewHolder {
        return SearchedUserPreviewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_searched_user_preview, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchedUserPreviewHolder, position: Int) {
        val user = getItem(position)!!
        holder.bind(user)
        addUserAction?.apply {
            holder
                .iv_add_user
                .setOnClickListener {
                    runOnMainThread({this.invoke(user)})
                }
        }
    }
}

class SearchedUserPreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val iv_user_image:ImageView = itemView.findViewById(R.id.iv_user_image)
    private val tv_user_display_name: TextView = itemView.findViewById(R.id.tv_user_display_name)
    private val tv_user_email:TextView = itemView.findViewById(R.id.tv_user_email)
    private val tv_user_phone:TextView = itemView.findViewById(R.id.tv_user_phone)
    val iv_add_user:ImageView = itemView.findViewById(R.id.iv_add_user)

    fun bind(user: User) {
        user.apply {

            if (firstName !=null){
                tv_user_display_name.text = itemView.context.getString(R.string.display_name,firstName,lastName ?: "")
                tv_user_display_name.show()
            }else{
                tv_user_display_name.hide()
            }

            if (email !=null){
                tv_user_email.text = email
                tv_user_email.show()
            }else{
                tv_user_email.hide()
            }

            if (phone !=null){
                tv_user_phone.text = phone
                tv_user_phone.show()
            }else{
                tv_user_phone.hide()
            }
            thumbPhotoUrl.let {
                if (it!=null) {
                    AsyncTaskManager.addTask<Any, Any> {
                        GlobalScope.launch {
                            ImageRepo.downloadImageFile(itemView.context, it)?.let {
                                try {
                                    ImageUtils.displayImageFile(iv_user_image, it)
                                } catch (ex: Throwable) {
                                }
                            }
                        }
                    }
                }else{
                    iv_user_image.setImageResource(R.drawable.ic_account)
                }
            }
        }
    }
}