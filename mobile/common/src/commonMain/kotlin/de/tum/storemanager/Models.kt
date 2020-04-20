@file:UseSerializers(GMTDateSerializer::class, UUIDSerializer::class)

package de.tum.storemanager

import com.soywiz.korio.util.UUID
import de.tum.storemanager.data.Minutes
import de.tum.storemanager.serializers.GMTDateSerializer
import de.tum.storemanager.serializers.UUIDSerializer
import io.ktor.util.date.GMTDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


sealed class AuthToken

@Serializable
data class JWTAuthToken(val jwtToken: String) : AuthToken()

data class Email(
    val rawEmail: String
) {
    override fun toString(): String {
        return rawEmail
    }
}

@Serializable
data class Reservation (
    val id: UUID,
    val startDate: GMTDate,
    val checkinTime: Minutes,
    val duration: Minutes,
    val checkedInTime: GMTDate? = null
)

@Serializable
data class Store (
    val id: UUID,
    @SerialName("name")
    val displayName: String,

    @SerialName("maximumCapacity")
    val maximumPeople: Int,
    val reservationPercentage: Double = 0.3,
    val currentNoPeopleInStore: Int,
    val numRemainingReservations: Int,
    val numUpcomingReservations: Int,
    val address: String
) {

    /**
     * computed property
     */
    val createdAt: GMTDate = GMTDate()

    /**
     * percentage walk-in
     */
    val walkInPercentage = 1 - reservationPercentage

    /**
     * defines how many people can walk in.
     * Basically as we do not know
     */
    val allowedWalkIn: Int
        get() = ceil((maximumPeople - currentNoPeopleInStore - numRemainingReservations - numUpcomingReservations) * walkInPercentage).toInt() - max(0, newWalkIn)

    /**
     * Represents new walkins since last server sync
     */
    var newWalkIn: Int = 0

    override fun toString(): String {
        return displayName;
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Store

        if (id != other.id) return false
        if (displayName != other.displayName) return false
        if (maximumPeople != other.maximumPeople) return false
        if (reservationPercentage != other.reservationPercentage) return false
        if (currentNoPeopleInStore != other.currentNoPeopleInStore) return false
        if (numRemainingReservations != other.numRemainingReservations) return false
        if (numUpcomingReservations != other.numUpcomingReservations) return false
        if (createdAt != other.createdAt) return false
        if (walkInPercentage != other.walkInPercentage) return false
        if (newWalkIn != other.newWalkIn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + maximumPeople
        result = 31 * result + reservationPercentage.hashCode()
        result = 31 * result + currentNoPeopleInStore
        result = 31 * result + numRemainingReservations
        result = 31 * result + numUpcomingReservations
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + walkInPercentage.hashCode()
        result = 31 * result + newWalkIn
        return result
    }
}

data class User(
    val name: String,
    val email: Email,
    val token: AuthToken?
)
