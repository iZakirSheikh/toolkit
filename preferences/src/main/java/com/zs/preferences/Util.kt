/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 26-01-2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zs.preferences

import com.zs.preferences.Key.Key1
import com.zs.preferences.Key.Key2

internal inline  val <S, O> Key<S, O>.storeKey get() = when(this){
    is Key1 -> value
    is Key2 -> value
}
internal inline val <S, O> Key<S, O>.saver: Saver<S, O>? get() = when(this){
    is Key1 -> saver
    is Key2 -> saver
}
