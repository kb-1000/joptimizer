package de.kb1000.joptimizer.data

data class KnownField(val signature: String, val fieldType: String, val type: Type = Type.INSTANCE) {
    enum class Type {
        INSTANCE, STATIC
    }
}
