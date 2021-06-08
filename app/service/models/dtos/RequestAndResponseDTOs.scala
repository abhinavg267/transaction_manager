package service.models.dtos

import play.api.libs.json.{Json, OFormat}
import service.models.DataClasses.{Price, TransactionId, TransactionType}
import service.models.Transaction

case object RequestAndResponseDTOs {
  case class TransactionDetailsDTO(amount: Price, `type`: TransactionType,
                                   parent_id: Option[TransactionId])
  object TransactionDetailsDTO {
    def fromTransaction(transaction: Transaction): TransactionDetailsDTO = {
      TransactionDetailsDTO(transaction.transactionAmount, transaction.transactionType,
        transaction.parentTransactionIdOpt)
    }

    implicit val formats: OFormat[TransactionDetailsDTO] = Json.format[TransactionDetailsDTO]
  }

  case class TransactionValueDTO(sum: Price)
  object TransactionValueDTO {
    implicit val formats: OFormat[TransactionValueDTO] = Json.format[TransactionValueDTO]
  }
}
