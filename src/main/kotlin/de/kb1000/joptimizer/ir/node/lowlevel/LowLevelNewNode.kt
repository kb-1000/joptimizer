package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo

class LowLevelNewNode(val clazz: Class) : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString(): String {
        return "ll_new ${clazz.internalName}"
    }

    override val nodeInfo: NodeInfo
        get() = LowLevelNewNode.nodeInfo
}
