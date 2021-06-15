package service.transaction_group.impl

import com.google.inject.Singleton
import service.models.DataClasses.TransactionGroupAssociationType.Self
import service.models.DataClasses.{TransactionGroupAssociationType, TransactionGroupId, TransactionId}
import service.models._
import service.transaction_group.TransactionGroupService
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TransactionGroupServiceImpl extends TransactionGroupService {
  val transactionGroupQuery = TransactionGroups.query
  val transactionGroupAssociationQuery = TransactionGroupAssociations.query

  override def createNewTransactionGroupAndAssociationsDBIO(transactionId: TransactionId,
                                                            parentTransactionIdOpt: Option[TransactionId]): DBIO[Unit] = {
    for {
      selfTransactionGroupId <- createNewTransactionGroupDBIO()
      res <- createNewTransactionGroupAssociationsDBIO(transactionId, parentTransactionIdOpt, selfTransactionGroupId)
    } yield res
  }

  override def getTransactionsBelongsToItsGroupDBIO(transactionId: TransactionId): DBIO[Seq[TransactionId]] = {
    for {
      selfGroupId <- getSelfGroupId(transactionId)
      transactionIds <- getTransactionsByGroupId(selfGroupId)
    } yield transactionIds
  }

  // PRIVATE METHODS
  private def createNewTransactionGroupAssociationsDBIO(transactionId: TransactionId,
                                                        parentTransactionIdOpt: Option[TransactionId],
                                                        selfTransactionGroupId: TransactionGroupId): DBIO[Unit] = {
    for {
      parentsGroups <- parentTransactionIdOpt match {
        case Some(parentTransactionId) => getGroupIdsForTransaction(parentTransactionId)
        case None => DBIO.successful(Seq.empty[TransactionGroupId])
      }

      parentsGroupsAssociations = parentsGroups.map { gId =>
        TransactionGroupAssociation(transactionId, gId, TransactionGroupAssociationType.Descendant)
      }

      selfGroupAssociation = TransactionGroupAssociation(transactionId, selfTransactionGroupId,
        TransactionGroupAssociationType.Self)

      allAssociations = parentsGroupsAssociations :+ selfGroupAssociation

      _ <- transactionGroupAssociationQuery ++= allAssociations
    } yield ()
  }

  private def createNewTransactionGroupDBIO(): DBIO[TransactionGroupId] = {
    for {
      newTransactionGroupId <- transactionGroupQuery returning transactionGroupQuery.map(_.transactionGroupId) +=
        TransactionGroup(transactionGroupId = TransactionGroupId(-1))
    } yield newTransactionGroupId
  }

  private def getSelfGroupId(transactionId: TransactionId): DBIO[TransactionGroupId] = {
    transactionGroupAssociationQuery.filter(r => r.transactionId === transactionId &&
      r.transactionGroupAssociationType === (Self: TransactionGroupAssociationType))
      .map(_.transactionGroupId).take(1).result.head
  }

  private def getTransactionsByGroupId(transactionGroupId: TransactionGroupId): DBIO[Seq[TransactionId]] = {
    transactionGroupAssociationQuery.filter(_.transactionGroupId === transactionGroupId).map(_.transactionId).result
  }

  private def getGroupIdsForTransaction(transactionId: TransactionId): DBIO[Seq[TransactionGroupId]] = {
    transactionGroupAssociationQuery.filter(_.transactionId === transactionId).map(_.transactionGroupId).result
  }
}
