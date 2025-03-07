package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.MethodType
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.constant.Constant
import de.kb1000.joptimizer.ir.node.constant.MethodHandleConstant

class LowLevelInvokeDynamicNode(
    var name: String,
    var methodType: MethodType,
    var bootstrapMethod: MethodHandleConstant,
    var bootstrapMethodArguments: List<Constant>
) : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() =
        "ll_invokedynamic \"$name\"$methodType from $bootstrapMethod(${bootstrapMethodArguments.joinToString()})"

    override val nodeInfo
        get() = LowLevelInvokeDynamicNode.nodeInfo
}
