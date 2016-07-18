package com.gu.atom.publish

import com.gu.contentatom.thrift.ContentAtomEvent

import scala.util.Try

trait AtomPublisher {
  def publishAtomEvent(event: ContentAtomEvent): Try[Unit]
}

trait PreviewAtomPublisher extends AtomPublisher

trait LiveAtomPublisher extends AtomPublisher
