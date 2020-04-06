package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.*
import com.dasbikash.book_keeper_repo.AuthRepo
import java.util.*

@Keep
@Entity()
data class OnlineDocShareReq(
    @PrimaryKey
    var id: String = "",
    var ownerId: String? = null,
    var partnerUserId: String? = null,
    var documentPath: String? = null,
    var approvalStatus: ShoppingListApprovalStatus = ShoppingListApprovalStatus.PENDING,
    var modified: Date = Date()
) {
    companion object{
        fun getInstance(onlineDocShareParams:OnlineDocShareParams):OnlineDocShareReq{
            return OnlineDocShareReq(
                id=onlineDocShareParams.shareReqDocId,
                ownerId = onlineDocShareParams.ownerId!!,
                partnerUserId = AuthRepo.getUserId(),
                documentPath = onlineDocShareParams.documentPath!!
            )
        }
    }
}