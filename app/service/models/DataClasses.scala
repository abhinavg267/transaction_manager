package service.models
import play.api.libs.json.{Format, JsNumber, JsResult, JsString, JsValue, Json, OFormat}
import slick.ast.BaseTypedType
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcType
import utils.{StringCompanion, WithAsString}

object DataClasses {
  case class TransactionId(id: Long)
  object TransactionId {
    implicit val formats: Format[TransactionId] = new Format[TransactionId] {
      override def reads(json: JsValue): JsResult[TransactionId] = json.validate[Long].map(TransactionId(_))

      override def writes(o: TransactionId): JsValue = JsNumber(o.id)
    }

    implicit val dbMapping: JdbcType[TransactionId] with BaseTypedType[TransactionId] =
      MappedColumnType.base[TransactionId, Long](_.id, TransactionId(_))
  }

  case class TransactionGroupId(id: Long)
  object TransactionGroupId {
    implicit val dbMapping: JdbcType[TransactionGroupId] with BaseTypedType[TransactionGroupId] =
      MappedColumnType.base[TransactionGroupId, Long](_.id, TransactionGroupId(_))
  }

  case class Price(amount: BigDecimal) {
    def +(thatPrice: Price): Price = {
      Price(amount + thatPrice.amount)
    }
  }
  object Price {
    val zeroPrice: Price = Price(amount = 0)

    implicit val formats: Format[Price] = new Format[Price] {
      override def reads(json: JsValue): JsResult[Price] = json.validate[BigDecimal].map(Price(_))
      override def writes(o: Price): JsValue = JsNumber(o.amount)
    }

    implicit val dbMapping: JdbcType[Price] with BaseTypedType[Price] =
      MappedColumnType.base[Price, BigDecimal](_.amount, Price(_))
  }

  // TRANSACTION TYPE
  sealed trait TransactionType extends WithAsString
  object TransactionType extends StringCompanion[TransactionType] {
    case object TransactionType1 extends TransactionType {
      override def asString: String = "TRANSACTION_TYPE_1"
    }

    case object TransactionType2 extends TransactionType {
      override def asString: String = "TRANSACTION_TYPE_2"
    }

    override def all: Set[TransactionType] = Set(TransactionType1, TransactionType2)

    implicit val formats: Format[TransactionType] = new Format[TransactionType] {
      override def reads(json: JsValue): JsResult[TransactionType] = json.validate[String].map(fromString)
      override def writes(o: TransactionType): JsValue = JsString(o.asString)
    }

    implicit val dbMapping: JdbcType[TransactionType] with BaseTypedType[TransactionType] =
      MappedColumnType.base[TransactionType, String](_.asString, fromString)
  }

  // TRANSACTION_GROUP_ASSOCIATION_TYPE
  sealed trait TransactionGroupAssociationType extends WithAsString
  object TransactionGroupAssociationType extends StringCompanion[TransactionGroupAssociationType] {
    case object Self extends TransactionGroupAssociationType {
      override def asString: String = "SELF"
    }

    case object Descendant extends TransactionGroupAssociationType {
      override def asString: String = "DESCENDANT"
    }

    override def all: Set[TransactionGroupAssociationType] = Set(Self, Descendant)

    implicit val formats: Format[TransactionGroupAssociationType] = new Format[TransactionGroupAssociationType] {
      override def reads(json: JsValue): JsResult[TransactionGroupAssociationType] = json.validate[String].map(fromString)
      override def writes(o: TransactionGroupAssociationType): JsValue = JsString(o.asString)
    }

    implicit val dbMapping: JdbcType[TransactionGroupAssociationType] with BaseTypedType[TransactionGroupAssociationType] =
      MappedColumnType.base[TransactionGroupAssociationType, String](_.asString, fromString)
  }
}

