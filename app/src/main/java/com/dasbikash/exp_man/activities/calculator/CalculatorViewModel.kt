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
        currentNumberDigits.clear()
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
                if((currentNumberDigits.size - (getIntegerPartLength()+1)) >= MAX_NUMBER_LENGTH_AFTER_DOT) {
                    return
                }
            }else if (getIntegerPartLength() >= MAX_NUMBER_LENGTH){
                return
            }
        }
        currentNumberDigits.add(char)
        sendCurrentNumberAsString()
    }

    fun removeLast(){
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
        var dotIndex:Int? = null
        String(currentNumberDigits.toCharArray()).let {
            if (it.contains(DOT_CHAR)){
                dotIndex = it.indexOf(DOT_CHAR)
            }
        }
        if (currentNumberDigits.first == '-'){
            if (dotIndex!=null){
                debugLog("IntegerPartLength: ${dotIndex!!-1}")
                return dotIndex!!-1
            }else{
                debugLog("IntegerPartLength: ${currentNumberDigits.size - 1}")
                return currentNumberDigits.size - 1
            }
        }else{
            if (dotIndex!=null){
                debugLog("IntegerPartLength: ${dotIndex!!}")
                return dotIndex!!
            }else{
                debugLog("IntegerPartLength: ${currentNumberDigits.size}")
                return currentNumberDigits.size
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
        var i = 0

        val integerPartLength = getIntegerPartLength()
        val numberString:String
        if (getCurrentNumberVal()<0){
            numberStringBuilder.append('-')
            numberString = String(currentNumberDigits.toCharArray()).substring(1)
        }else{
            numberString = String(currentNumberDigits.toCharArray())
        }

        numberString.forEach {
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
        private const val MAX_NUMBER_LENGTH = 10
        private const val MAX_NUMBER_LENGTH_AFTER_DOT = 4

        private enum class CalculatorTask{ADD,SUB,MUL,DIV}
    }
}