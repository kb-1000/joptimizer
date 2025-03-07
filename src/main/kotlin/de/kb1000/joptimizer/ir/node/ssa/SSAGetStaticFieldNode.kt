package de.kb1000.joptimizer.ir.node.ssa

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.Type
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo
import de.kb1000.joptimizer.ir.node.ValueNode

class SSAGetStaticFieldNode(var owner: Class, var name: String, var type: Type) : SSANode(), ValueNode {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "@$nodeIndex = ssa_get_static ${owner.internalName}->$name:${type.descriptor}"

    override val nodeInfo: NodeInfo
        get() = Companion.nodeInfo
}