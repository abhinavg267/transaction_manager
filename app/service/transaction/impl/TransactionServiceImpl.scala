package service.transaction.impl

import com.google.inject.{Inject, Singleton}
import service.models.DataClasses.{Price, TransactionId, TransactionType}
import service.models.dtos.RequestAndResponseDTOs.{TransactionDetailsDTO, TransactionValueDTO}
import service.models.{Transaction, Transactions}
import service.transaction.TransactionService
import service.transaction_group.TransactionGroupService
import slick.jdbc.H2Profile.api._
import utils.DataBase.{h2DataBase => db}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TransactionServiceImpl @Inject()(transactionGroupService: TransactionGroupService) extends TransactionService {
  private val transactionsQuery = Transactions.query

  override def addNewTransaction(transactionId: TransactionId, requestDTO: TransactionDetailsDTO): Future[Unit] = {
    db.run {
      for {
        rowsAdded <- _addNewTransactionDBIO(transactionId, requestDTO.`type`, requestDTO.amount, requestDTO.parent_id)
        _ <- transactionGroupService.createNewTransactionGroupAndAssociationsDBIO(transactionId, requestDTO.parent_id)
      } yield ()
    }
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
      allAssociatedTransactionIds <- transactionGroupService.getTransactionsBelongsToItsGroupDBIO(transactionId)
      allAssociatedTransactions <- _getTransactionByIdsDBIO(allAssociatedTransactionIds)
    } yield {
      allAssociatedTransactions.foldLeft(Price.zeroPrice) { case (priceYet, transaction) =>
        priceYet + transaction.transactionAmount
      }
    }
  }

  private def _getTransactionByIdDBIO(transactionId: TransactionId): DBIO[Transaction] = {
    _getTransactionByIdsDBIO(Seq(transactionId)).map(_.headOption.getOrElse(
      throw new Exception(s"Cannot find transaction with transactionId: $transactionId")))
  }

  private def _getTransactionByIdsDBIO(transactionIds: Seq[TransactionId]): DBIO[Seq[Transaction]] = {
    transactionsQuery.filter(_.transactionId.inSetBind(transactionIds)).result
  }

  private def _getTransactionsByTypesDBIO(transactionTypesOpt: Option[Set[TransactionType]]): DBIO[Seq[Transaction]] = {
    transactionsQuery.filter { r =>
        transactionTypesOpt.fold(true: Rep[Boolean])(r.transactionType.inSetBind(_))
    }.result
  }

  private def _addNewTransactionDBIO(transactionId: TransactionId, transactionType: TransactionType,
                                     transactionAmount: Price, parentTransactionIdOpt: Option[TransactionId])
  : DBIO[Int] = {
    for {
      rowsAdded <- transactionsQuery +=
        Transaction(transactionId, transactionType, transactionAmount, parentTransactionIdOpt)
    } yield rowsAdded
  }
}
