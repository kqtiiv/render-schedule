package schedule

import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

class SchedulerTest {
    @Test
    fun `matches the example in examples directory`() {
        val schedule = Json.decodeFromString<Schedule>(File("examples/schedule.json").readText())
        val overrides = Json.decodeFromString<List<Entry>>(File("examples/overrides.json").readText())
        val expected = Json.decodeFromString<List<Entry>>(File("examples/expected_output.json").readText())

        val actual = renderSchedule(
            schedule,
            overrides,
            from = Instant.parse("2026-11-06T17:00:00Z"),
            until = Instant.parse("2026-11-20T17:00:00Z"),
        )

        assertEquals(expected, actual)
    }
}
