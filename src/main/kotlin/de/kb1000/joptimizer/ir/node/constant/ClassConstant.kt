package de.kb1000.joptimizer.ir.node.constant

import de.kb1000.joptimizer.ir.ObjectType
import de.kb1000.joptimizer.ir.node.Value

/**
 * This may only be used in a [Value] context when it has been proven that evaluating this class literal has no side
 * effects (such as when the class is already loaded, or when it has been proven that the class is present at runtime
 * and its initializer has no side effects, and all superclasses are either loaded or side effect free as well)
 */
class ClassConstant(val type: ObjectType) : Constant {
    override fun toString() = "class ${type.descriptor}"
}