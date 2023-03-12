package com.primex.preferences

import androidx.compose.runtime.*
import androidx.datastore.preferences.core.*
import com.primex.preferences.Key.Key1
import com.primex.preferences.Key.Key2
import androidx.datastore.preferences.core.Preferences as StorePreference

/**
 * Author: Zakir Sheikh
 * Since: 1.0.0
 *
 * Description:
 * The `StoreKey` type alias is used to create a shorthand reference to the
 * `androidx.datastore.preferences.core.Preferences.Key` type, which is
 * an instance of the Android `Preferences DataStore`. It is used to store and
 * retrieve values of a specified type `T` in the Android preferences data store.
 * @see StorePreference.Key
 *
 * @param T: The type of the value stored in the preference.
 */
internal typealias StoreKey<T> = StorePreference.Key<T>

/**
 * The `Saver` interface provides a simplified way to convert objects of type [Original] into
 * objects of type [Saveable], and vice versa.
 *
 * Implementations of this interface can be used to simplify the objects before saving them in
 * Android's Preference DataStore, and restore the objects back to their original form after
 * reading from the DataStore.
 *
 * **Usage**
 * ```
 * class StringIntSaver : Saver<Int, String> {
 *     override fun save(value: String): Int {
 *         return value.toInt()
 *     }
 *
 *     override fun restore(value: Int): String {
 *         return value.toString()
 *     }
 * }
 * ```
 *
 * @author Zakir Sheikh
 * @since 1.0.0
 *
 * @param S The type of the simplified object that can be saved in the DataStore.
 * @param O The type of the original object that needs to be converted.
 */
interface Saver<S, O> {
    /**
     * Converts the [Original] object into a [Saveable] object that can be saved in the DataStore.
     *
     * @param value The [Original] object that needs to be converted.
     * @return The [Saveable] object that can be saved in the DataStore.
     */
    fun save(value: O): S

    /**
     * Converts the [Saveable] object back into the [Original] object.
     *
     * @param value The [Saveable] object that needs to be converted back to the [Original] object.
     * @return The [Original] object.
     */
    fun restore(value: S): O
}

/**
 * A key interface for a data store that contains a value of type [O] that can be saved as type [S].
 *
 * @param S the type of the value that can be saved in the data store
 * @param O the original type of the value before it is saved in the data store
 */
sealed interface Key<S, O> {
    /**
     * The name of the key.
     */
    val name: String

    /**
     * This constructs a [StoreKey] wrapper with [Saver].
     *
     * @param value the store key for the value in the data store
     * @param saver the saver used to convert between the original type [O] and the saved type [S]
     */
    class Key1<S, O> internal constructor(
        internal val value: StoreKey<S>,
        internal val saver: Saver<S, O>?
    ) : Key<S, O> {
        override val name: String
            get() = value.name
    }

    /**
     * This constructs a [StoreKey] wrapper with [Saver] and [default].
     *
     * @param value the store key for the value in the data store
     * @param default the default value to use when the data store has no value for this key
     * @param saver the saver used to convert between the original type [O] and the saved type [S]
     */
    class Key2<S, O> internal constructor(
        internal val value: StoreKey<S>,
        internal val default: O,
        internal val saver: Saver<S, O>?
    ) : Key<S, O> {
        override val name: String
            get() = value.name
    }
}


/**
 * The `IntSaver` typealias provides a shorthand for the [Saver] interface with `Int` as the
 * saveable type and `O` as the original type.
 *
 * Implementations of this typealias can be used to simplify objects of type `O` before saving
 * them in Android's Preference DataStore, and restore the objects back to their original form
 * after reading from the DataStore.
 *
 * Example usage:
 * ```
 * class StringIntSaver : IntSaver<String> {
 *     override fun save(value: String): Int {
 *         return value.toInt()
 *     }
 *
 *     override fun restore(value: Int): String {
 *         return value.toString()
 *     }
 * }
 * ```
 *
 * @author Zakir Sheikh
 * @since 1.0.0
 *
 * @param O The type of the original object that needs to be converted.
 */
