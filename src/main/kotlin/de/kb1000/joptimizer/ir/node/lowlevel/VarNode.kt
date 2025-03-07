package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Block

interface VarNode {
    val i: Int

    val isUse: Boolean

    val type: VarType

    fun dropVarNode(block: Block)
}