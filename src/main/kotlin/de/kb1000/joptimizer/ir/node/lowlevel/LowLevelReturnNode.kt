package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.TerminalNode

class LowLevelReturnNode(var type: VarType) : LowLevelNode(), TerminalNode {
    override fun toString() = "ll_return $type"
}
