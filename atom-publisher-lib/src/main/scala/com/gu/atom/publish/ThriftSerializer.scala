package com.gu.atom.publish

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

import com.twitter.scrooge.ThriftStruct
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TIOStreamTransport

trait ThriftSerializer[T <: ThriftStruct] {

  lazy val compressionByte = 0

  def serializeEvent(event: T): ByteBuffer = {
    val out = new ByteArrayOutputStream()
    val transport = new TIOStreamTransport(out)
    val protocol = new TCompactProtocol(transport)
    out.write(compressionByte)
    event.write(protocol)
    ByteBuffer.wrap(out.toByteArray())
  }

}
