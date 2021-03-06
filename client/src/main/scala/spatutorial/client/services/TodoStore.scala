package spatutorial.client.services

import autowire._
import rx._
import spatutorial.client.ukko._
import spatutorial.shared.{TodoItem, Api, _}
import boopickle.Default._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js

case object RefreshTodos

case class UpdateAllTodos(todos: Seq[TodoItem])

case class UpdateTodo(item: TodoItem)

trait TodoStore extends Actor {
  override val name = "TodoStore"

  // define a reactive variable
  private val items = Var(Seq.empty[TodoItem])

  private def updateItems(newItems: Seq[TodoItem]): Unit = {
    // check if todos have really changed
    if (newItems != items()) {
      // use Rx to update, which propagates down to dependencies
      items() = newItems
      newItems.foreach(println)
    }
  }

  override def receive = {
    case RefreshTodos =>
      // load all todos from the server
      AjaxClient[Api].getTodos().call().foreach { todos =>
        updateItems(todos)
      }
      AjaxClient[Api].getMessage("1234").call().foreach {
        case Success =>
          js.Dynamic.global.console("yeepee")
        case Failure =>
          js.Dynamic.global.console("Failure")
      }
    case UpdateAllTodos(todos) =>
      updateItems(todos)
  }

  // return as Rx to prevent mutation in dependencies
  def todos:Rx[Seq[TodoItem]] = Rx { items() }
}

// create a singleton instance of TodoStore
object TodoStore extends TodoStore {
  // register this actor with the dispatcher
  MainDispatcher.register(this)
}

object TodoActions {
  def updateTodo(item: TodoItem) = {
    // inform the server to update/add the item
    AjaxClient[Api].updateTodo(item).call().foreach { todos =>
      MainDispatcher.dispatch(UpdateAllTodos(todos))
    }
  }

  def deleteTodo(item: TodoItem) = {
    // tell server to delete a todo
    AjaxClient[Api].deleteTodo(item.id).call().foreach { todos =>
      MainDispatcher.dispatch(UpdateAllTodos(todos))
    }
  }
}
