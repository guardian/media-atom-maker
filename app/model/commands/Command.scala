package model.commands

import com.gu.media.logging.Logging
import util.atom.MediaAtomImplicits

trait Command extends MediaAtomImplicits with Logging {
  type T

  def process(): T
}
