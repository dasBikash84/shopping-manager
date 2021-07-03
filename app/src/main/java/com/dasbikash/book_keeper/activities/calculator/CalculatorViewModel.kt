package com.dasbikash.book_keeper.activities.calculator

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper.utils.formatForDisplay
import com.dasbikash.book_keeper.utils.optimizedString
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class CalculatorViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val leftOperand = MutableLiveData<Double?>()
    private val rightOperand = MutableLiveData<Double?>()
    private val operation = MutableLiveData<CalculatorTask?>()

    private val currentNumberDigits:Deque<Char> = LinkedList<Char>()

    private var rightAfterCal = false

    private val currentNumberLiveData = MutableLiveData<String>()
    fun getCurrentNumber():LiveData<String> = currentNumberLiveData

    fun getLeftOperand():LiveData<String?> = leftOperand.map { it?.formatForDisplay()}
    fun getRightOperand():LiveData<String?> = rightOperand.map { it?.formatForDisplay()}
    fun getOperation():LiveData<String?> = operation.map { it?.sign }

    init {
        sendCurrentNumberAsString()
        leftOperand.postValue(null)
        rightOperand.postValue(null)
        operation.postValue(null)
    }

    fun addPressedDigit(char: Char){
        debugLog(String(currentNumberDigits.toCharArray()))

        if (rightAfterCal){
            rightAfterCal = false
            clearCurrentNumber()
        }

        if (currentNumberDigits.size >= MAX_NUM_LENGTH){return}

        if (char == DOT_CHAR) {
            if (currentNumberDigits.isNotEmpty()){
                if(currentNumberDigits.contains(DOT_CHAR)) {
                    return
                }
            }else {
                currentNumberDigits.add('0')
            }
        }
        currentNumberDigits.add(char)
        sendCurrentNumberAsString()
    }

    fun removeLast(){
        if (rightAfterCal){rightAfterCal=false}
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
            sendToDisplay("0")
            return
        }

        val numberStringBuilder = StringBuilder()
        val integerPartLength = getIntegerPartLength()
        var i = 0

        String(currentNumberDigits.toCharArray()).let {
//            val sanitizedString = sanitizeNumberString(it)
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
        sendToDisplay(numberStringBuilder.toString())
    }

    private fun sanitizeNumberString(numberString: String):String {
        return trailingZeroMatcher.matchEntire(numberString)?.destructured?.toList()?.get(0) ?: numberString
    }

    private fun sendToDisplay(numberString:String){
        currentNumberLiveData.postValue(
            numberString
        )
    }

    fun clearAll(){
        leftOperand.postValue(null)
        rightOperand.postValue(null)
        operation.postValue(null)
        clearCurrentNumber()
    }

    fun clearCurrentNumber(){
        rightAfterCal=false
        currentNumberDigits.clear()
        sendCurrentNumberAsString()
    }

    private fun getCurrentNumberVal():Double{
        if (currentNumberDigits.isEmpty()){return 0.0}
        if (isInvalidNumber()){return 0.0}
        return String(currentNumberDigits.toCharArray()).toDouble()
    }

    private fun setInvalidNumberString(){
        currentNumberDigits.clear()
        INVALID_NUMBER_MESSAGE.toCharArray().forEach { currentNumberDigits.add(it) }
        sendCurrentNumberAsString()
    }

    fun invertAction() = unaryOperation({1 / it},10)
    fun squareAction() = unaryOperation({Math.pow(it,2.0)})
    fun sqrtAction() = unaryOperation({Math.pow(it,0.5)})

    private fun unaryOperation(action:(Double)->Double,decimalPointCount:Int?=null){
        if (isInvalidNumber()){return}
        rightAfterCal = false
        try {
            action(getCurrentNumberVal()).let {
                currentNumberDigits.clear()
                it.optimizedString(decimalPointCount).forEach { currentNumberDigits.add(it) }
                sendCurrentNumberAsString()
            }
        }catch (ex:Throwable){
            ex.printStackTrace()
            setInvalidNumberString()
        }
    }

    private fun isInvalidNumber():Boolean = String(currentNumberDigits.toCharArray()) == INVALID_NUMBER_MESSAGE

    private fun calculate(context: Context,operation: CalculatorTask?=null){
        val currentNumber = getCurrentNumberVal()
        if (operation!=null){
            if (leftOperand.value!=null &&
                rightOperand.value!=null &&
                this.operation.value !=null){
                if(currentNumber !=0.0) {
                    leftOperand.postValue(currentNumber)
                    this.operation.postValue(operation)
                    rightOperand.postValue(null)
                    clearCurrentNumber()
                }
            }else if (leftOperand.value==null && currentNumber!=0.0){
                leftOperand.postValue(currentNumber)
                this.operation.postValue(operation)
                clearCurrentNumber()
            }else if (leftOperand.value!=null &&
                        rightOperand.value==null &&
                        this.operation.value !=null){
                if (currentNumberDigits.isNotEmpty()) {
                    calculate(context,leftOperand.value!!, currentNumber, this.operation.value!!).let {
                        if (it != null) {
                            rightOperand.postValue(currentNumber)
                            setCurrentNumber(it)
                        } else {
                            setInvalidNumberString()
                        }
                    }
                }else{
                    this.operation.postValue(operation)
                }
            }
        }else{
            if (leftOperand.value!=null &&
                this.operation.value !=null) {
                if (rightOperand.value!=null) {
                    calculate(context,
                        leftOperand.value!!,
                        rightOperand.value!!,
                        this.operation.value!!
                    ).let {
                        if (it != null) {
                            debugLog("result: $it")
                            setCurrentNumber(it)
                        } else {
                            setInvalidNumberString()
                        }
                    }
                }else if (currentNumberDigits.isNotEmpty()) {
                    calculate(context,leftOperand.value!!, currentNumber, this.operation.value!!).let {
                        if (it != null) {
                            debugLog("result: $it")
                            rightOperand.postValue(currentNumber)
                            setCurrentNumber(it)
                        } else {
                            setInvalidNumberString()
                        }
                    }
                }

            }
        }
    }

    private fun setCurrentNumber(number: Double) {
        currentNumberDigits.clear()
        number.optimizedString(5).toCharArray().forEach { currentNumberDigits.add(it) }
        sendCurrentNumberAsString()
    }

    private fun calculate(context: Context,leftOperand:Double,rightOperand:Double,operation:CalculatorTask):Double?{
        rightAfterCal = true
        try {
            val returnValue =  when (operation) {
                CalculatorTask.ADD -> leftOperand + rightOperand
                CalculatorTask.SUB -> leftOperand - rightOperand
                CalculatorTask.MUL -> leftOperand * rightOperand
                CalculatorTask.DIV -> leftOperand / rightOperand
            }
            GlobalScope.launch { CalculatorHistory.saveHistory(context, leftOperand, rightOperand,returnValue, operation) }
            return returnValue
        }catch (ex:Throwable){
            ex.printStackTrace()
            return null
        }
    }

    fun addAction(context: Context) = calculate(context,CalculatorTask.ADD)
    fun subAction(context: Context) = calculate(context,CalculatorTask.SUB)
    fun mulAction(context: Context) = calculate(context,CalculatorTask.MUL)
    fun divAction(context: Context) = calculate(context,CalculatorTask.DIV)
    fun equalAction(context: Context) = calculate(context)

    suspend fun loadFromMem(context: Context){
        debugLog("loadFromMem")
        SharedPreferenceUtils.getDefaultInstance().getDataSuspended(context, MEM_ENTRY_SP_KEY,Double::class.java).let {
            debugLog("loadFromMem: $it")
            clearCurrentNumber()
            it?.let {
                setCurrentNumber(it)
                return
            }
        }
    }

    fun clearMem(context: Context){
        debugLog("clearMem")
        SharedPreferenceUtils.getDefaultInstance().removeKey(context, MEM_ENTRY_SP_KEY)
    }

    suspend fun addToMem(context: Context){
        debugLog("addToMem")
        SharedPreferenceUtils.getDefaultInstance().getDataSuspended(context, MEM_ENTRY_SP_KEY,Double::class.java).let {
            debugLog("From Mem: $it")
            if (it==null) {getCurrentNumberVal()} else {it+getCurrentNumberVal()}.apply {
                debugLog("addToMem: $this")
                SharedPreferenceUtils
                    .getDefaultInstance()
                    .saveDataSuspended(context,this,MEM_ENTRY_SP_KEY)
            }
        }
    }

    suspend fun subFromMem(context: Context){
        debugLog("subFromMem")
        SharedPreferenceUtils.getDefaultInstance().getDataSuspended(context, MEM_ENTRY_SP_KEY,Double::class.java).let {
            debugLog("From Mem: it")
            if (it==null) {0-getCurrentNumberVal()} else {it-getCurrentNumberVal()}.apply {
                debugLog("subFromMem: $this")
                SharedPreferenceUtils
                    .getDefaultInstance()
                    .saveDataSuspended(context,this,MEM_ENTRY_SP_KEY)
            }
        }
    }

    fun percentAction(){
        if (leftOperand.value!=null &&
            operation.value!=null &&
            currentNumberDigits.isNotEmpty()){
            val perValue = (leftOperand.value!! * getCurrentNumberVal()) / 100.00
            setCurrentNumber(perValue)
            rightOperand.postValue(perValue)
        }
    }

    companion object{
        private const val MEM_ENTRY_SP_KEY = "com.dasbikash.exp_man.activities.calculator.MEM_ENTRY_SP_KEY"
        private const val INVALID_NUMBER_MESSAGE = "Invalid Number!!"
        private val DOT_CHAR = '.'
        private const val MAX_NUM_LENGTH = 15
        private const val MAX_INT_PART_LENGTH = 10
        private const val MAX_DECIMAL_PART_LENGTH = 4
        private val trailingZeroMatcher = Regex("(-?\\d+\\...?+)(0+)")
        @Keep
        enum class CalculatorTask(val sign:String){
            ADD("+"),
            SUB("-"),
            MUL("*"),
            DIV("÷")
        }
    }
}