package schedule

import java.io.File
import java.time.Instant
import kotlin.system.exitProcess
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

private val usage =
    "usage: render-schedule --schedule=FILE --overrides=FILE --from=TIMESTAMP --until=TIMESTAMP"

fun main(args: Array<String>) {
    val opts = parseArgs(args)

    val schedule = json.decodeFromString<Schedule>(File(opts.require("schedule")).readText())
    val overrides = json.decodeFromString<List<Entry>>(File(opts.require("overrides")).readText())
    val from = Instant.parse(opts.require("from"))
    val until = Instant.parse(opts.require("until"))

    println(json.encodeToString(renderSchedule(schedule, overrides, from, until)))
}

/** Parses `--key=value` arguments into a map. */
private fun parseArgs(args: Array<String>): Map<String, String> =
    args.associate { arg ->
        val match = Regex("^--([a-z]+)=(.*)$").matchEntire(arg) ?: fail("unrecognised argument: $arg")
        match.groupValues[1] to match.groupValues[2]
    }

private fun Map<String, String>.require(key: String): String = this[key] ?: fail("missing --$key")

private fun fail(message: String): Nothing {
    System.err.println("error: $message\n$usage")
    exitProcess(1)
}
