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

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.zs.preferences.Key.Key1
import com.zs.preferences.Key.Key2
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import androidx.datastore.preferences.core.Preferences as StorePreference

internal class PreferencesImpl(context: Context, name: String, val scope: CoroutineScope) :
    Preferences {
    private val Context.store by preferencesDataStore(name)

    private val store = context.store
    private val flow: Flow<StorePreference> = store.data.catch { exception ->
        when (exception) {
            is IOException -> {
                Log.e("PreferencesImpl", "getString: $exception")
                emit(emptyPreferences())
            }

            else -> throw exception
        }
    }

    override fun <S, O> observe(key: Key1<S, O>): Flow<O?> {
        return flow.map { preferences ->
            if (key.saver == null) // must not be null if not provided values.
                preferences[key.value] as O // possible only if basic values.
            else
                preferences[key.value]?.let { key.saver.restore(it) }
        }
    }

    override fun <S, O> observe(key: Key2<S, O>): Flow<O> {
        return flow.map { preferences ->
            if (key.saver == null) // must not be null if not provided values.
                (preferences[key.value] ?: key.default) as O // possible only if basic values.
            else
                preferences[key.value]?.let { key.saver.restore(it) } ?: key.default
        }
    }

    override fun <S, O> set(key: Key<S, O>, value: O) {
        scope.launch {
            store.edit {
                val saver = key.saver
                // if saver is null in that case the savable value is simple like int.
                // else use saver to save the value.
                it[key.storeKey] = if (saver == null) (value as S) else saver.save(value)
            }
        }
    }

    override fun minusAssign(key: Key<*, *>) {
        scope.launch {
            store.edit {
                it -= key.storeKey
            }
        }
    }

    override fun contains(key: Key<*, *>): Boolean {
        return runBlocking {
            flow.map { preference -> key.storeKey in preference }.first()
        }
    }

    override fun clear(x: MutablePreferences) {
        scope.launch {
            store.edit {
                it.clear()
            }
        }
    }

    override fun remove(key: Key<*, *>) {
        scope.launch {
            store.edit {
                it.remove(key.storeKey)
            }
        }
    }
}
