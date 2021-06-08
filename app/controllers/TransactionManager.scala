package controllers

import javax.inject._
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import service.models.DataClasses.{TransactionId, TransactionType}
import service.models.dtos.RequestAndResponseDTOs.TransactionDetailsDTO
import service.transaction.TransactionService

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

@Singleton
class TransactionManager @Inject()(val controllerComponents: ControllerComponents,
                                   transactionService: TransactionService) extends BaseController {
  private val logger = Logger.apply("transaction_manager_logger")

  def addNewTransaction(transactionIdLong: Long): Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>
    Json.fromJson[TransactionDetailsDTO](request.body) match {
      case JsSuccess(requestDTO, _) =>
        Try { transactionService.addNewTransaction(TransactionId(transactionIdLong), requestDTO) }.safely {
          _ => Ok("Transaction added successfully!")
        }

      case JsError(errors) =>
        Future.successful(BadRequest(s"Request body cannot be parsed due to $errors"))
    }
  }

  def getTransactionId(transactionIdLong: Long): Action[AnyContent] = Action.async {
    Try {
      val transactionId = TransactionId(transactionIdLong)
      transactionService.getTransactionById(transactionId)
    }.safely { transactionDetailsDTO =>
      Ok(Json.toJson(transactionDetailsDTO))
    }
  }
  def getTransactionByType(transactionTypeString: String): Action[AnyContent] = Action.async {
    Try {
      val transactionType = TransactionType.fromString(transactionTypeString)
      transactionService.getTransactionByType(transactionType)
    }.safely { transactionIds =>
      Ok(Json.toJson(transactionIds))
    }
  }

  def getTotalTransactionValueOfGroup(transactionIdLong: Long): Action[AnyContent] = Action.async {
    Try {
      val transactionId = TransactionId(transactionIdLong)
      transactionService.calculateAndGetTotalTransactionValue(transactionId)
    }.safely { transactionValueDTO =>
      Ok(Json.toJson(transactionValueDTO))
    }
  }

  //// Safely handle return value
  implicit class Safely[T](private val fTry: Try[Future[T]]) {
    def safely(onSuccess: T => Result): Future[Result] = {
      fTry match {
        case Failure(th) =>
          logger.error(s"${th.getMessage}", th)
          Future.successful(InternalServerError(s"Something happened! Please try again later. Error Message: ${th.getMessage}"))
        case Success(f) => f.map { t => onSuccess(t) }.recover { case th =>
          logger.error(s"${th.getMessage}", th)
          InternalServerError(s"Something happened! Please try again later. Error Message: ${th.getMessage}") }
      }
    }
  }
}
