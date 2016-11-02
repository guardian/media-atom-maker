package model.commands

trait Command {
  type T

  def process(): T
}
