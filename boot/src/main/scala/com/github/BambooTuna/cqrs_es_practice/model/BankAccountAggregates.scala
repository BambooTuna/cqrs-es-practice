package com.github.BambooTuna.cqrs_es_practice.model

import akka.actor.{Actor, Props}
import akka.cluster.sharding.{
  ClusterSharding,
  ClusterShardingSettings,
  ShardRegion
}
import com.github.BambooTuna.cqrs_es_practice.model.BankAccountAggregate.BankAccountCommandRequest

class BankAccountAggregates extends Actor {
  val shardName = "bank-accounts"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case cmd: BankAccountCommandRequest =>
      (cmd.bankAccountId, cmd)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: BankAccountCommandRequest =>
      (cmd.bankAccountId.toLong % 12).toString
  }

  ClusterSharding(context.system).start(
    shardName,
    Props(new BankAccountAggregate()),
    ClusterShardingSettings(context.system),
    extractEntityId,
    extractShardId
  )

  override def receive: Receive = {
    case cmd: BankAccountCommandRequest =>
      ClusterSharding(context.system).shardRegion(shardName) forward cmd
  }

}
