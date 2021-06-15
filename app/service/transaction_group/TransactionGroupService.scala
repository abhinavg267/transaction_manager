package service.transaction_group

import com.google.inject.ImplementedBy
import service.models.DataClasses.TransactionId
import service.models.Transaction
import service.transaction_group.impl.TransactionGroupServiceImpl
import slick.dbio.DBIO

@ImplementedBy(classOf[TransactionGroupServiceImpl])
trait TransactionGroupService {
  def createNewTransactionGroupAndAssociationsDBIO(transactionId: TransactionId,
                                                   parentTransactionIdOpt: Option[TransactionId]): DBIO[Unit]

  def getTransactionsBelongsToItsGroupDBIO(transactionId: TransactionId): DBIO[Seq[TransactionId]]
}
