package com.dasbikash.book_keeper.activities.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.rv_helpers.CalculatorHistoryAdapter
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.activity_calculator.*
import kotlinx.coroutines.launch

class ActivityCalculator : AppCompatActivity() {

    private lateinit var viewModel:CalculatorViewModel
    private val calculatorHistoryAdapter =
        CalculatorHistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        viewModel = ViewModelProviders.of(this).get(CalculatorViewModel::class.java)
        rv_history_holder.adapter = calculatorHistoryAdapter

        tv_zero.setOnClickListener { viewModel.addPressedDigit(DIGIT_ZERO) }
        tv_one.setOnClickListener { viewModel.addPressedDigit(DIGIT_ONE) }
        tv_two.setOnClickListener { viewModel.addPressedDigit(DIGIT_TWO) }
        tv_three.setOnClickListener { viewModel.addPressedDigit(DIGIT_THREE) }
        tv_four.setOnClickListener { viewModel.addPressedDigit(DIGIT_FOUR) }
        tv_five.setOnClickListener { viewModel.addPressedDigit(DIGIT_FIVE) }
        tv_six.setOnClickListener { viewModel.addPressedDigit(DIGIT_SIX) }
        tv_seven.setOnClickListener { viewModel.addPressedDigit(DIGIT_SEVEN) }
        tv_eight.setOnClickListener { viewModel.addPressedDigit(DIGIT_EIGHT) }
        tv_nine.setOnClickListener { viewModel.addPressedDigit(DIGIT_NINE) }
        tv_dot.setOnClickListener { viewModel.addPressedDigit(DIGIT_DOT) }
        tv_sign.setOnClickListener { viewModel.toggleSign() }

        tv_percent.setOnClickListener { viewModel.percentAction() }
        tv_ce_action.setOnClickListener { viewModel.clearCurrentNumber() }
        tv_c_action.setOnClickListener { viewModel.clearAll() }
        tv_bs_sign.setOnClickListener { viewModel.removeLast() }

        tv_inv.setOnClickListener { viewModel.invertAction() }
        tv_square.setOnClickListener { viewModel.squareAction() }
        tv_sqrt.setOnClickListener { viewModel.sqrtAction() }

        tv_plus_sign.setOnClickListener { viewModel.addAction(this) }
        tv_minus_sign.setOnClickListener { viewModel.subAction(this) }
        tv_mul_sign.setOnClickListener { viewModel.mulAction(this) }
        tv_div_sign.setOnClickListener { viewModel.divAction(this) }
        tv_equal_sign.setOnClickListener { viewModel.equalAction(this) }

        tv_mem_plus.setOnClickListener { lifecycleScope.launch { viewModel.addToMem(this@ActivityCalculator) }}
        tv_mem_minus.setOnClickListener { lifecycleScope.launch { viewModel.subFromMem(this@ActivityCalculator) }}
        tv_mem_recall.setOnClickListener { lifecycleScope.launch { viewModel.loadFromMem(this@ActivityCalculator) }}
        tv_mem_clear.setOnClickListener { viewModel.clearMem(this) }

        btn_history.setOnClickListener {
            if (calc_history_block.isVisible){
                calc_history_block.hide()
                btn_history_delete.hide()
            }else{
                lifecycleScope.launch {
                    CalculatorHistory.getAllHistories(this@ActivityCalculator).let {
                        if (!it.isNullOrEmpty()){
                            calculatorHistoryAdapter.submitList(it.toList())
                            calc_history_block.show()
                            btn_history_delete.show()
                            calc_history_block.bringToFront()
                        }
                    }
                }
            }
        }

        btn_history_delete.setOnClickListener {
            DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
                message = getString(R.string.delete_calc_history_prompt),
                doOnPositivePress = {
                    if (calculatorHistoryAdapter.currentList.isNotEmpty()) {
                        deleteHistory()
                        showShortSnack(R.string.delete_calc_history_message)
                    }
                }
            ))

        }

        viewModel.getCurrentNumber().observe(this,object : Observer<String>{
            override fun onChanged(currentNumber: String?) {
                currentNumber?.let { tv_current_number.text = it}
            }
        })

        viewModel.getLeftOperand().observe(this,object : Observer<String?>{
            override fun onChanged(data: String?) {
                tv_left_operand.text = data ?: ""
            }
        })

        viewModel.getRightOperand().observe(this,object : Observer<String?>{
            override fun onChanged(data: String?) {
                tv_right_operand.text = data ?: ""
            }
        })

        viewModel.getOperation().observe(this,object : Observer<String?>{
            override fun onChanged(data: String?) {
                tv_operation.text = data ?: ""
            }
        })
    }

    private fun deleteHistory() {
        CalculatorHistory.clearHistory(this)
        calculatorHistoryAdapter.submitList(emptyList())
        calc_history_block.hide()
        btn_history_delete.hide()
    }

    companion object{
        private const val DIGIT_ZERO = '0'
        private const val DIGIT_ONE = '1'
        private const val DIGIT_TWO = '2'
        private const val DIGIT_THREE = '3'
        private const val DIGIT_FOUR = '4'
        private const val DIGIT_FIVE = '5'
        private const val DIGIT_SIX = '6'
        private const val DIGIT_SEVEN = '7'
        private const val DIGIT_EIGHT = '8'
        private const val DIGIT_NINE = '9'
        private const val DIGIT_DOT = '.'
    }
}
