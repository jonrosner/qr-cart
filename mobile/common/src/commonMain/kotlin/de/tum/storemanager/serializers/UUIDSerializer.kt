package de.tum.storemanager.serializers

import com.soywiz.korio.util.UUID
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor


/**
 * According to mask: "yyyy-MM-dd'T'HH:mm:ss"
 */
@Serializer(forClass = UUID::class)
internal object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("UUIDSerializer")

    override fun serialize(encoder: Encoder, obj: UUID) {
        val value = obj.toString()
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): UUID {
        val value = decoder.decodeString()
        with(value) {
            return UUID(value)
        }
    }
}
