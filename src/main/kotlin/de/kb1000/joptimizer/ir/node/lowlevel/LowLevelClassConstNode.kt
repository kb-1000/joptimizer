package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.ObjectType
import de.kb1000.joptimizer.ir.node.Node

class LowLevelClassConstNode(var type: ObjectType) : LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "ll_class ${type.descriptor}"

    override val nodeInfo
        get() = LowLevelClassConstNode.nodeInfo
}
