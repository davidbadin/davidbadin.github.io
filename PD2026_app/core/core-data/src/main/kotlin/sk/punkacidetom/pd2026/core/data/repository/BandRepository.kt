package sk.punkacidetom.pd2026.core.data.repository

import kotlinx.coroutines.flow.Flow
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.FestivalInfo

interface BandRepository {
    /** Emits cached bands immediately, then fresh bands after a background refresh. */
    fun observeBands(): Flow<List<Band>>

    /** Derived festival info (start, end, days). Emits null until first data arrives. */
    fun observeFestivalInfo(): Flow<FestivalInfo?>

    /** Force a refresh right now, regardless of the 30-minute cooldown. */
    suspend fun forceRefresh(): Result<Unit>

    /** Sorted bands list (spec §5.3): priority asc, nulls last, then name asc. */
    fun observeSortedBands(): Flow<List<Band>>
}
