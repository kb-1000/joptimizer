package de.kb1000.joptimizer.data

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.Method
import de.kb1000.joptimizer.ir.node.constant.StringConstant

object kotlin_Unit : KnownClass {
    override val classType: Class.ClassType
        get() = Class.ClassType.CLASS
    override val superClass: String
        get() = "java/lang/Object"
    override val interfaces: List<String>
        get() = listOf()
    override val loadingHasSideEffects
        get() = false

    override val methods: List<KnownMethod>
        get() = listOf(
            KnownMethod(
                Method.Type.METHOD,
                "toString()Ljava/lang/String;",
                KnownMethod.KnownMethodBehavior(
                    hasSideEffects = false,
                    constantReturnValue = StringConstant("kotlin.Unit")
                )
            ),
        )

    override val fields: List<KnownField>
        get() = listOf(
            KnownField("INSTANCE", "Lkotlin/Unit;", KnownField.Type.STATIC),
        )
}
