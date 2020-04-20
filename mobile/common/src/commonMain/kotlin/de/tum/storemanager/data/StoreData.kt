@file:UseSerializers(GMTDateSerializer::class, UUIDSerializer::class)

package de.tum.storemanager.data

import com.soywiz.korio.util.UUID
import de.tum.storemanager.serializers.GMTDateSerializer
import de.tum.storemanager.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class StoreData (
    val storeId: UUID
)
