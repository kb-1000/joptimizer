package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.Node

class LowLevelArrayLengthNode : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "ll_arraylength"

    override val nodeInfo
        get() = LowLevelArrayLengthNode.nodeInfo
}
