package de.kb1000.joptimizer.data

import de.kb1000.joptimizer.data.KnownMethod.KnownMethodBehavior
import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.Method

object java_lang_Object : KnownClass {
    override val classType: Class.ClassType
        get() = Class.ClassType.CLASS
    override val superClass: String
        get() = "java/lang/Object"
    override val interfaces
        get() = listOf<String>()
    override val loadingHasSideEffects
        get() = false

    override val methods
        get() = listOf(
            KnownMethod(Method.Type.CONSTRUCTOR, KnownMethodBehavior(hasSideEffects = false)),
            KnownMethod(Method.Type.METHOD, "toString()Ljava/lang/String;", KnownMethodBehavior(hasSideEffects = false))
        )

    override val fields
        get() = listOf<KnownField>()
}