package de.kb1000.joptimizer.ir.node.constant

import de.kb1000.joptimizer.ir.MethodType
import de.kb1000.joptimizer.ir.node.Value

/**
 * This may only be used in a [Value] context when it has been proven that evaluating this method type literal has no
 * side effects (for example when it only consists of primitive types, when all involved classes are already loaded, or
 * when it has been proven that all classes are present at runtime and their initializers have no side effects, and all
 * superclasses are either loaded or side effect free as well)
 */
class MethodTypeConstant(val methodType: MethodType) : Constant {
    override fun toString() = "methodtype $methodType"
}
