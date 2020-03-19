package com.github.BambooTuna.cqrs_es_practice

import java.util.concurrent.TimeUnit

import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.persistence.query.{EventEnvelope, Offset, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.github.BambooTuna.cqrs_es_practice.aggregate.BankAccountAggregate._
import com.github.BambooTuna.cqrs_es_practice.aggregate.BankAccountAggregates
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object Main extends App {

  implicit val system: ActorSystem =
    ActorSystem("bank-system", config = ConfigFactory.load())
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def eventsByTagSource(tag: String, seqNr: Long) = {
    val readJournal =
      PersistenceQuery(system)
        .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
    readJournal.eventsByTag(classOf[BankAccountEvent].getName,
                            Offset.sequence(seqNr))
  }

  val bankAccountAggregatesRef: ActorRef =
    system.actorOf(Props(new BankAccountAggregates()), "sharded-bank-accounts")
  implicit val timeout = Timeout(5000, TimeUnit.MILLISECONDS)
//  bankAccountAggregatesRef ! OpenBankAccountRequest("1")
//  bankAccountAggregatesRef ! OpenBankAccountRequest("2")
//  bankAccountAggregatesRef ! DepositRequest("1", 1000)
//  bankAccountAggregatesRef ! DepositRequest("2", 2000)
//  (bankAccountAggregatesRef ? GetBalanceRequest("1")).onComplete(println)
//  (bankAccountAggregatesRef ? GetBalanceRequest("2")).onComplete(println)
//  bankAccountAggregatesRef ! WithdrawRequest("1", 100)
//  bankAccountAggregatesRef ! WithdrawRequest("2", 200)
//  (bankAccountAggregatesRef ? GetBalanceRequest("1")).onComplete(println)
//  (bankAccountAggregatesRef ? GetBalanceRequest("2")).onComplete(println)
//
//  bankAccountAggregatesRef ! DepositRequest("2", 1)
//  bankAccountAggregatesRef ! DepositRequest("2", 2)
//  bankAccountAggregatesRef ! DepositRequest("2", 3)
//  bankAccountAggregatesRef ! DepositRequest("2", 4)
//  bankAccountAggregatesRef ! DepositRequest("2", 5)
//  bankAccountAggregatesRef ! DepositRequest("2", 6)
//  (bankAccountAggregatesRef ? GetBalanceRequest("2")).onComplete(println)

  Source
    .single(1)
    .mapAsync(1) { _ =>
      Future.successful(0)
    }
    .flatMapConcat { lastSeqNr =>
      eventsByTagSource(classOf[BankAccountEvent].getName, 3)
    }
    .runForeach(println)

  Thread.sleep(5000)
  bankAccountAggregatesRef ! OpenBankAccountRequest("3")

  sys.addShutdownHook {
    system.terminate()
  }
}
