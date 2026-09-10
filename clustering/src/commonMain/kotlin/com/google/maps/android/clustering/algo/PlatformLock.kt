/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.clustering.algo

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A reentrant mutual-exclusion lock usable from common code. Replaces the JVM-only
 * `synchronized` blocks and `ReentrantReadWriteLock` the algorithms used before the
 * multiplatform migration. Exclusive locking is a strict (safe) narrowing of the previous
 * read/write locking.
 */
internal expect class PlatformLock() {
    fun lock()

    fun unlock()
}

@OptIn(ExperimentalContracts::class)
internal inline fun <T> PlatformLock.withLock(block: () -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    lock()
    try {
        return block()
    } finally {
        unlock()
    }
}
