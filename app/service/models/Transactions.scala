package service.models

import service.models.DataClasses.{Price, TransactionId, TransactionType}
import slick.jdbc.H2Profile.api._

case class Transaction(transactionId: TransactionId, transactionType: TransactionType,
                       transactionAmount: Price, parentTransactionIdOpt: Option[TransactionId])

class Transactions(tag: Tag) extends Table[Transaction](tag, "transactions") {
  def transactionId = column[TransactionId]("transaction_id", O.PrimaryKey)
  def transactionType = column[TransactionType]("transaction_type")
  def transactionAmount = column[Price]("amount")
  def parentTransactionId = column[Option[TransactionId]]("parent_transaction_id")

  // ForeignKey from parentTransactionId to transactionId
  def parentTransactionIdFk = foreignKey(s"${tableName}_parent_trans_id_fk",
    parentTransactionId, Transactions.query)(_.transactionId.?)

  override def * = (transactionId, transactionType, transactionAmount, parentTransactionId) <> (
    (Transaction.apply _).tupled, Transaction.unapply)
}

object Transactions {
  val query = TableQuery[Transactions]
}
