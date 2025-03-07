package de.kb1000.joptimizer.data

import de.kb1000.joptimizer.ir.Class

interface KnownClass {
    val classType: Class.ClassType
    val superClass: String
    val interfaces: List<String>
    val loadingHasSideEffects
        get() = true

    /**
     * This list contains information about the methods of the class.
     * The information ONLY applies to the methods of this class, not any overridden methods!
     */
    val methods: List<KnownMethod>
    val fields: List<KnownField>

    companion object {
        @JvmField
        val KNOWN_CLASSES: Map<String, () -> KnownClass> = mapOf(
            "java/lang/Object" to { java_lang_Object },
            "kotlin/Unit" to { kotlin_Unit },
        )
    }
}