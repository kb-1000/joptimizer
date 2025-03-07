package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.Node

class LowLevelArrayStoreNode(val type: ArrayLoadStoreType) : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "ll_arraystore $type"

    override val nodeInfo
        get() = LowLevelArrayStoreNode.nodeInfo
}
