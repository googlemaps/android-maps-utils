package com.google.maps.android.utils.demo

import org.junit.Test
import com.google.common.truth.Truth.assertThat

class AipKeyValidatorTest {

    @Test
    fun testValidKey() {
        val apiKey = "AIzaSyBZAuYobWtoFlmuyyG2HxQWatnPJZ79_BW"
        assertThat(keyHasValidFormat(apiKey)).isTrue()
    }

    @Test
    fun testInvalidKeys() {
        assertThat(keyHasValidFormat("")).isFalse()
        assertThat(keyHasValidFormat("YOUR_API_KEY")).isFalse()
        val apiKey = "AIzaSyBZAuYobWtoFlmuyyG2HxQWatnPJZ79_BW"
        assertThat(keyHasValidFormat(apiKey.dropLast(1))).isFalse()
    }

}
