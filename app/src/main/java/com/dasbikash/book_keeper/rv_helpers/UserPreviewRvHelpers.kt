package com.dasbikash.book_keeper.rv_helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_extensions.show
import com.dasbikash.android_image_utils.ImageUtils
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper_repo.ImageRepo
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.attachMenuViewForClick

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

abstract class UserPreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val iv_user_image:ImageView = itemView.findViewById(R.id.iv_user_image)
    private val tv_user_display_name: TextView = itemView.findViewById(R.id.tv_user_display_name)
    private val tv_user_email:TextView = itemView.findViewById(R.id.tv_user_email)
    private val tv_user_phone:TextView = itemView.findViewById(R.id.tv_user_phone)

    @CallSuper
    open fun bind(user: User) {
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
                    ImageRepo
                        .downloadImageFile(
                            itemView.context, it,doOnDownload = {
                                ImageUtils.displayImageFile(iv_user_image, it)
                            }
                        )
                }else{
                    iv_user_image.setImageResource(R.drawable.ic_account)
                }
            }
        }
    }
}

class SearchedUserPreviewHolder(itemView: View) : UserPreviewHolder(itemView) {
    val iv_add_user:ImageView = itemView.findViewById(R.id.iv_add_user)
}

class ConnectionUserAdapter(private val getOptionsMenu:(Context,User)->MenuView):ListAdapter<User, ConnectionUserPreviewHolder>(UserDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionUserPreviewHolder {
        return ConnectionUserPreviewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_connection_user_preview, parent, false
            ),getOptionsMenu
        )
    }

    override fun onBindViewHolder(holder: ConnectionUserPreviewHolder, position: Int) {
        val user = getItem(position)!!
        holder.bind(user)
        holder
            .itemView
            .setOnClickListener {
                holder.getOptionsMenu(holder.itemView.context,user).show(holder.itemView.context)
            }
    }
}

class ConnectionUserPreviewHolder(itemView: View,
                                   val getOptionsMenu:(Context,User)->MenuView) : UserPreviewHolder(itemView) {

    private val iv_connection_options:ImageView = itemView.findViewById(R.id.iv_connection_options)

    @CallSuper
    override fun bind(user: User) {
        super.bind(user)
        iv_connection_options.attachMenuViewForClick(getOptionsMenu(itemView.context,user))
    }
}

class ConnectionUserPreviewForSlSendAdapter(private val sendSlTask:(User)->Unit)
    :ListAdapter<User, ConnectionUserPreviewForSlSendHolder>(UserDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionUserPreviewForSlSendHolder {
        return ConnectionUserPreviewForSlSendHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_connection_user_preview, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ConnectionUserPreviewForSlSendHolder, position: Int) {
        val user = getItem(position)!!
        holder.bind(user)
        holder
            .itemView
            .setOnClickListener {
                sendSlTask(user)
            }
    }
}
class ConnectionUserPreviewForSlSendHolder(itemView: View) : UserPreviewHolder(itemView) {
    private val iv_connection_options:ImageView = itemView.findViewById(R.id.iv_connection_options)
    @CallSuper
    override fun bind(user: User) {
        super.bind(user)
        iv_connection_options.hide()
    }
}

class ConnectionUserPreviewForDisplay(itemView: View) : UserPreviewHolder(itemView) {
    private val iv_connection_options:ImageView = itemView.findViewById(R.id.iv_connection_options)
    init {
        iv_connection_options.hide()
    }
}

class ConnectionUserPreviewForDisplayAdapter()
    :ListAdapter<User, ConnectionUserPreviewForDisplay>(UserDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionUserPreviewForDisplay {
        return ConnectionUserPreviewForDisplay(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_connection_user_preview, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ConnectionUserPreviewForDisplay, position: Int) {
        val user = getItem(position)!!
        holder.bind(user)
    }
}