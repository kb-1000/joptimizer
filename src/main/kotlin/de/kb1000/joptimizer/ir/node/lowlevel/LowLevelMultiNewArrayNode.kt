package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Type
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo

class LowLevelMultiNewArrayNode(var type: Type, var dimensions: Int) : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "ll_multinewarray ${type.descriptor} dimensions $dimensions"

    override val nodeInfo: NodeInfo
        get() = LowLevelMultiNewArrayNode.nodeInfo
}
