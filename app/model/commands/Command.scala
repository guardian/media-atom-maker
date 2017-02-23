package model.commands

import data.UnpackedDataStores

trait Command extends UnpackedDataStores {
  type T

  def process(): T
}
