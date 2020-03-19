package com.github.BambooTuna.cqrs_es_practice.model

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import com.github.BambooTuna.cqrs_es_practice.aggregate.BankAccountAggregate.BankAccountEvent

class MyTaggingEventAdapter extends WriteEventAdapter {

  private def withTag(event: Any, tag: String) = Tagged(event, Set(tag))

  private val tagName = classOf[BankAccountEvent].getName

  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = {
    withTag(event, tagName)
  }

}
