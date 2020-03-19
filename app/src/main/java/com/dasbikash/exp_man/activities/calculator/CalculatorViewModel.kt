package com.dasbikash.exp_man.activities.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.dasbikash.android_basic_utils.utils.debugLog
import java.lang.ArithmeticException
import java.lang.StringBuilder
import java.util.*

class CalculatorViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val leftOperand = MutableLiveData<Double?>()
    private val rightOperand = MutableLiveData<Double?>()
    private val operation = MutableLiveData<CalculatorTask?>()

    private val currentNumberDigits:Deque<Char> = LinkedList<Char>()

    private val currentNumberLiveData = MutableLiveData<String>()
    fun getCurrentNumber():LiveData<String> = currentNumberLiveData

    fun getLeftOperand():LiveData<String?> = leftOperand.map { it?.optimizedString(2) }
    fun getRightOperand():LiveData<String?> = rightOperand.map { it?.optimizedString(2) }
    fun getOperation():LiveData<String?> = operation.map { it?.sign }

    init {
        sendCurrentNumberAsString()
        leftOperand.postValue(null)
        rightOperand.postValue(null)
        operation.postValue(null)
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
        leftOperand.postValue(null)
        rightOperand.postValue(null)
        operation.postValue(null)
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

    private fun calculate(operation: CalculatorTask?=null){
        val currentNumber = getCurrentNumberVal()
        if (operation!=null){
            if (leftOperand.value!=null &&
                rightOperand.value!=null &&
                this.operation.value !=null){
                if(currentNumber >0.0) {
                    leftOperand.postValue(currentNumber)
                    this.operation.postValue(operation)
                    rightOperand.postValue(null)
                    clearCurrentNumber()
                }
            }else if (leftOperand.value==null && currentNumber>0.0){
                leftOperand.postValue(currentNumber)
                this.operation.postValue(operation)
                clearCurrentNumber()
            }else if (leftOperand.value!=null &&
                        this.operation.value !=null &&
                        currentNumberDigits.isNotEmpty()){
                calculate(leftOperand.value!!,currentNumber, this.operation.value!!).let {
                    if (it!=null) {
                        rightOperand.postValue(currentNumber)
                        setCurrentNumber(it)
                    }else{
                        setInvalidNumberString()
                    }
                }
            }
        }else{
            if (leftOperand.value!=null &&
                this.operation.value !=null &&
                currentNumberDigits.isNotEmpty()) {
                calculate(leftOperand.value!!, currentNumber, this.operation.value!!).let {
                    if (it!=null) {
                        rightOperand.postValue(currentNumber)
                        setCurrentNumber(it)
                    }else{
                        setInvalidNumberString()
                    }
                }
            }
        }
    }

    private fun setCurrentNumber(number: Double) {
        currentNumberDigits.clear()
        number.optimizedString(5).toCharArray().forEach { currentNumberDigits.addFirst(it) }
        sendCurrentNumberAsString()
    }

    private fun calculate(leftOperand:Double,rightOperand:Double,operation:CalculatorTask):Double?{
        try {
            return when (operation) {
                CalculatorTask.ADD -> leftOperand + rightOperand
                CalculatorTask.SUB -> leftOperand - rightOperand
                CalculatorTask.MUL -> leftOperand * rightOperand
                CalculatorTask.DIV -> leftOperand / rightOperand
            }
        }catch (ex:ArithmeticException){
            ex.printStackTrace()
            return null
        }
    }

    fun addAction() = calculate(CalculatorTask.ADD)
    fun subAction() = calculate(CalculatorTask.SUB)
    fun mulAction() = calculate(CalculatorTask.MUL)
    fun divAction() = calculate(CalculatorTask.DIV)
    fun equalAction() = calculate()

    companion object{
        private const val INVALID_NUMBER_MESSAGE = "Invalid Number!!"
        private val DOT_CHAR = '.'
        private const val MAX_INT_PART_LENGTH = 10
        private const val MAX_DECIMAL_PART_LENGTH = 4

        private enum class CalculatorTask(val sign:String){
            ADD("+"),
            SUB("-"),
            MUL("*"),
            DIV("÷")
        }
    }
}

fun Double.optimizedString(decimalPointCount:Int?=null):String{
    val maxDecimalPoints = 10
    val defaultDecimalPoints = 5
    val strFormat = "%2.${if (decimalPointCount==null || decimalPointCount>=maxDecimalPoints) defaultDecimalPoints else decimalPointCount}f"
    if (this != this.toLong().toDouble()) {
        return String.format(strFormat, this)
    }else{
        return this.toLong().toString()
    }
}