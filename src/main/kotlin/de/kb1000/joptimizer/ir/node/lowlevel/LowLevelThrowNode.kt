package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo
import de.kb1000.joptimizer.ir.node.TerminalNode

class LowLevelThrowNode : LowLevelNode(), TerminalNode {
    companion object {
        // throw can definitely throw
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "ll_throw"

    override val nodeInfo: NodeInfo
        get() = LowLevelThrowNode.nodeInfo
}
