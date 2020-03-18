package com.dasbikash.exp_man.activities.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dasbikash.android_basic_utils.utils.debugLog
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayDeque

class CalculatorViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private var currentNumberSign = false
    private val currentNumber:Deque<Char> = LinkedList<Char>()

    private val currentNumberLiveData = MutableLiveData<String>()
    fun getCurrentNumber():LiveData<String> = currentNumberLiveData

    fun addPressedDigit(char: Char){
        debugLog(String(currentNumber.toCharArray()))
        if (char == DOT_CHAR){
            if (currentNumber.contains(DOT_CHAR)){
                debugLog("Dot found")
                return
            }
            String(currentNumber.toCharArray()).substringAfter(DOT_CHAR,"").let {
                debugLog("After Dot: $it")
                if (it.length>MAX_NUMBER_LENGTH_AFTER_DIT){
                    return
                }
            }
        }else{
            if (currentNumber.size >= MAX_NUMBER_LENGTH){
                return
            }
        }
        currentNumber.add(char)
        sendCurrentNumberAsString()
    }

    fun removeLast(){
        currentNumber.pollLast()?.let {
            sendCurrentNumberAsString()
        }
    }

    fun toggleSign(){
        currentNumberSign != currentNumberSign

    }

    private fun sendCurrentNumberAsString(){
        val numberStringBuilder = StringBuilder()
        var i = 0
        currentNumber.asSequence().forEach {
            numberStringBuilder.append(it)
            i++
            (currentNumber.size - i).let {
                if (it>0 && it%3==0){
                    numberStringBuilder.append(',')
                }
            }
        }
        currentNumberLiveData.postValue(numberStringBuilder.toString())
    }
    companion object{
        private val DOT_CHAR = '.'
        private const val MAX_NUMBER_LENGTH = 10
        private const val MAX_NUMBER_LENGTH_AFTER_DIT = 4
    }
}