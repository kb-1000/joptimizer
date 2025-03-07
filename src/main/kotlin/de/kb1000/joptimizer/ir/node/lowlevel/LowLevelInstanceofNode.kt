package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.ObjectType
import de.kb1000.joptimizer.ir.node.Node

class LowLevelInstanceofNode(var type: ObjectType) : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "ll_instanceof ${type.descriptor}"

    override val nodeInfo
        get() = LowLevelInstanceofNode.nodeInfo
}