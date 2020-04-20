package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.firebase.FireStoreRefUtils
import com.google.firebase.Timestamp
import java.util.*

@Keep
@Entity()
data class OnlineSlShareReq(
    @PrimaryKey
    override var id: String = "",
    override var partnerId: String? = null,
    override var requesterId: String? = null,
    override var documentPath: String? = null,
    override var approvalStatus: RequestApprovalStatus = RequestApprovalStatus.PENDING,
    override var modified: Timestamp = Timestamp.now()
):OnlineDocShareReq() {

    companion object{
        fun getInstance(onlineDocShareParams:OnlineDocShareParams):OnlineSlShareReq{
            return OnlineSlShareReq(
                id=onlineDocShareParams.shareReqDocId,
                partnerId = onlineDocShareParams.partnerId!!,
                requesterId = AuthRepo.getUserId(),
                documentPath = onlineDocShareParams.documentPath!!
            )
        }

        fun getInstanceForSend(shoppingList: ShoppingList,partner:User):OnlineSlShareReq{
            return OnlineSlShareReq(
                id=UUID.randomUUID().toString(),
                partnerId = shoppingList.userId,
                requesterId = partner.id,
                documentPath = FireStoreRefUtils.getShoppingListCollectionRef().document(shoppingList.id).path,
                approvalStatus = RequestApprovalStatus.APPROVED
            )
        }
    }
}