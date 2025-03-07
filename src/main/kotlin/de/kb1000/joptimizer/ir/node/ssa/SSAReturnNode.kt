package de.kb1000.joptimizer.ir.node.ssa

import de.kb1000.joptimizer.ir.node.TerminalNode
import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.lowlevel.VarType

class SSAReturnNode(var type: VarType, var value: Value) : SSANode(), TerminalNode {
    override fun toString() = "ssa_return $type ${value.valueToString()}"

    override fun getUse(int: Int) = when (int) {
        0 -> value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun setUse(int: Int, value: Value) = when (int) {
        0 -> this.value = value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun useCount() = 1
}
