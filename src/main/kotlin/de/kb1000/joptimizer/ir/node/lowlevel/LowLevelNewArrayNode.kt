package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Type
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo

class LowLevelNewArrayNode(var type: Type) : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "ll_newarray ${type.descriptor}"

    override val nodeInfo: NodeInfo
        get() = LowLevelNewArrayNode.nodeInfo
}
