package de.tum.storemanager.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DurationUnit {
    SECONDS,
    MINUTES,
    HOURS,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS
}

@Serializable
sealed class Duration (
    open val value: Int,
    open val Unit: DurationUnit
)

@Serializable
data class Minutes(
    @SerialName("valueMinutes")
    override val value: Int
): Duration(value, DurationUnit.MINUTES) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Minutes

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }
}
