package de.kb1000.joptimizer.ir.node.ssa

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.Type
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo
import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.ValueNode

class SSAGetInstanceFieldNode(
    var instance: Value,
    var owner: Class,
    var name: String,
    var type: Type,
) : SSANode(), ValueNode {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "@$nodeIndex = ssa_get_instance ${instance.valueToString()} ${owner.internalName}->$name:${type.descriptor}"

    override val nodeInfo: NodeInfo
        get() = Companion.nodeInfo

    override fun getUse(int: Int) = when (int) {
        0 -> instance
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun setUse(int: Int, value: Value) = when (int) {
        0 -> instance = value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun useCount() = 1
}