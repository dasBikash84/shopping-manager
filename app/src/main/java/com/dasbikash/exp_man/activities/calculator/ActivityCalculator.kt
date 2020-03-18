package com.dasbikash.exp_man.activities.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dasbikash.exp_man.R
import kotlinx.android.synthetic.main.activity_calculator.*

class ActivityCalculator : AppCompatActivity() {

    private lateinit var viewModel:CalculatorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)
        viewModel = ViewModelProviders.of(this).get(CalculatorViewModel::class.java)

        tv_zero.setOnClickListener { viewModel.addPressedDigit('0') }
        tv_one.setOnClickListener { viewModel.addPressedDigit('1') }
        tv_two.setOnClickListener { viewModel.addPressedDigit('2') }
        tv_three.setOnClickListener { viewModel.addPressedDigit('3') }
        tv_four.setOnClickListener { viewModel.addPressedDigit('4') }
        tv_five.setOnClickListener { viewModel.addPressedDigit('6') }
        tv_six.setOnClickListener { viewModel.addPressedDigit('6') }
        tv_seven.setOnClickListener { viewModel.addPressedDigit('7') }
        tv_eight.setOnClickListener { viewModel.addPressedDigit('8') }
        tv_nine.setOnClickListener { viewModel.addPressedDigit('9') }
        tv_dot.setOnClickListener { viewModel.addPressedDigit('.') }
        tv_bs_sign.setOnClickListener { viewModel.removeLast() }

        viewModel.getCurrentNumber().observe(this,object : Observer<String>{
            override fun onChanged(currentNumber: String?) {
                currentNumber?.let { tv_current_number.text = it}
            }
        })
    }
}
