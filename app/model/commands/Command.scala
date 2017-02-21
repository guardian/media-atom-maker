package model.commands

import data.HasDataStores

trait Command extends HasDataStores {
  type T

  def process(): T
}
