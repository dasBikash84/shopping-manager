package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dasbikash.book_keeper_repo.AuthRepo
import java.util.*

@Keep
@Entity()
data class OnlineSlShareReq(
    @PrimaryKey
    override var id: String = "",
    override var ownerId: String? = null,
    override var partnerUserId: String? = null,
    override var documentPath: String? = null,
    override var approvalStatus: RequestApprovalStatus = RequestApprovalStatus.PENDING,
    override var modified: Date = Date()
):OnlineDocShareReq() {

    companion object{
        fun getInstance(onlineDocShareParams:OnlineDocShareParams):OnlineSlShareReq{
            return OnlineSlShareReq(
                id=onlineDocShareParams.shareReqDocId,
                ownerId = onlineDocShareParams.ownerId!!,
                partnerUserId = AuthRepo.getUserId(),
                documentPath = onlineDocShareParams.documentPath!!
            )
        }
    }
}