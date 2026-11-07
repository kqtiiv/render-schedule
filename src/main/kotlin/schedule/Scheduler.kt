package schedule

import java.time.Duration
import java.time.Instant

/**
 * Returns the regular rotation shifts (ignoring overrides) that overlap the window [from, until).
 *
 * Shifts are returned whole, not truncated to the window, as can be done via a truncate function. All intervals are
 * half-open: a shift includes its start but not its end, so back-to-back shifts never overlap.
 */
private fun rotationShifts(schedule: Schedule, from: Instant, until: Instant): List<Entry> {
    val rotationSchedule = mutableListOf<Entry>()

    // Length of one shift.
    val interval = Duration.ofDays(schedule.handoverIntervalDays.toLong())

    // n counts shifts from the first handover.
    var n = 0

    // Shift n runs from handoverStartAt + n intervals until the next handover.
    var start = schedule.handoverStartAt + interval.multipliedBy(n.toLong())
    var end = start + interval

    // Once a shift starts at or after `until`, none of the
    // remaining shifts can overlap the window.
    while (start < until) {
        // Users take turns in order, wrapping back to the first after the last.
        val user = schedule.users[n % schedule.users.size]

        // Shifts that finished before `from` are skipped.
        if (end > from) {
            rotationSchedule.add(Entry(user, start, end))
        }

        // Move on to the next shift: it starts exactly where this one ended.
        n++
        start = end
        end = start + interval
    }

    return rotationSchedule
}

/** Returns the parts of [entry] that [override] doesn't cover: 0, 1 or 2 pieces. */
private fun subtract(entry: Entry, override: Entry): List<Entry> {
    val pieces = mutableListOf<Entry>()

    val beforeEnd = minOf(entry.endAt, override.startAt)
    if (entry.startAt < beforeEnd) pieces += Entry(entry.user, entry.startAt, beforeEnd)

    val afterStart = maxOf(entry.startAt, override.endAt)
    if (afterStart < entry.endAt) pieces += Entry(entry.user, afterStart, entry.endAt)

    return pieces
}

/**
 * Renders the final schedule between [from] and [until]: the projected rotation from [schedule],
 * with [overrides] applied on top, and entries truncated to the window.
 */
fun renderSchedule(
    schedule: Schedule,
    overrides: List<Entry>,
    from: Instant,
    until: Instant,
): List<Entry> {
    // Start from the regular rotation
    var entries = rotationShifts(schedule, from, until)

    // Cut each override's time out of the rotation
    for (override in overrides) {
        entries = entries.flatMap { subtract(it, override) }
    }

    // Fill the holes with the overrides, and sort by start time
    entries = (entries + overrides).sortedBy { it.startAt }

    // Truncate to [from, until) and drop anything left empty
    return entries
        .map { entry -> Entry(entry.user, maxOf(entry.startAt, from), minOf(entry.endAt, until)) }
        .filter { entry -> entry.startAt < entry.endAt }
}
