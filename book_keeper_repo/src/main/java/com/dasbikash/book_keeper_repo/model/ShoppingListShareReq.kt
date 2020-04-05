package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.dasbikash.book_keeper_repo.firebase.FireStoreRefUtils
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import java.util.*

@Keep
@Entity
data class ShoppingListShareReq(
    @PrimaryKey
    var id:String = UUID.randomUUID().toString(),
    @Exclude
    private var userId:String?=null,
    var partnerUserId:String?=null,
    var shoppingListId:String?=null,
    var approvalStatus:ShoppingListApprovalStatus=ShoppingListApprovalStatus.PENDING,
    var modified:Date = Date()
){
    @Ignore
    @Exclude
    fun dbRef(ownerUserId:String):DocumentReference{
        return FireStoreRefUtils
                    .getShoppingListShareRequestCollectionRef()
                    .document(ownerUserId)
                    .collection("request").document(id)
    }

    fun setUserId(userId: String?){
        this.userId = userId
    }

    @Exclude
    fun getUserId():String?{
        return this.userId
    }
}

@Keep
@Entity
data class ShoppingListShareReqLog(
    @PrimaryKey
    var id:String = UUID.randomUUID().toString(),
    @Exclude
    private var userId:String?=null,
    var ownerUserId:String?=null,
    var shoppingListId:String?=null,
    var approvalStatus:ShoppingListApprovalStatus=ShoppingListApprovalStatus.PENDING,
    var modified:Date = Date()
){
    @Ignore
    @Exclude
    fun shareReq(user: User):ShoppingListShareReq{
        return ShoppingListShareReq(id=id,partnerUserId = user.id,shoppingListId = shoppingListId!!,approvalStatus = approvalStatus)
    }

    @Ignore
    @Exclude
    fun dbRef(user: User):DocumentReference{
        return FireStoreRefUtils
            .getShoppingListShareRequestLogCollectionRef()
            .document(user.id)
            .collection("request").document(id)
    }

    fun setUserId(userId: String?){
        this.userId = userId
    }

    @Exclude
    fun getUserId():String?{
        return this.userId
    }
}

@Keep
enum class ShoppingListApprovalStatus{
    PENDING,APPROVED,DENIED
}