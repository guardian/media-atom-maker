package model.commands

import data.UnpackedDataStores
import com.gu.pandomainauth.model.{User => PandaUser}

trait Command extends UnpackedDataStores {
  type T

  def process(): T

  def getUsername(user: PandaUser): String = {
    user.email match {
      case "" => user.firstName
      case _  => user.email
    }
  }
}
