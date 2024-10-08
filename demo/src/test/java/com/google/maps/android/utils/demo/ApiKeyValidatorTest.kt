/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo

import org.junit.Test
import com.google.common.truth.Truth.assertThat

class ApiKeyValidatorTest {

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
