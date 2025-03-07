package de.kb1000.joptimizer.ir.node.adapter

import de.kb1000.joptimizer.ir.Block
import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.lowlevel.VarNode
import de.kb1000.joptimizer.ir.node.lowlevel.VarType

class AdapterSetRegister(override var type: VarType, override var i: Int, var value: Value) : AdapterNode(), VarNode {
    override val isUse: Boolean
        get() = false

    override fun dropVarNode(block: Block) {
        block.remove(this)
    }

    override fun toString() = "adapter_set_register $type \$var$i = ${value.valueToString()}"

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