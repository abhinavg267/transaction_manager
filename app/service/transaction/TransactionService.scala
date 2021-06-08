package service.transaction

import com.google.inject.ImplementedBy
import service.models.DataClasses.{TransactionId, TransactionType}
import service.models.dtos.RequestAndResponseDTOs.{TransactionDetailsDTO, TransactionValueDTO}
import service.transaction.impl.TransactionServiceImpl

import scala.concurrent.Future

@ImplementedBy(classOf[TransactionServiceImpl])
trait TransactionService {
  def addNewTransaction(transactionId: TransactionId, requestDTO: TransactionDetailsDTO): Future[Unit]

  def getTransactionById(transactionId: TransactionId): Future[TransactionDetailsDTO]

  def getTransactionByType(transactionType: TransactionType): Future[Seq[TransactionId]]

  def calculateAndGetTotalTransactionValue(transactionId: TransactionId): Future[TransactionValueDTO]
}
