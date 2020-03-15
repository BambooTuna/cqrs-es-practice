package com.github.BambooTuna.cqrs_es_practice

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.github.BambooTuna.cqrs_es_practice.model.BankAccountAggregate

import scala.concurrent.ExecutionContextExecutor

object Main extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val readJournal =
    PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
  val source: Source[EventEnvelope, NotUsed] =
    readJournal.eventsByPersistenceId("persistenceId", 0, Long.MaxValue)
  source.runForeach { event =>
    println("Event: " + event)
  }

  val persistentActor = system.actorOf(Props[BankAccountAggregate], "BankAccountAggregate")


}
