package com.github.BambooTuna.cqrs_es_practice.serialization

import java.nio.ByteBuffer

import akka.serialization.SerializerWithStringManifest
import com.github.BambooTuna.cqrs_es_practice.model.BankAccountAggregate._
import boopickle.Default._

class BankAccountSerializer extends SerializerWithStringManifest {
  val BankAccountOpenedManifest = classOf[BankAccountOpened].getName
  val BankAccountDepositedManifest = classOf[BankAccountDeposited].getName
  val BankAccountWithdrawnManifest = classOf[BankAccountWithdrawn].getName
  val BankAccountClosedManifest = classOf[BankAccountClosed].getName

  override def identifier: Int = 51109916

  override def manifest(o: AnyRef): String = o match {
    case orig: BankAccountOpened    => BankAccountOpenedManifest
    case orig: BankAccountDeposited => BankAccountDepositedManifest
    case orig: BankAccountWithdrawn => BankAccountWithdrawnManifest
    case orig: BankAccountClosed    => BankAccountClosedManifest
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case orig: BankAccountOpened    => Pickle.intoBytes(orig).array()
    case orig: BankAccountDeposited => Pickle.intoBytes(orig).array()
    case orig: BankAccountWithdrawn => Pickle.intoBytes(orig).array()
    case orig: BankAccountClosed    => Pickle.intoBytes(orig).array()
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case BankAccountOpenedManifest =>
        Unpickle.apply[BankAccountOpened].fromBytes(ByteBuffer.wrap(bytes))
      case BankAccountDepositedManifest =>
        Unpickle.apply[BankAccountDeposited].fromBytes(ByteBuffer.wrap(bytes))
      case BankAccountWithdrawnManifest =>
        Unpickle.apply[BankAccountWithdrawn].fromBytes(ByteBuffer.wrap(bytes))
      case BankAccountClosedManifest =>
        Unpickle.apply[BankAccountClosed].fromBytes(ByteBuffer.wrap(bytes))
    }

}
