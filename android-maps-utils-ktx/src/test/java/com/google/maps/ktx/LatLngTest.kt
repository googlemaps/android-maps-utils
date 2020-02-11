package com.google.maps.ktx

import com.google.maps.android.PolyUtil
import org.junit.Assert
import org.junit.Test

class LatLngTest {
    @Test
    fun testSimplifyEndpointsEqual() {
        val line = PolyUtil.decode(
            "elfjD~a}uNOnFN~Em@fJv@tEMhGDjDe@hG^nF??@lA?n@IvAC`Ay@A{@DwCA{CF_EC{CEi@PBTFDJBJ?V?n@?D@?A@?@?F?F?LAf@?n@@`@@T@~@FpA?fA?p@?r@?vAH`@OR@^ETFJCLD?JA^?J?P?fAC`B@d@?b@A\\@`@Ad@@\\?`@?f@?V?H?DD@DDBBDBD?D?B?B@B@@@B@B@B@D?D?JAF@H@FCLADBDBDCFAN?b@Af@@x@@")
        val simplifiedLine = line.simplify(tolerance = 5.0)
        Assert.assertEquals(20, simplifiedLine.size)
        Assert.assertEquals(line.first(), simplifiedLine.first())
        Assert.assertEquals(line.last(), simplifiedLine.last())
    }
}