package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.Type
import de.kb1000.joptimizer.ir.node.Node

class LowLevelFieldNode(var nodeType: FieldNodeType, var owner: Class, var name: String, var type: Type) :
    LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    enum class FieldNodeType(private val text: String) {
        GET_STATIC("get_static"),
        PUT_STATIC("put_static"),
        GET_INSTANCE("get_instance"),
        PUT_INSTANCE("put_instance"),
        ;

        override fun toString() = text
    }

    override fun toString() = "ll_field $nodeType ${owner.internalName}->$name:${type.descriptor}"

    override val nodeInfo
        get() = LowLevelFieldNode.nodeInfo
}