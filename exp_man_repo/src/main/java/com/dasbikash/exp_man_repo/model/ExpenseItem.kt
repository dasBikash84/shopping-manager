package com.dasbikash.exp_man_repo.model

class ExpenseItem {
    var id:String=""
    var productId:String?=null
    var name:String?=null
    var brandName:String?=null
    var unitPrice:Double=0.0
    var qty:Int=1
    var unitId: String?=null
    var unitOfMeasure: UnitOfMeasure?=null
    var created:Long?=null
    var modified:Long?=null
}