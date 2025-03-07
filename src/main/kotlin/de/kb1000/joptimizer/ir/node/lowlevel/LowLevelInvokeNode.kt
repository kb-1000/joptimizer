package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.MethodType
import de.kb1000.joptimizer.ir.ObjectType
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo

class LowLevelInvokeNode(
    var type: InvokeType,
    var owner: ObjectType,
    var name: String,
    var descriptor: MethodType,
    var isInterface: Boolean
) :
    LowLevelNode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    enum class InvokeType(private val text: String) {
        VIRTUAL("virtual"),
        SPECIAL("special"),
        STATIC("static"),
        INTERFACE("interface"),
        ;

        override fun toString() = text
    }

    override fun toString(): String {
        return "ll_invoke $type${if (isInterface) " interface" else ""} ${owner.descriptor}->$name$descriptor"
    }

    override val nodeInfo: NodeInfo
        get() = LowLevelInvokeNode.nodeInfo
}
