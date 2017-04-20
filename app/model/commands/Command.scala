package model.commands

import data.UnpackedDataStores
import model.AuditEvent

trait Command extends UnpackedDataStores {
  type T

  def process(): (T, AuditEvent)
}
