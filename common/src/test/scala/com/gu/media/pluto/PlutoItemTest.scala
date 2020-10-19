package com.gu.media.pluto

import com.gu.media.pluto.PlutoItem.numericIdsOnlyFilter
import org.scalatest.{FunSuite, ShouldMatchers}

class PlutoItemTest extends FunSuite with ShouldMatchers {

  test("numericIdsOnlyFilter must filter out items with non-numeric IDs") {
    val numericItems = List(
      PlutoCommission("12345",""),
      PlutoCommission("67890",""),
      PlutoCommission("0","")
    )
    val nonNumericItems = List(
      PlutoCommission("KP-12345678",""),
      PlutoCommission("DR-12345678",""),
      PlutoCommission("asfsdfsds",""),
      PlutoCommission("","")
    )
    val result = (nonNumericItems ++ numericItems).filter(numericIdsOnlyFilter)
    result shouldBe numericItems
    result shouldNot be(nonNumericItems)
  }

}
