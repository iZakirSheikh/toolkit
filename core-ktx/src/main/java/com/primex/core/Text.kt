package com.primex.core

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.*
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString


/**
 * Represents a text item that can be sent from non-UI components such as [ViewModel]s or [Services].
 *
 * This interface can be used to send text resources directly to UI components.
 *
 * @since 1.0.0
 * @author Zakir Sheikh
 */
@Immutable
@Stable
sealed interface Text {
    companion object {
        /**
         * Creates a [Text] instance with the given [String] value.
         *
         * @param value The text value to use.
         * @return A new [Text] instance with the given value.
         */
        operator fun invoke(value: String): Text = Raw(value)

        /**
         * Creates a [Text] instance with the given [AnnotatedString].
         *
         * @param value The annotated string value to use.
         * @return A new [Text] instance with the given value.
         */
        operator fun invoke(value: CharSequence): Text = Raw(value)


        /**
         * Creates a [Text] instance with the given string resource ID and format arguments.
         *
         * @param id The string resource ID to use.
         * @param formatArgs The format arguments to use.
         * @return A new [Text] instance with the given value.
         */
        operator fun invoke(@StringRes id: Int, formatArgs: Array<out Any>): Text =
            StringResource2(id, formatArgs)

        /**
         * Returns a [Text] object representing a string resource.
         *
         * @param id The resource identifier of the string resource.
         * @param isHtml A flag indicating whether the string is in HTML format. Defaults to false.
         * @return A [Text] object representing the specified string resource.
         */
        operator fun invoke(@StringRes id: Int, isHtml: Boolean = false): Text =
            if (isHtml) HtmlResource(id) else StringResource(id)

        /**
         * Represents a text item that can be sent from non-UI components such as [ViewModel]s or [Services].
         *
         * This interface can be used to send text resources directly to UI components.
         *
         * @since 1.0.0
         * @param id The id of the plurals string resource.
         * @param quantity The quantity to use for pluralization.
         * @return An instance of [PluralResource] created using the provided parameters.
         */
        operator fun invoke(@PluralsRes id: Int, quantity: Int): Text =
            PluralResource(packInts(id, quantity))

        /**
         * Creates a [PluralResource2] text item that represents a quantity-dependent string resource with variable arguments.
         *
         * @param id The resource ID for the plural string
         * @param quantity The quantity for which the string resource should be retrieved
         * @param formatArgs Optional format arguments to be applied to the string resource
         * @return A [PluralResource2] text item
         */
        operator fun invoke(id: Int, quantity: Int, vararg formatArgs: Any): Text =
            PluralResource2(id, quantity, formatArgs)
    }
}

/**
 * A Raw text. i.e., [String]
 */
@JvmInline
@Immutable
private value class Raw(val value: CharSequence) : Text

/**
 * Constructs an [StringResource] String [Text] wrapper.
 */
@JvmInline
@Immutable
private value class StringResource(val id: Int) : Text

/**
 * A data class holding [Resource] String with [formatArgs]
 */
private data class StringResource2(val id: Int, val formatArgs: Array<out Any>) : Text {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringResource2

        if (id != other.id) return false
        if (!formatArgs.contentEquals(other.formatArgs)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + formatArgs.contentHashCode()
        return result
    }
}

/**
 * A value class holding [Html] [Resource].
 * Currently only supports as are supported by [stringHtmlResource()] function.
 */
@JvmInline
@Immutable
private value class HtmlResource(@StringRes val id: Int) : Text

/**
 * @see TextPlural
 */
@JvmInline
@Immutable
private value class PluralResource(val packedValue: Long) : Text {

    @Stable
    val id: Int
        @PluralsRes
        get() = unpackInt1(packedValue)

    @Stable
    val quantity: Int
        get() = unpackInt1(packedValue)
}

/**
 * A data class holds [Plural] resource [String]s. with [formatArgs]
 */
private data class PluralResource2(
    val id: Int,
    val quantity: Int,
    val formatArgs: Array<out Any>
) : Text {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PluralResource2

        if (id != other.id) return false
        if (quantity != other.quantity) return false
        if (!formatArgs.contentEquals(other.formatArgs)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + quantity
        result = 31 * result + formatArgs.contentHashCode()
        return result
    }
}

/**
 * A simple function that returns the [Bundle] value inside this wrapper.
 *  It is either resource id from which this Wrapper was created or the raw text [String]
 */
val Text.raw: Any
    get() = when (this) {
            is HtmlResource -> id
            is PluralResource -> id
            is Raw -> value
            is StringResource -> id
            is StringResource2 -> id
            is PluralResource2 -> id
        }


/**
 * Unpacks the text wrapper to result in a `CharSequence`, which can be either a `String` or an `AnnotatedString`.
 *
 * @return The unpacked `CharSequence` result.
 */
val Text.get: CharSequence
    @Composable
    @ReadOnlyComposable
    @NonRestartableComposable
    get() = when (this) {
            is HtmlResource -> stringHtmlResource(id = this.id)
            is PluralResource ->
                pluralStringResource(id = this.id, count = this.quantity)
            is PluralResource2 -> stringHtmlResource(this.id, this.quantity, this.formatArgs)
            is Raw -> this.value
            is StringResource -> stringResource(id = id)
            is StringResource2 -> stringResource(id, formatArgs)
        }


/**
 * Resolves the resource to [AnnotatedString]
 */
@Composable
@ReadOnlyComposable
@NonRestartableComposable
@Deprecated("Use the extension fun get")
fun stringResource(value: Text) = value.get

/**
 * **Note: Doesn't support collecting [HtmlResource] Strings.
 * @param text: The [Text] to collect.
 */
fun Resources.resolve(text: Text): CharSequence =
    when (text) {
        is HtmlResource -> error("Not supported when collecting from Resource")
        is PluralResource -> getQuantityString(text.id, text.quantity)
        is PluralResource2 -> getQuantityString(
            text.id,
            text.quantity,
            text.formatArgs
        )
        is Raw -> text.value
        is StringResource -> getString(text.id)
        is StringResource2 -> getString(text.id, text.formatArgs)
    }

/**
 * @see resolve
 */
@JvmName("resolve2")
fun Resources.resolve(text: Text?): CharSequence? =
    if (text == null) null else resolve(text)
