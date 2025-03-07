package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.TerminalNode

class ReturnVoidNode : LowLevelNode(), TerminalNode, GenericNode {
    override fun toString() = "return_void"
}
