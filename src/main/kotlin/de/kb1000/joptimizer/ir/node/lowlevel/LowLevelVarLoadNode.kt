package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Block

class LowLevelVarLoadNode(override var i: Int, override var type: VarType) : LowLevelNode(), VarNode {
    override val isUse: Boolean
        get() = true
    override fun dropVarNode(block: Block) {
        throw IllegalStateException("Can't drop load node")
    }

    override fun toString() = "ll_load $type \$var$i"
}
