package de.kb1000.joptimizer.ir.node.ssa

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.Type
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo
import de.kb1000.joptimizer.ir.node.Value

class SSAPutInstanceFieldNode(
    var instance: Value,
    var owner: Class,
    var name: String,
    var type: Type,
    var value: Value,
) : SSANode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "ssa_put_instance ${instance.valueToString()} ${owner.internalName}->$name:${type.descriptor} = ${value.valueToString()}"

    override val nodeInfo: NodeInfo
        get() = Companion.nodeInfo

    override fun getUse(int: Int) = when (int) {
        0 -> instance
        1 -> value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun setUse(int: Int, value: Value) = when (int) {
        0 -> instance = value
        1 -> this.value = value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun useCount() = 2
}

