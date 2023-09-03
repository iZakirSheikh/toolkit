package com.primex.core.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

private const val TAG = "SnackbarHost"

/**
 * Possible results of the [SnackbarHostState.showSnackbar] call
 */
enum class SnackbarResult {
    /**
     * [Snackbar] that is shown has been dismissed either by timeout of by user
     */
    Dismissed,

    /**
     * Action on the [Snackbar] has been clicked before the time out passed
     */
    ActionPerformed,
}

/**
 * Possible durations of the [Snackbar] in [SnackbarHost]
 */
enum class SnackbarDuration {
    /**
     * Show the Snackbar for a short period of time
     */
    Short,

    /**
     * Show the Snackbar for a long period of time
     */
    Long,

    /**
     * Show the Snackbar indefinitely until explicitly dismissed or action is clicked
     */
    Indefinite
}


/**
 * Interface to represent one particular [Snackbar] as a piece of the [SnackbarHost] State.
 *
 * @property message text to be shown in the [SnackbarHost]
 * @property action optional action label to show as button in the Toast
 * @property duration duration of the toast
 * @property accent The accent color of this [SnackbarHost]. Default [Color.Unspecified]
 * @property leading optional leading icon for [SnackbarHost]. Default null. The leading must be a vector icon or resource drawbale.
 */
@Stable
interface Data {

    val accent: Color
    val leading: Any?

    val message: CharSequence
    val action: CharSequence?
    val duration: SnackbarDuration
    val withDismissAction: Boolean

    /**
     * Function to be called when Toast action has been performed to notify the listeners
     */
    fun action()

    /**
     * Function to be called when Toast is dismissed either by timeout or by the user
     */
    fun dismiss()
}

@Stable
private class DataImpl(
    override val message: CharSequence,
    override val action: CharSequence?,
    override val accent: Color,
    override val leading: Any?,
    override val duration: SnackbarDuration,
    override val withDismissAction: Boolean,
    private val continuation: CancellableContinuation<SnackbarResult>,
) : Data {
    override fun action() {
        if (continuation.isActive) continuation.resume(SnackbarResult.ActionPerformed)
    }

    override fun dismiss() {
        if (continuation.isActive) continuation.resume(SnackbarResult.Dismissed)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataImpl

        if (accent != other.accent) return false
        if (leading != other.leading) return false
        if (message != other.message) return false
        if (action != other.action) return false
        if (duration != other.duration) return false
        if (withDismissAction != other.withDismissAction) return false
        if (continuation != other.continuation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = accent.hashCode()
        result = 31 * result + (leading?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        result = 31 * result + (action?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + withDismissAction.hashCode()
        result = 31 * result + continuation.hashCode()
        return result
    }
}

// TODO: magic numbers adjustment
internal fun SnackbarDuration.toMillis(
    hasAction: Boolean,
    accessibilityManager: AccessibilityManager?
): Long {
    val original = when (this) {
        SnackbarDuration.Indefinite -> Long.MAX_VALUE
        SnackbarDuration.Long -> 10000L
        SnackbarDuration.Short -> 4000L
    }
    if (accessibilityManager == null) {
        return original
    }
    return accessibilityManager.calculateRecommendedTimeoutMillis(
        original,
        containsIcons = true,
        containsText = true,
        containsControls = hasAction
    )
}

/**
 * State of the [SnackbarHost], which controls the queue and the current [Snackbar] being shown
 * inside the [SnackbarHost].
 *
 * This state is usually [remember]ed and used to provide a [SnackbarHost] to a [Scaffold].
 */
@Stable
class SnackbarHostState {
    /**
     * Only one [Snackbar] can be shown at a time. Since a suspending Mutex is a fair queue, this
     * manages our message queue and we don't have to maintain one.
     */
    private val mutex = Mutex()

    /**
     * The current [SnackbarData] being shown by the [SnackbarHost], or `null` if none.
     */
    var currentSnackbarData by mutableStateOf<Data?>(null)
        private set

    /**
     * Shows or queues to be shown a [Snackbar] at the bottom of the [Scaffold] to which this state
     * is attached and suspends until the snackbar has disappeared.
     *
     * [SnackbarHostState] guarantees to show at most one snackbar at a time. If this function is
     * called while another snackbar is already visible, it will be suspended until this snackbar is
     * shown and subsequently addressed. If the caller is cancelled, the snackbar will be removed
     * from display and/or the queue to be displayed.
     * @see [SnackbarData2]
     */
    suspend fun showSnackbar(
        msg: CharSequence,
        action: CharSequence? = null,
        leading: Any? = null,
        accent: Color = Color.Unspecified,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = if (action == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
    ): SnackbarResult = mutex.withLock {
        try {
            return suspendCancellableCoroutine { continuation ->
                currentSnackbarData =
                    DataImpl(
                        msg,
                        action,
                        accent,
                        leading,
                        duration,
                        withDismissAction,
                        continuation
                    )
            }
        } finally {
            currentSnackbarData = null
        }
    }
}

/**
 * Host for [Snackbar]s to be used in [Scaffold] to properly show, hide and dismiss items based
 * on Material specification and the [hostState].
 *
 * This component with default parameters comes build-in with [Scaffold], if you need to show a
 * default [Snackbar], use [SnackbarHostState.showSnackbar].
 *
 * If you want to customize appearance of the [Snackbar], you can pass your own version as a child
 * of the [SnackbarHost] to the [Scaffold]:
 *
 * @param hostState state of this component to read and show [Snackbar]s accordingly
 * @param modifier the [Modifier] to be applied to this component
 * @param snackbar the instance of the [Snackbar] to be shown at the appropriate time with
 * appearance based on the [Data] provided as a param
 */
@Composable
fun SnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (Data) -> Unit
) {
    val currentSnackbarData = hostState.currentSnackbarData
    val accessibilityManager = LocalAccessibilityManager.current
    LaunchedEffect(currentSnackbarData) {
        if (currentSnackbarData != null) {
            val duration = currentSnackbarData.duration.toMillis(
                currentSnackbarData.action != null,
                accessibilityManager
            )
            delay(duration)
            currentSnackbarData.dismiss()
        }
    }
    FadeInFadeOutWithScale(
        current = hostState.currentSnackbarData,
        modifier = modifier,
        content = snackbar
    )
}

