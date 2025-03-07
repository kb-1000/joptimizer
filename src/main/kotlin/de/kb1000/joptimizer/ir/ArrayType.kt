package de.kb1000.joptimizer.ir

import java.util.*

data class ArrayType(val elementType: Type) : Type, ObjectType {
    companion object {
        private val cache = WeakHashMap<Type, ArrayType>()
        internal fun from(type: Type) = cache.computeIfAbsent(type, ::ArrayType)
    }
    override val descriptor
        get() = "[${elementType.descriptor}"

    override val humanName
        get() = "${elementType.humanName}[]"
}