typealias IntSaver<O> = Saver<Int, O>

/**
 * @see [intPreferencesKey]
 */
fun intPreferenceKey(name: String) =
    Key1<Int, Int>(intPreferencesKey(name), null)

/**
 * @see [intPreferencesKey]
 */
fun intPreferenceKey(name: String, defaultValue: Int) =
    Key2(intPreferencesKey(name), defaultValue, null)

/**
 * @see [intPreferencesKey]
 */
fun <O> intPreferenceKey(name: String, saver: IntSaver<O>) =
    Key1(intPreferencesKey(name), saver)

/**
 * @see [intPreferencesKey]
 */
fun <O> intPreferenceKey(name: String, defaultValue: O, saver: IntSaver<O>) =
    Key2(intPreferencesKey(name), defaultValue, saver)


/**
 * The `FloatSaver` typealias provides a shorthand for the [Saver] interface with `Float` as the
 * saveable type and `O` as the original type.
 *
 * Implementations of this typealias can be used to simplify objects of type `O` before saving
 * them in Android's Preference DataStore, and restore the objects back to their original form
 * after reading from the DataStore.
 *
 * Example usage:
 * ```
 * class StringFloatSaver : FloatSaver<String> {
 *     override fun save(value: String): Float {
 *         return value.toFloat()
 *     }
 *
 *     override fun restore(value: Float): String {
 *         return value.toString()
 *     }
 * }
 * ```
 *
 * @author Zakir Sheikh
 * @since 1.0.0
 *
 * @param O The type of the original object that needs to be converted.
 */
typealias FloatSaver<O> = Saver<Float, O>

/**
 * @see [floatPreferencesKey]
 */
fun floatPreferenceKey(name: String) =
    Key1<Float, Float>(floatPreferencesKey(name), null)

/**
 * @see [floatPreferencesKey]
 */
fun floatPreferenceKey(name: String, defaultValue: Float) =
    Key2(floatPreferencesKey(name), defaultValue, null)

/**
 * @see [floatPreferencesKey]
 */
fun <O> floatPreferenceKey(name: String, saver: FloatSaver<O>) =
    Key1(floatPreferencesKey(name), saver)

/**
 * @see [floatPreferencesKey]
 */
fun <O> floatPreferenceKey(name: String, defaultValue: O, saver: FloatSaver<O>) =
    Key2(floatPreferencesKey(name), defaultValue, saver)


/**
 * The `DoubleSaver` typealias provides a shorthand for the [Saver] interface with `Double` as the
 * saveable type and `O` as the original type.
 *
 * Implementations of this typealias can be used to simplify objects of type `O` before saving
 * them in Android's Preference DataStore, and restore the objects back to their original form
 * after reading from the DataStore.
 *
 * Example usage:
 * ```
 * class StringDoubleSaver : DoubleSaver<String> {
 *     override fun save(value: String): Double {
 *         return value.toDouble()
 *     }
 *
 *     override fun restore(value: Double): String {
 *         return value.toString()
 *     }
 * }
 * ```
 *
 * @author Zakir Sheikh
 * @since 1.0.0
 *
 * @param O The type of the original object that needs to be converted.
 */
typealias DoubleSaver<O> = Saver<Double, O>

/**
 * @see [doublePreferencesKey]
 */
fun doublePreferenceKey(name: String) =
    Key1<Double, Double>(doublePreferencesKey(name), null)

/**
 * @see [doublePreferencesKey]
 */
fun doublePreferenceKey(name: String, defaultValue: Double) =
    Key2(doublePreferencesKey(name), defaultValue, null)

/**
 * @see [doublePreferencesKey]
 */
fun <O> doublePreferenceKey(name: String, saver: DoubleSaver<O>) =
    Key1(doublePreferencesKey(name), saver)

/**
 * @see [doublePreferencesKey]
 */
fun <O> doublePreferenceKey(name: String, defaultValue: O, saver: DoubleSaver<O>) =
    Key2(doublePreferencesKey(name), defaultValue, saver)

