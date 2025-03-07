package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Block

class LowLevelVarIncrementNode(override var i: Int, var value: Int) : LowLevelNode(), VarNode {
    // while iinc reads from the variable, it doesn't push the result to stack, it must be read by iload
    override val isUse: Boolean
        get() = false
    override val type: VarType
        get() = VarType.INTEGER
    override fun dropVarNode(block: Block) {
        block.remove(this)
    }

    override fun toString() = "ll_increment int \$var$i += $value"
}
