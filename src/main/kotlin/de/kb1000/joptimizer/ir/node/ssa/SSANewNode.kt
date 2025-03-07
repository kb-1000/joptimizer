package de.kb1000.joptimizer.ir.node.ssa

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo
import de.kb1000.joptimizer.ir.node.ValueNode

class SSANewNode(var clazz: Class) : SSANode(), ValueNode {
    companion object {
        private val nodeInfo: NodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString(): String {
        return "@$nodeIndex = ssa_new ${clazz.internalName}"
    }

    override val nodeInfo: NodeInfo
        get() = Companion.nodeInfo
}