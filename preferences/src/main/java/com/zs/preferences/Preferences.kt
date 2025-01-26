package com.zs.preferences

import android.app.Application
import android.content.Context
import androidx.annotation.WorkerThread
import androidx.datastore.preferences.core.MutablePreferences
import com.zs.preferences.Key.Key1
import com.zs.preferences.Key.Key2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface Preferences {
    /**
     * Returns a [Flow] of type [O], representing the value associated with the given [key] in the data store.
     *
     * @param key the key for the value to retrieve
     * @return a [Flow] of type [O], representing the value associated with the given [key] in the data store
     * Note: This can be nullable if [key] doesn't exist.
     */
    fun <S, O> observe(key: Key1<S, O>): Flow<O?>


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
    fun <S, O> observe(key: Key2<S, O>): Flow<O>

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

    /**
     * Returns the raw value associated with the given [key] in this preferences instance.
     *
     * @param key the key of the preference to retrieve the value of.
     * @return the raw value from the [Preferences] linked with [key].
     */
    operator fun <S, O> get(key: Key1<S, O>) = runBlocking { observe(key).first() }

    /**
     * @see [get]
     */
    operator fun <S, O> get(key: Key2<S, O>) = runBlocking { observe(key).first() }

    //
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