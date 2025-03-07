package de.kb1000.joptimizer.ir.node.ssa

import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.ValueNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelBinOpNode.Operation
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelBinOpNode.OperationClass
import de.kb1000.joptimizer.ir.node.lowlevel.VarType

// TODO: can any of these throw?
class SSABinOpNode(var operation: Operation, var op0: Value, var op1: Value) : SSANode(), ValueNode {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "@$nodeIndex = ssa_binop $operation ${op0.valueToString()} ${op1.valueToString()}"

    override val nodeInfo
        get() = Companion.nodeInfo

    override fun getUse(int: Int) = when (int) {
        0 -> op0
        1 -> op1
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun setUse(int: Int, value: Value) = when (int) {
        0 -> op0 = value
        1 -> op1 = value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun useCount() = 2

    // This should return true only when the node can't throw  TODO: verify
    override fun isPure() =
        (OperationClass.DIVIDE != operation.opClass && OperationClass.MODULO != operation.opClass)
                || (VarType.INTEGER != operation.type && VarType.LONG != operation.type)
}
