@file:OptIn(DelicateCoroutinesApi::class)

package com.primex.preferences

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.primex.preferences.Key.Key1
import com.primex.preferences.Key.Key2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import androidx.datastore.preferences.core.Preferences as StorePreference

import androidx.datastore.preferences.protobuf.ExperimentalApi
import com.primex.preferences.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private const val TAG = "Preferences"

/**
 * An interface that represents a data store to persist and retrieve key-value pairs.
 *
 * The Preference DataStore API provides a type-safe and efficient way to store small data using key-value pairs.
 * The data is stored asynchronously and can be accessed using a coroutine-based API. The data is also observable
 * using the Kotlin Flow API, which allows for easy integration with other reactive programming libraries.
 *
 * This interface is a wrapper around the Preference DataStore API and provides additional functionality for working
 * with different data types and setting default values for keys.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/datastore/preferences">Android Preference DataStore documentation</a>
 */
interface Preferences {

    /**
     * Returns a [Flow] of type [O], representing the value associated with the given [key] in the data store.
     *
     * @param key the key for the value to retrieve
     * @return a [Flow] of type [O], representing the value associated with the given [key] in the data store
     * Note: This can be nullable if [key] doesn't exist.
     */
    operator fun <S, O> get(key: Key1<S, O>): Flow<O?>


    /**
     * Returns a [Flow] of type [O], representing the value associated with the given [key] in the data store.
     * If the value associated with the given [key] does not exist in the data store, the [default] value will be emitted.
     *
     * @param key the key for the value to retrieve
     * @param default the default value to emit if the value associated with the given [key] does not exist in the data store
     * @return a [Flow] of type [O], representing the value associated with the given [key] in the data store,
     * emitting [default] if the value does not exist
     * @see observe
     */
    operator fun <S, O> get(key: Key2<S, O>): Flow<O>

    /**
     * Sets a key-value pair in the data store, where the [key] represents the key to associate with the [value].
     *
     * @param key the key to associate with the [value]
     * @param value the value to be stored in the data store
     */
    operator fun <S, O> set(key: Key<S, O>, value: O)

    /** Removes the preference with the given key from this MutablePreferences. If this
     * Preferences does not contain the key, this is a no-op.
     *
     * Example usage:
     * mutablePrefs -= COUNTER_KEY
     *
     * @param key the key to remove from this MutablePreferences
     */
    operator fun minusAssign(key: Key<*, *>)

    /**
     * Returns true if this Preferences contains the specified key.
     *
     * @param key the key to check for
     */
    @WorkerThread
    operator fun contains(key: Key<*, *>): Boolean

    /** Removes all preferences from this MutablePreferences. */
    fun clear(x: MutablePreferences)

    /**
     * Remove a preferences from this MutablePreferences.
     */
    fun remove(key: Key<*, *>)

    companion object {

        // Singleton prevents multiple instances of repository opening at the
        // same time.
        private const val TAG = "Preferences"

        private const val DEFAULT_NAME = "preferences.file"

        @Volatile
        private var INSTANCE: Preferences? = null

        @Deprecated("Create instance and use with Hilt")
        fun get(context: Context): Preferences {

            // if the INSTANCE is not null, then return it,
            // if it is, then create the repository
            return INSTANCE ?: synchronized(this) {
                val instance =
                    PreferencesImpl(context.applicationContext as Application, DEFAULT_NAME, GlobalScope)
                INSTANCE = instance
                instance
            }
        }

        /**
         * Creates a new [Preferences] instance with the given [context], [name], and [scope].
         *
         * @param context the Android application context used to access the Android data store API
         * @param name the name of the data store
         * @param scope the coroutine scope for the data store operations
         * @return a new [Preferences] instance
         */
        operator fun invoke(context: Context, name: String = DEFAULT_NAME, scope: CoroutineScope = GlobalScope): Preferences =
            PreferencesImpl(context.applicationContext, name, scope)
    }
}

