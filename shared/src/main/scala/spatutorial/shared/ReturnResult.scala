package spatutorial.shared

import boopickle.Default._

sealed trait ReturnResult

case object Success extends ReturnResult

case object Failure extends ReturnResult

object ReturnResult {
  implicit val priorityPickler: Pickler[ReturnResult] = generatePickler[ReturnResult]
}
