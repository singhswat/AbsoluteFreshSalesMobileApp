package com.absolutefresh.sales.ui.beat

import com.absolutefresh.sales.data.model.BeatOutletItem
import com.absolutefresh.sales.data.model.BeatProgress

/** All UI states the Beat screen can be in. */
sealed class BeatUiState {
    /** Initial load — nothing in cache yet. */
    object Loading : BeatUiState()

    /** No beat has been assigned for the requested date. */
    object Empty : BeatUiState()

    /** Beat data is available (from cache or fresh fetch). */
    data class Populated(
        val items: List<BeatOutletItem>,
        val progress: BeatProgress,
        val lastRefreshed: Long,       // epoch-millis
        val isStale: Boolean = false,  // threshold TBD — open item #5
        val isOffline: Boolean = false,
        val pendingCount: Int = 0      // items in outbox not yet synced
    ) : BeatUiState()

    /** A non-fatal error was encountered (e.g. fetch failed, cache shown). */
    data class Error(val message: String) : BeatUiState()
}
