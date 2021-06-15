package service.models

import service.models.DataClasses.TransactionGroupId
import slick.jdbc.H2Profile.api._

case class TransactionGroup(transactionGroupId: TransactionGroupId)

class TransactionGroups(tag: Tag) extends Table[TransactionGroup](tag, "transaction_groups") {
  def transactionGroupId = column[TransactionGroupId]("transaction_group_id", O.AutoInc, O.PrimaryKey)

  override def * = transactionGroupId <> (TransactionGroup.apply, TransactionGroup.unapply)
}

object TransactionGroups {
  val query = TableQuery[TransactionGroups]
}
