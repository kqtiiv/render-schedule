package schedule

import java.time.Instant

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
    TODO("implement the scheduling algorithm")
}
