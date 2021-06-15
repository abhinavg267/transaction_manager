package service.models

import service.models.DataClasses.{TransactionGroupAssociationType, TransactionGroupId, TransactionId}
import slick.jdbc.H2Profile.api._

case class TransactionGroupAssociation(transactionId: TransactionId, transactionGroupId: TransactionGroupId,
                                       transactionGroupAssociationType: TransactionGroupAssociationType)

class TransactionGroupAssociations(tag: Tag) extends Table[TransactionGroupAssociation](tag, "transaction_group_associations") {
  def transactionId = column[TransactionId]("transaction_id")
  def transactionGroupId = column[TransactionGroupId]("transaction_group_id")
  def transactionGroupAssociationType = column[TransactionGroupAssociationType]("transaction_group_association_type")

  def idx = index(s"${tableName}_idx", on = (transactionId, transactionGroupId), unique = true)

  override def * = (transactionId, transactionGroupId,
    transactionGroupAssociationType) <> ((TransactionGroupAssociation.apply _).tupled, TransactionGroupAssociation.unapply)
}

object TransactionGroupAssociations {
  val query = TableQuery[TransactionGroupAssociations]
}
