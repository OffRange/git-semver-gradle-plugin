package de.davis.gradle.plugin.versioning

import org.gradle.api.model.ObjectFactory
import kotlin.reflect.KProperty

internal class GradleProperty<V : Any>(objectFactory: ObjectFactory, type: Class<V>, defaultValue: V? = null) {
    private val property = objectFactory.property(type).apply {
        set(defaultValue)
    }

    operator fun getValue(thisRef: Any, kProperty: KProperty<*>): V = property.get()
    operator fun setValue(thisRef: Any, kProperty: KProperty<*>, value: V) = property.set(value)
}

internal class OptionalGradleProperty<V : Any>(objectFactory: ObjectFactory, type: Class<V>, defaultValue: V? = null) {
    private val property = objectFactory.property(type).apply {
        set(defaultValue)
    }

    operator fun getValue(thisRef: Any, kProperty: KProperty<*>): V? = property.orNull
    operator fun setValue(thisRef: Any, kProperty: KProperty<*>, value: V?) = property.set(value)
}

internal inline fun <reified T : Any> ObjectFactory.createGradleProperty(defaultValue: T? = null): GradleProperty<T> =
    GradleProperty(this, T::class.java, defaultValue)

internal inline fun <reified T : Any> ObjectFactory.createOptionalGradleProperty(defaultValue: T? = null): OptionalGradleProperty<T> =
    OptionalGradleProperty(this, T::class.java, defaultValue)