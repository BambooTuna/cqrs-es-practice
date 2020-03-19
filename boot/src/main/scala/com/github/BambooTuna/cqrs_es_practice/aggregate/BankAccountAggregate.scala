package com.github.BambooTuna.cqrs_es_practice.aggregate

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted, SaveSnapshotSuccess, SnapshotOffer}
import com.github.BambooTuna.cqrs_es_practice.aggregate.BankAccountAggregate._
import com.github.BambooTuna.cqrs_es_practice.model.BankAccount

import scala.concurrent.duration._

class BankAccountAggregate extends PersistentActor with ActorLogging {
  context.setReceiveTimeout(120.seconds)

  private var stateOpt: Option[BankAccount] = None

  private def equalsId(requestId: String): Boolean =
    stateOpt match {
      case None =>
        throw new IllegalStateException(
          s"Invalid state: requestId = $requestId")
      case Some(state) =>
        state.bankAccountId == requestId
    }

  private def applyState(event: BankAccountOpened): BankAccount = {
    BankAccount(
      bankAccountId = event.bankAccountId,
      isClosed = false,
      balance = 0
    )
  }

  private def foreachState(f: BankAccount => Unit): Unit =
    stateOpt.filter(!_.isClosed).foreach(f)

  private def tryToSaveSnapshot: Unit =
    if (lastSequenceNr % 5 == 0) {
      println(s"SaveSnapshot: lastSequenceNr=$lastSequenceNr")
      foreachState(saveSnapshot)
    }

  override def receiveRecover: Receive = {
    case event: BankAccountOpened =>
      stateOpt = Some(applyState(event))
    case BankAccountDeposited(_, deposit) =>
      stateOpt = stateOpt.flatMap(_.deposit(deposit).toOption)
    case BankAccountWithdrawn(_, withdraw) =>
      stateOpt = stateOpt.flatMap(_.withdraw(withdraw).toOption)
    case BankAccountClosed(_) =>
      stateOpt = stateOpt.flatMap(_.close().toOption)
    case SnapshotOffer(_, _state: BankAccount) =>
      stateOpt = Some(_state)
    case SaveSnapshotSuccess(metadata) =>
      println(s"receiveRecover: SaveSnapshotSuccess succeeded: $metadata")
    case RecoveryCompleted =>
      println(s"Recovery completed: $persistenceId")
  }

  override def receiveCommand: Receive = {
    case OpenBankAccountRequest(bankAccountId) if stateOpt.isEmpty =>
      persist(BankAccountOpened(bankAccountId)) { event =>
        stateOpt = Some(applyState(event))
        tryToSaveSnapshot
      }
    case GetBalanceRequest(bankAccountId) if equalsId(bankAccountId) =>
      foreachState { state =>
        sender() ! GetBalanceResponse(state.bankAccountId, state.balance)
      }
    case DepositRequest(bankAccountId, deposit) if equalsId(bankAccountId) =>
      persist(BankAccountDeposited(bankAccountId, deposit)) { event =>
        stateOpt = stateOpt.flatMap(_.deposit(event.deposit).toOption)
        tryToSaveSnapshot
      }
    case WithdrawRequest(bankAccountId, withdraw) if equalsId(bankAccountId) =>
      persist(BankAccountWithdrawn(bankAccountId, withdraw)) { event =>
        stateOpt = stateOpt.flatMap(_.withdraw(event.withdraw).toOption)
        tryToSaveSnapshot
      }
    case CloseBankAccountRequest(bankAccountId) if equalsId(bankAccountId) =>
      persist(BankAccountClosed(bankAccountId)) { _ =>
        stateOpt = stateOpt.flatMap(_.close().toOption)
        tryToSaveSnapshot
      }
    case SaveSnapshotSuccess(metadata) =>
      println(s"receiveCommand: SaveSnapshotSuccess succeeded: $metadata")
  }

  override def persistenceId: String = self.path.name
}

object BankAccountAggregate {

  sealed trait BankAccountEvent {
    val bankAccountId: String
  }

  case class BankAccountOpened(bankAccountId: String) extends BankAccountEvent
  case class BankAccountDeposited(bankAccountId: String, deposit: BigDecimal)
      extends BankAccountEvent
  case class BankAccountWithdrawn(bankAccountId: String, withdraw: BigDecimal)
      extends BankAccountEvent
  case class BankAccountClosed(bankAccountId: String) extends BankAccountEvent

  sealed trait BankAccountCommandRequest {
    val bankAccountId: String
  }
  case class OpenBankAccountRequest(bankAccountId: String)
      extends BankAccountCommandRequest

  case class GetBalanceRequest(bankAccountId: String)
      extends BankAccountCommandRequest
  case class GetBalanceResponse(bankAccountId: String, balance: BigDecimal)
      extends BankAccountCommandRequest

  case class DepositRequest(bankAccountId: String, deposit: BigDecimal)
      extends BankAccountCommandRequest
  case class WithdrawRequest(bankAccountId: String, withdraw: BigDecimal)
      extends BankAccountCommandRequest
  case class CloseBankAccountRequest(bankAccountId: String)
      extends BankAccountCommandRequest

}
