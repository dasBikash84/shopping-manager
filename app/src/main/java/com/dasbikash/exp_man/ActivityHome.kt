package com.dasbikash.exp_man

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_home.*

class ActivityHome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    override fun onResume() {
        super.onResume()
        if(intent.hasExtra(EXTRA_USER)){
            home_ac_title.text = "Hello user"
        }else if(intent.hasExtra(EXTRA_GUEST)){
            home_ac_title.text = "Hello guest"
        }
    }

    companion object{
        private const val EXTRA_GUEST = "com.dasbikash.exp_man.ActivityHome.EXTRA_GUEST"
        private const val EXTRA_USER = "com.dasbikash.exp_man.ActivityHome.EXTRA_USER"

        fun getGuestInstance(context: Context):Intent{
            val intent = Intent(context.applicationContext,ActivityHome::class.java)
            intent.putExtra(EXTRA_GUEST,EXTRA_GUEST)
            return intent
        }

        fun getUserInstance(context: Context):Intent{
            val intent = Intent(context.applicationContext,ActivityHome::class.java)
            intent.putExtra(EXTRA_USER,EXTRA_USER)
            return intent
        }
    }
}
