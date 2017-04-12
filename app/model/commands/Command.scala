package model.commands

import data.UnpackedDataStores
import model.Audit

trait Command extends UnpackedDataStores {
  type T

  def process(): (T, Audit)
}
