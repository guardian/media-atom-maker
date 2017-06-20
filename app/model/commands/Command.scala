package model.commands

import com.gu.pandomainauth.model.User
import data.UnpackedDataStores

trait Command extends UnpackedDataStores {
  type T

  def process(): T

  def getUsername (user: User): String = {
    user.email match {
      case "" => user.firstName
      case _ => user.email
    }
  }
}
