package service.transaction.impl

import com.google.inject.{Inject, Singleton}
import service.models.DataClasses.{Price, TransactionId, TransactionType}
import service.models.dtos.RequestAndResponseDTOs.{TransactionDetailsDTO, TransactionValueDTO}
import service.models.{Transaction, Transactions}
import service.transaction.TransactionService
import slick.jdbc.H2Profile.api._
import utils.DataBase.{h2DataBase => db}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TransactionServiceImpl extends TransactionService {
  private val transactionsQuery = Transactions.query

  override def addNewTransaction(transactionId: TransactionId, requestDTO: TransactionDetailsDTO): Future[Unit] = {
    db.run {
      _addNewTransactionDBIO(transactionId, requestDTO.`type`, requestDTO.amount, requestDTO.parent_id)
    }.map(_ => ())
  }

  override def getTransactionById(transactionId: TransactionId): Future[TransactionDetailsDTO] = {
    db.run {
      _getTransactionByIdDBIO(transactionId).map(TransactionDetailsDTO.fromTransaction)
    }
  }

  override def getTransactionByType(transactionType: TransactionType): Future[Seq[TransactionId]] = {
    db.run {
      _getTransactionsByTypesDBIO(transactionTypesOpt = Some(Set(transactionType))).map(_.map(_.transactionId))
    }
  }

  override def calculateAndGetTotalTransactionValue(transactionId: TransactionId): Future[TransactionValueDTO] = {
    db.run(getTotalTransactionValueDBIO(transactionId)).map {
      TransactionValueDTO(_)
    }
  }

  private def getTotalTransactionValueDBIO(transactionId: TransactionId): DBIO[Price] = {
    for {
      thisTransaction <- _getTransactionByIdDBIO(transactionId)
      res <- thisTransaction.parentTransactionIdOpt match {
        case Some(parentTransactionId) => getTotalTransactionValueDBIO(parentTransactionId)
        case None => DBIO.successful(Price.zeroPrice)
      }
    } yield thisTransaction.transactionAmount + res
  }

  private def _getTransactionByIdDBIO(transactionId: TransactionId): DBIO[Transaction] = {
    transactionsQuery.filter(_.transactionId === transactionId).take(1).result.map(_.headOption.getOrElse(
      throw new Exception(s"Cannot find transaction with transactionId: $transactionId")))
  }

  private def _getTransactionsByTypesDBIO(transactionTypesOpt: Option[Set[TransactionType]]): DBIO[Seq[Transaction]] = {
    transactionsQuery.filter { r =>
        transactionTypesOpt.fold(true: Rep[Boolean])(r.transactionType.inSetBind(_))
    }.result
  }

  private def _addNewTransactionDBIO(transactionId: TransactionId, transactionType: TransactionType,
                                     transactionAmount: Price, parentTransactionIdOpt: Option[TransactionId])
  : DBIO[Unit] = {
    for {
      _ <- transactionsQuery +=
        Transaction(transactionId, transactionType, transactionAmount, parentTransactionIdOpt)
    } yield ()
  }
}
