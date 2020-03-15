package com.github.BambooTuna.cqrs_es_practice.model

import scala.util.Try

case class BankAccount(bankAccountId: String,
                       isClosed: Boolean,
                       balance: BigDecimal) {
  require(balance >= 0)

  def deposit(money: BigDecimal): Try[BankAccount] = Try {
    require(!this.isClosed)
    copy(balance = this.balance + money)
  }

  def withdraw(money: BigDecimal): Try[BankAccount] = Try {
    require(!this.isClosed)
    copy(balance = this.balance - money)
  }

  def close(): Try[BankAccount] = Try {
    require(!this.isClosed)
    copy(isClosed = true)
  }
}
