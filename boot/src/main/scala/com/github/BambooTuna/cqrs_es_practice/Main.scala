package com.github.BambooTuna.cqrs_es_practice

import java.util.concurrent.TimeUnit

import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.persistence.query.{EventEnvelope, Offset, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.github.BambooTuna.cqrs_es_practice.model.BankAccountAggregate._
import com.github.BambooTuna.cqrs_es_practice.model.BankAccountAggregates

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("bank-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val readJournal =
    PersistenceQuery(system)
      .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  def eventsByTagSource(tag: String, seqNr: Long) = {
    readJournal.eventsByTag(classOf[BankAccountEvent].getName,
                            Offset.sequence(seqNr))
//    readJournal.currentPersistenceIds()
  }

  Source
    .repeat(1)
    .throttle(1, 1.seconds)
    .mapAsync(1) { _ =>
      Future.successful(0)
    }
    .flatMapConcat { lastSeqNr =>
      eventsByTagSource(classOf[BankAccountEvent].getName, lastSeqNr + 1)
    }
    .runForeach(a => {
      println("-------------------")
      println(a)
    })

  val bankAccountAggregatesRef: ActorRef =
    system.actorOf(Props(new BankAccountAggregates()), "sharded-bank-accounts")
  implicit val timeout = Timeout(5000, TimeUnit.MILLISECONDS)
  bankAccountAggregatesRef ! OpenBankAccountRequest("1")
  bankAccountAggregatesRef ! OpenBankAccountRequest("2")
  bankAccountAggregatesRef ! DepositRequest("1", 1000)
  bankAccountAggregatesRef ! DepositRequest("2", 2000)
  (bankAccountAggregatesRef ? GetBalanceRequest("1")).onComplete(println)
  (bankAccountAggregatesRef ? GetBalanceRequest("2")).onComplete(println)
  bankAccountAggregatesRef ! WithdrawRequest("1", 100)
  bankAccountAggregatesRef ! WithdrawRequest("2", 200)
  (bankAccountAggregatesRef ? GetBalanceRequest("1")).onComplete(println)
  (bankAccountAggregatesRef ? GetBalanceRequest("2")).onComplete(println)

//  bankAccountAggregatesRef ! CloseBankAccountRequest("1")
  (bankAccountAggregatesRef ? GetBalanceRequest("1")).onComplete(println)

  bankAccountAggregatesRef ! DepositRequest("2", 1)
  bankAccountAggregatesRef ! DepositRequest("2", 2)
  bankAccountAggregatesRef ! DepositRequest("2", 3)
  bankAccountAggregatesRef ! DepositRequest("2", 4)
  bankAccountAggregatesRef ! DepositRequest("2", 5)
  bankAccountAggregatesRef ! DepositRequest("2", 6)
  (bankAccountAggregatesRef ? GetBalanceRequest("2")).onComplete(println)

}
