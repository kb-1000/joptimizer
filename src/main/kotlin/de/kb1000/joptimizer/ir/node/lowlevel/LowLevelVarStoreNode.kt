package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Block

class LowLevelVarStoreNode(override var i: Int, override var type: VarType) : LowLevelNode(), VarNode {
    override val isUse: Boolean = false
    override fun dropVarNode(block: Block) {
        block.set(this, LowLevelPopNode(type.size).apply { line = this@LowLevelVarStoreNode.line })
    }

    override fun toString() = "ll_store $type \$var$i"
}