/**
 * The `LongSaver` typealias provides a shorthand for the [Saver] interface with `Long` as the
 * saveable type and `O` as the original type.
 *
 * Implementations of this typealias can be used to simplify objects of type `O` before saving
 * them in Android's Preference DataStore, and restore the objects back to their original form
 * after reading from the DataStore.
 *
 * Example usage:
 * ```
 * class StringLongSaver : LongSaver<String> {
 *     override fun save(value: String): Long {
 *         return value.toLong()
 *     }
 *
 *     override fun restore(value: Long): String {
 *         return value.toString()
 *     }
 * }
 * ```
 *
 * @author Zakir Sheikh
 * @since 1.0.0
 *
 * @param O The type of the original object that needs to be converted.
 */
typealias LongSaver<O> = Saver<Long, O>

/**
 * @see [longPreferencesKey]
 */
fun longPreferenceKey(name: String) =
    Key1<Long, Long>(longPreferencesKey(name), null)

/**
 * @see [longPreferencesKey]
 */
fun longPreferenceKey(name: String, defaultValue: Long) =
    Key2(longPreferencesKey(name), defaultValue, null)

/**
 * @see [longPreferencesKey]
 */
fun <O> longPreferenceKey(name: String, saver: LongSaver<O>) =
    Key1(longPreferencesKey(name), saver)

/**
 * @see [longPreferencesKey]
 */
fun <O> longPreferenceKey(name: String, defaultValue: O, saver: LongSaver<O>) =
    Key2(longPreferencesKey(name), defaultValue, saver)

/**
 * @see [booleanPreferencesKey]
 */
fun booleanPreferenceKey(name: String) =
    Key1<Boolean, Boolean>(booleanPreferencesKey(name), null)

/**
 * @see [booleanPreferencesKey]
 */
fun booleanPreferenceKey(name: String, defaultValue: Boolean) =
    Key2(booleanPreferencesKey(name), defaultValue, null)

/**
 * The `StringSaver` typealias provides a shorthand for the [Saver] interface with `String` as the
 * saveable type and `O` as the original type.
 *
 * Implementations of this typealias can be used to simplify objects of type `O` before saving
 * them in Android's Preference DataStore, and restore the objects back to their original form
 * after reading from the DataStore.
 *
 * Example usage:
 * ```
 * class IntStringSaver : StringSaver<Int> {
 *     override fun save(value: Int): String {
 *         return value.toString()
 *     }
 *
 *     override fun restore(value: String): Int {
 *         return value.toInt()
 *     }
 * }
 * ```
 *
 * @author Zakir Sheikh
 * @since 1.0.0
 *
 * @param O The type of the original object that needs to be converted.
 */
typealias StringSaver<O> = Saver<String, O>

/**
 * @see [stringPreferencesKey]
 */
fun stringPreferenceKey(name: String) =
    Key1<String, String>(stringPreferencesKey(name), null)

/**
 * @see [stringPreferencesKey]
 */
fun stringPreferenceKey(name: String, defaultValue: String) =
    Key2(stringPreferencesKey(name), defaultValue, null)

/**
 * @see [stringPreferencesKey]
 */
fun <O> stringPreferenceKey(name: String, saver: StringSaver<O>) =
    Key1(stringPreferencesKey(name), saver)

/**
 * @see [stringPreferencesKey]
 */
fun <O> stringPreferenceKey(name: String, defaultValue: O, saver: StringSaver<O>) =
    Key2(stringPreferencesKey(name), defaultValue, saver)

/**
 * @see [stringSetPreferenceKey]
 */
fun stringSetPreferenceKey(name: String) =
    Key1<Set<String>, Set<String>>(stringSetPreferencesKey(name), null)

/**
 * @see [stringSetPreferenceKey]
 */
fun stringSetPreferenceKey(name: String, defaultValue: Set<String>) =
    Key2(stringSetPreferencesKey(name), defaultValue, null)


