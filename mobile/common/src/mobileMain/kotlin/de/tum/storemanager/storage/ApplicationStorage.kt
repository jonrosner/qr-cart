package de.tum.storemanager.storage

import de.tum.storemanager.StoreService
import de.tum.storemanager.wrap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.properties.*
import kotlin.reflect.*

expect class ApplicationContext

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun ApplicationStorage(context: ApplicationContext): ApplicationStorage

interface ApplicationStorage {
    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putString(key: String, value: String)
    fun getString(key: String): String?
}

@UseExperimental(UnstableDefault::class)
inline operator fun <reified T> ApplicationStorage.invoke(
    serializer: KSerializer<T>,
    crossinline block: () -> T
): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
    private var currentValue: T? = null

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        val key = property.name
        currentValue = value
        putString(key, Json.stringify(serializer, value))
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        currentValue?.let { return it }

        val key = thisRef::class.qualifiedName!! + "_" + property.name

        val result = try {
            getString(key)?.let { Json.parse(serializer, it) }
        } catch (cause: Throwable) {
            null
        } ?: block()

        setValue(thisRef, property, result)
        return result
    }
}

@UseExperimental(ImplicitReflectionSerializer::class, ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
inline fun <reified T> ApplicationStorage.live(
    crossinline initial: () -> T
): ReadOnlyProperty<Any, ConflatedBroadcastChannel<T>> {
    val serializer = serializer<T>()

    return object : ReadOnlyProperty<Any, ConflatedBroadcastChannel<T>> {
        private var channel: ConflatedBroadcastChannel<T>? = null
        private var key: String? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): ConflatedBroadcastChannel<T> {
            if (channel == null) {
                key = thisRef::class.qualifiedName!! + "_" + property.name
                val value = try {
                    getString(key!!)?.let { Json.parse(serializer, it) }
                } catch (_: Throwable) {
                    null
                } ?: initial()

                channel = ConflatedBroadcastChannel(value)
                channel!!.asFlow().wrap().watch {
                    putString(key!!, Json.stringify(serializer, it))
                }
            }

            return channel!!
        }
    }
}
