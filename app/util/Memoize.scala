package util

import com.google.common.base.Suppliers.memoizeWithExpiration
import com.google.common.base.Supplier
import java.time.Duration
import java.util.concurrent.TimeUnit

object Memoize {

  def apply[T](refresh: => T, duration: Duration): Supplier[T] =
    memoizeWithExpiration(
      makeSupplier(refresh),
      duration.toMillis,
      TimeUnit.MILLISECONDS
    )

  private def makeSupplier[T](fn: => T): Supplier[T] = new Supplier[T] {
    override def get(): T = fn
  }
}
