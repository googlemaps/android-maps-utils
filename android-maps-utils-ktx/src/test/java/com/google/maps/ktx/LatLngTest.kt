package com.google.maps.ktx

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class LatLngTest {
    @Test
    fun testEncode() {
        val line = listOf(LatLng(1.0, 2.0))
        assertEquals("_ibE_seK", line.latLngListEncode())
    }

    @Test
    fun testDecode() {
        val lineEncoded = "_yfyF_ocsF"
        val line = lineEncoded.toLatLngList()
        assertEquals(LatLng(41.0, 40.0), line.first())
    }

    @Test
    fun testSimplifyEndpointsEqual() {
        val lineEncoded = "elfjD~a}uNOnFN~Em@fJv@tEMhGDjDe@hG^nF??@lA?n@IvAC`Ay@A{@DwCA{CF_EC{CEi@PBTFDJBJ?V?n@?D@?A@?@?F?F?LAf@?n@@`@@T@~@FpA?fA?p@?r@?vAH`@OR@^ETFJCLD?JA^?J?P?fAC`B@d@?b@A\\@`@Ad@@\\?`@?f@?V?H?DD@DDBBDBD?D?B?B@B@@@B@B@B@D?D?JAF@H@FCLADBDBDCFAN?b@Af@@x@@"
        val line = lineEncoded.toLatLngList()
        val simplifiedLine = line.simplify(tolerance = 5.0)
        assertEquals(20, simplifiedLine.size)
        assertEquals(line.first(), simplifiedLine.first())
        assertEquals(line.last(), simplifiedLine.last())
    }
}