private inline  val <S, O> Key<S, O>.storeKey get() = when(this){
    is Key1 -> value
    is Key2 -> value
}

private inline val <S, O> Key<S, O>.saver: Saver<S, O>? get() = when(this){
    is Key1 -> saver
    is Key2 -> saver
}

private class PreferencesImpl(context: Context, name: String, val scope: CoroutineScope) : Preferences {
    private val Context.store by preferencesDataStore(name)

    private val store = context.store
    private val flow: Flow<StorePreference> = store.data.catch { exception ->
        when (exception) {
            is IOException -> {
                Log.e(TAG, "getString: $exception")
                emit(emptyPreferences())
            }
            else -> throw exception
        }
    }

    override fun <S, O> get(key: Key1<S, O>): Flow<O?> {
        return flow.map { preferences ->
            if (key.saver == null) // must not be null if not provided values.
                preferences[key.value] as O // possible only if basic values.
            else
                preferences[key.value]?.let { key.saver.restore(it) }
        }
    }

    override fun <S, O> get(key: Key2<S, O>): Flow<O> {
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

/**
 * Observes the value of the given [key] in the preferences and returns a [State] that represents
 * the current value. The [State] object can be read and updated like a regular variable.
 *
 * @param key the [Key] of the value to observe
 * @return a [State] object representing the current value of the given [key]
 *
 * ### Usage Example
 *
 * ```
 * val preferences = context.preferences()
 * val intKey = intPreferenceKey("int_key")
 *
 * Column {
 *     val value by preferences.observe(intKey)
 *     Text("The value of the key is: $value")
 *     Button(onClick = { preferences[intKey] = 42 }) {
 *         Text("Set value to 42")
 *     }
 * }
 * ```
 */
@Composable
private inline fun <S, O> Preferences.observe(key: Key<S, O>): State<O?> {
    val flow = when(key){
        is Key1 -> this[key]
        is Key2 -> this[key]
    }

    val first = remember(key.name) {
        runBlocking { flow.first() }
    }
    return flow.collectAsState(initial = first)
}

/**
 * Returns a [State] object that represents the value associated with the given [key] in this
 * preferences instance.
 *
 * @param key the key of the preference to observe.
 * @return a [State] object that represents the value associated with the given [key] in this
 * preferences instance.
 *
 * @see [androidx.compose.runtime.State]
 * @see [androidx.datastore.preferences.core.Preferences]
 */
@Composable
@NonRestartableComposable
fun <S, O> Preferences.observeAsState(key: Key1<S, O>): State<O?> = observe(key = key)

/**
 * Returns a [State] object that represents the value associated with the given [key] in this preferences instance.
 *
 * @param key the key of the preference to observe.
 * @return a [State] object that represents the value associated with the given [key] in this preferences instance.
 *
 * @throws IllegalArgumentException if the [key]'s type is not supported by this preferences instance.
 *
 * @sample androidx.compose.material.samples.TextFieldsSample
 *
 * @see [androidx.compose.runtime.State]
 * @see [androidx.datastore.preferences.core.Preferences]
 */
@Composable
@NonRestartableComposable
fun <S, O> Preferences.observeAsState(key: Key2<S, O>): State<O> = observe(key = key) as State<O>

/**
 * Returns the raw value associated with the given [key] in this preferences instance.
 *
 * @param key the key of the preference to retrieve the value of.
 * @return the raw value from the [Preferences] linked with [key].
 */
fun <S, O> Preferences.value(key: Key1<S, O>): O? = runBlocking { this@value[key].first() }

/**
 * @see [value]
 */
@WorkerThread
fun <S, O> Preferences.value(key: Key2<S, O>): O = runBlocking { this@value[key].first() }

/**
 * @see [value]
 */
@ExperimentalApi
operator fun <S, O> Preferences.invoke(key: Key1<S, O>) = value(key)

/**
 * @see [value]
 */
@ExperimentalApi
operator fun <S, O> Preferences.invoke(key: Key2<S, O>) = value(key)
