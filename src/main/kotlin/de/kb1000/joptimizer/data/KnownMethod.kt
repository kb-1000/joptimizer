package de.kb1000.joptimizer.data

import de.kb1000.joptimizer.ir.Method
import de.kb1000.joptimizer.ir.node.constant.Constant

data class KnownMethod(
    val type: Method.Type,
    val signature: String,
    val methodBehavior: KnownMethodBehavior = KnownMethodBehavior()
) {
    data class KnownMethodBehavior(val hasSideEffects: Boolean = true, val constantReturnValue: Constant? = null)

    constructor(type: Method.Type.CONSTRUCTOR, methodBehavior: KnownMethodBehavior = KnownMethodBehavior()) : this(
        type,
        "<init>()V",
        methodBehavior
    )

    constructor(
        type: Method.Type.STATIC_CONSTRUCTOR,
        methodBehavior: KnownMethodBehavior = KnownMethodBehavior()
    ) : this(type, "<clinit>()V", methodBehavior)
}
