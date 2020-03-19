package com.dasbikash.exp_man.activities.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dasbikash.android_basic_utils.utils.debugLog
import java.lang.StringBuilder
import java.util.*

class CalculatorViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private var runningResult:Double?=null
    private val currentNumberDigits:Deque<Char> = LinkedList<Char>()

    private val currentNumberLiveData = MutableLiveData<String>()
    fun getCurrentNumber():LiveData<String> = currentNumberLiveData

    init {
        sendCurrentNumberAsString()
    }

    fun addPressedDigit(char: Char){
        debugLog(String(currentNumberDigits.toCharArray()))
        if (char == DOT_CHAR){
            if (currentNumberDigits.contains(DOT_CHAR)){
                return
            }
        }else {
            if (currentNumberDigits.contains(DOT_CHAR)){
                val integerPartLength = getIntegerPartLength() + if (currentNumberDigits.first=='-') 1 else 0
                if((currentNumberDigits.size - (integerPartLength+1)) >= MAX_DECIMAL_PART_LENGTH) {
                    return
                }
            }else if (getIntegerPartLength() >= MAX_INT_PART_LENGTH){
                return
            }
        }
        currentNumberDigits.add(char)
        sendCurrentNumberAsString()
    }

    fun removeLast(){
        if (isInvalidNumber()){
            currentNumberDigits.clear()
            sendCurrentNumberAsString()
        }
        currentNumberDigits.pollLast()?.let {
            if (currentNumberDigits.size==1 && currentNumberDigits.first=='-'){
                currentNumberDigits.clear()
            }
            sendCurrentNumberAsString()
        }
    }

    fun toggleSign(){
        if (currentNumberDigits.isNotEmpty()){
            if (getCurrentNumberVal() < 0){
                currentNumberDigits.pollFirst()
                sendCurrentNumberAsString()
            }else{
                currentNumberDigits.addFirst('-')
                sendCurrentNumberAsString()
            }
        }
    }

    private fun getIntegerPartLength():Int{
        if (currentNumberDigits.isEmpty()){
            return 0
        }
        currentNumberDigits.indexOf(DOT_CHAR).let {
            if (it == -1){
                return if(currentNumberDigits.first == '-') currentNumberDigits.size-1 else currentNumberDigits.size;
            }else{
                return if(currentNumberDigits.first == '-') it-1 else it;
            }
        }
    }

    private fun sendCurrentNumberAsString(){

        if (isInvalidNumber()){return}

        if (currentNumberDigits.isEmpty()){
            currentNumberLiveData.postValue("0")
            return
        }

        val numberStringBuilder = StringBuilder()
        val integerPartLength = getIntegerPartLength()
        var i = 0

        String(currentNumberDigits.toCharArray()).let {
            if (getCurrentNumberVal()<0){
                numberStringBuilder.append('-')
                return@let it.substring(1)
            }else{
                return@let it
            }
        }.forEach {
            numberStringBuilder.append(it)
            i++
            (integerPartLength - i).let {
                if (it>0 && it%3==0){
                    numberStringBuilder.append(',')
                }
            }
        }
        currentNumberLiveData.postValue(numberStringBuilder.toString())
    }

    fun clearAll(){
        runningResult = null
        clearCurrentNumber()
    }

    fun clearCurrentNumber(){
        currentNumberDigits.clear()
        sendCurrentNumberAsString()
    }

    private fun getCurrentNumberVal():Double{
        if (currentNumberDigits.isEmpty()){return 0.0}
        return String(currentNumberDigits.toCharArray()).toDouble()
    }

    private fun moveCurrentNumberValToResult(){

    }

    private fun setInvalidNumberString(){
        currentNumberDigits.clear()
        INVALID_NUMBER_MESSAGE.toCharArray().forEach { currentNumberDigits.add(it) }
        sendCurrentNumberAsString()
    }

    fun invertAction(){
        if (isInvalidNumber()){return}
        try {
            conditionResult(1 / getCurrentNumberVal()).let {
                currentNumberDigits.clear()
                String.format("%2.5f",it).toCharArray().forEach { currentNumberDigits.add(it) }
//                isNumberSignNegative = it < 0
                sendCurrentNumberAsString()
            }
        }catch (ex:Throwable){
            ex.printStackTrace()
            setInvalidNumberString()
        }
    }

    private fun conditionResult(result: Double):Double{
        if (result != result.toLong().toDouble()){
            return String.format("%2.5f",result).toDouble()
        }
        return result
    }

    private fun isInvalidNumber():Boolean = String(currentNumberDigits.toCharArray()) == INVALID_NUMBER_MESSAGE

    companion object{
        private const val INVALID_NUMBER_MESSAGE = "Invalid Number!!"
        private val DOT_CHAR = '.'
        private const val MAX_INT_PART_LENGTH = 10
        private const val MAX_DECIMAL_PART_LENGTH = 4

        private enum class CalculatorTask{ADD,SUB,MUL,DIV}
    }
}