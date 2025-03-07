package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ObjectType
import de.kb1000.joptimizer.ir.node.Node

class LowLevelCastNode(var type: ObjectType) : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString(): String {
        val type = type
        return "ll_cast ${if (type is Class) type.internalName else type.descriptor}"
    }

    override val nodeInfo
        get() = LowLevelCastNode.nodeInfo
}