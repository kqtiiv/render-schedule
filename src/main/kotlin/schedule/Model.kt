package schedule

import java.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Schedule(
    val users: List<String>,
    @SerialName("handover_start_at") @Serializable(InstantSerializer::class) val handoverStartAt: Instant,
    @SerialName("handover_interval_days") val handoverIntervalDays: Int,
)

/** Used for both overrides (input) and rendered entries (output) — they share a shape. */
@Serializable
data class Entry(
    val user: String,
    @SerialName("start_at") @Serializable(InstantSerializer::class) val startAt: Instant,
    @SerialName("end_at") @Serializable(InstantSerializer::class) val endAt: Instant,
)

/** Reads/writes ISO-8601 UTC timestamps like "2026-11-06T17:00:00Z". */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
}
