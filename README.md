# render-schedule

A command-line tool that renders an on-call schedule. It takes a rotation (users who take turns
covering fixed-length shifts) and a list of overrides (temporary cover), and outputs who is on call
for each part of a given time window.

Written in Kotlin, using `kotlinx.serialization` for JSON. The original task description is in
[`ProblemDescription.md`](./ProblemDescription.md).

## Requirements

- **Java 17 or newer** must be installed to run Gradle. Check with `java -version`.

## Running it

From the project root:

```console
$ ./render-schedule \
    --schedule=examples/schedule.json \
    --overrides=examples/overrides.json \
    --from='2026-11-06T17:00:00Z' \
    --until='2026-11-20T17:00:00Z'
```

The script builds the project and then runs it. The rendered schedule is printed to stdout as a JSON array. Any build output goes to stderr, so you can safely pipe or redirect the result:

```console
$ ./render-schedule ... > output.json
```

| Argument | Description |
|---|---|
| `--schedule` | Path to a JSON file describing the rotation (`users`, `handover_start_at`, `handover_interval_days`) |
| `--overrides` | Path to a JSON file containing an array of overrides (`user`, `start_at`, `end_at`) |
| `--from` | Start of the window to render (ISO-8601 UTC, e.g. `2026-11-06T17:00:00Z`) |
| `--until` | End of the window to render |

Example input and expected output are in [`examples/`](./examples).

> **"Permission denied"** Run `chmod +x render-schedule gradlew` once.
>
> **On Windows**, run the script from WSL or Git Bash.

## Running the tests

```console
$ ./gradlew test
```

The tests run the scheduler against the example in `examples/` and check the output matches
`examples/expected_output.json`.

## How it works

All the scheduling logic is in [`Scheduler.kt`](./src/main/kotlin/schedule/Scheduler.kt). The
algorithm has three steps:

1. **Project the rotation.** Starting from `handover_start_at`, generate shifts of
   `handover_interval_days`, cycling through `users` in order, and keep the ones that overlap the
   window.
2. **Apply overrides: cut a hole, then fill it.** For each override, cut its time range out of the
   rotation shifts. A shift becomes 0, 1 or 2 pieces depending on how the override overlaps it.
   Then add the overrides themselves and sort everything by start time. An override that crosses a
   handover cuts the end off one shift and the start off the next, with no special-casing.
3. **Truncate to the window.** Trim every entry to `[from, until)` and drop anything left with no
   length, such as an override entirely outside the window.

All time ranges are treated as **half-open**: an entry includes its start but not its end. So a
shift ending at 17:00 and the next starting at 17:00 don't overlap, and an entry that ends exactly
at `from` is excluded.

I chose this approach because it reads the way people describe overrides therefore, is easily abstractable and traceable, and each step can be checked on its own. The brief puts correctness ahead of performance, and with windows of a few weeks and fewer than 100 overrides, the simple loop is more than fast enough.

### Project layout

```
render-schedule                  Entry-point script: builds, then runs the app
src/main/kotlin/schedule/
  Main.kt                        Parses arguments, reads the input files, prints JSON
  Model.kt                       Schedule and Entry data classes, timestamp (de)serialisation
  Scheduler.kt                   The scheduling algorithm
src/test/kotlin/schedule/
  SchedulerTest.kt               Tests
examples/                        Example schedule, overrides and expected output
```

### Assumptions and decisions

- **All times are UTC**, so a "day" is always exactly 24 hours.
- **Overrides don't overlap each other** and always start after `handover_start_at`, as stated in
  the brief.
- **Nothing is on call before `handover_start_at`.** If `from` is earlier, the output starts at
  `handover_start_at`.
- **Entries aren't merged.** If an override gives a shift to the person who's already on call, the
  output contains consecutive entries for the same user rather than one combined entry.
- **A schedule needs at least one user.**
