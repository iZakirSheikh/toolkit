/*
 * Copyright 2025 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 08-02-2025.
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

package com.zs.compose.theme.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AccessibilityManager
import kotlinx.coroutines.sync.Mutex
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalAccessibilityManager
import com.zs.compose.theme.ExperimentalThemeApi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/** Possible results of the [SnackbarHostState.showSnackbar] call */
enum class SnackbarResult {
    /** [Snackbar] that is shown has been dismissed either by timeout of by user */
    Dismissed,

    /** Action on the [Snackbar] has been clicked before the time out passed */
    ActionPerformed,
}

/** Possible durations of the [Snackbar] in [SnackbarHost] */
enum class SnackbarDuration {
    /** Show the Snackbar for a short period of time */
    Short,

    /** Show the Snackbar for a long period of time */
    Long,

    /** Show the Snackbar indefinitely until explicitly dismissed or action is clicked */
    Indefinite
}

// TODO: magic numbers adjustment
internal fun SnackbarDuration.toMillis(
    hasAction: Boolean,
    accessibilityManager: AccessibilityManager?
): Long {
    val original =
        when (this) {
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
 * Interface to represent one particular [Snackbar] as a piece of the [SnackbarHostState]
 *
 * @property message text to be shown in the [Snackbar]
 * @property actionLabel optional action label to show as button in the Snackbar
 * @property duration duration of the snackbar
 */
interface SnackbarData {
    val message: CharSequence
    val action: CharSequence?
    val duration: SnackbarDuration

    val accent: Color get() = Color.Unspecified
    val icon: ImageVector?

    /** Function to be called when Snackbar action has been performed to notify the listeners */
    fun action()

    /** Function to be called when Snackbar is dismissed either by timeout or by the user */
    fun dismiss()
}

/**
 * State of the [SnackbarHost], controls the queue and the current [Snackbar] being shown inside the
 * [SnackbarHost].
 *
 * This state usually lives as a part of a [ScaffoldState] and provided to the [SnackbarHost]
 * automatically, but can be decoupled from it and live separately when desired.
 */
@Stable
class SnackbarHostState {

    /**
     * Only one [Snackbar] can be shown at a time. Since a suspending Mutex is a fair queue, this
     * manages our message queue and we don't have to maintain one.
     */
    private val mutex = Mutex()

    /** The current [SnackbarData] being shown by the [SnackbarHost], of `null` if none. */
    var currentSnackbarData by mutableStateOf<SnackbarData?>(null)
        private set

    /**
     * Shows or queues to be shown a [Snackbar] at the bottom of the [Scaffold] at which this state
     * is attached and suspends until snackbar is disappeared.
     *
     * [SnackbarHostState] guarantees to show at most one snackbar at a time. If this function is
     * called while another snackbar is already visible, it will be suspended until this snack bar
     * is shown and subsequently addressed. If the caller is cancelled, the snackbar will be removed
     * from display and/or the queue to be displayed.
     *
     * All of this allows for granular control over the snackbar queue from within:
     *
     * @sample androidx.compose.material.samples.ScaffoldWithCoroutinesSnackbar
     *
     * To change the Snackbar appearance, change it in 'snackbarHost' on the [Scaffold].
     *
     * @param message text to be shown in the Snackbar
     * @param actionLabel optional action label to show as button in the Snackbar
     * @param duration duration to control how long snackbar will be shown in [SnackbarHost], either
     *   [SnackbarDuration.Short], [SnackbarDuration.Long] or [SnackbarDuration.Indefinite]
     * @return [SnackbarResult.ActionPerformed] if option action has been clicked or
     *   [SnackbarResult.Dismissed] if snackbar has been dismissed via timeout or by the user
     */
    suspend fun showSnackbar(
        message: CharSequence,
        action: CharSequence? = null,
        icon: ImageVector? = null,
        accent: Color = Color.Unspecified,
        duration: SnackbarDuration = SnackbarDuration.Short
    ): SnackbarResult =
        mutex.withLock() {
            try {
                return suspendCancellableCoroutine { continuation ->
                    currentSnackbarData =
                        SnackbarDataImpl(message, action, icon, duration, accent, continuation)
                }
            } finally {
                currentSnackbarData = null
            }
        }

    @Stable
    private class SnackbarDataImpl(
        override val message: CharSequence,
        override val action: CharSequence?,
        override val icon: ImageVector?,
        override val duration: SnackbarDuration,
        override val accent: Color,
        private val continuation: CancellableContinuation<SnackbarResult>
    ) : SnackbarData {


        override fun action() {
            if (continuation.isActive) continuation.resume(SnackbarResult.ActionPerformed)
        }

        override fun dismiss() {
            if (continuation.isActive) continuation.resume(SnackbarResult.Dismissed)
        }
    }
}

/**
 * Host for [Snackbar]s to be used in [Scaffold] to properly show, hide and dismiss items based on
 * material specification and the [hostState].
 *
 * This component with default parameters comes build-in with [Scaffold], if you need to show a
 * default [Snackbar], use use [ScaffoldState.snackbarHostState] and
 * [SnackbarHostState.showSnackbar].
 *
 * @sample androidx.compose.material.samples.ScaffoldWithSimpleSnackbar
 *
 * If you want to customize appearance of the [Snackbar], you can pass your own version as a child
 * of the [SnackbarHost] to the [Scaffold]:
 *
 * @sample androidx.compose.material.samples.ScaffoldWithCustomSnackbar
 * @param hostState state of this component to read and show [Snackbar]s accordingly
 * @param modifier optional modifier for this component
 * @param snackbar the instance of the [Snackbar] to be shown at the appropriate time with
 *   appearance based on the [SnackbarData] provided as a param
 */
@OptIn(ExperimentalThemeApi::class)
@Composable
fun SnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { Snackbar(it) }
) {
    val currentSnackbarData = hostState.currentSnackbarData
    val accessibilityManager = LocalAccessibilityManager.current
    LaunchedEffect(currentSnackbarData) {
        if (currentSnackbarData != null) {
            val duration =
                currentSnackbarData.duration.toMillis(
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