package de.kb1000.joptimizer.ir.node.ssa

import de.kb1000.joptimizer.ir.MethodType
import de.kb1000.joptimizer.ir.ObjectType
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.NodeInfo
import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.ValueNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelInvokeNode.InvokeType

abstract class SSAInvokeNode(
    var type: InvokeType,
    var owner: ObjectType,
    var name: String,
    var descriptor: MethodType,
    var isInterface: Boolean,
    var arguments: MutableList<Value>
) : SSANode() {
    companion object {
        private val nodeInfo = Node.nodeInfo.copy(canThrow = true)
    }

    override fun toString() = "${if (isInterface) " interface" else ""} ${owner.descriptor}->$name$descriptor (${arguments.joinToString(transform = Value::valueToString)})"

    override val nodeInfo: NodeInfo
        get() = Companion.nodeInfo
}

abstract class SSAInstanceInvokeNode(
    type: InvokeType,
    owner: ObjectType,
    name: String,
    descriptor: MethodType,
    isInterface: Boolean,
    arguments: MutableList<Value>,
    var instance: Value,
) : SSAInvokeNode(type, owner, name, descriptor, isInterface, arguments) {
    override fun toString(): String {
        return "ssa_instance_invoke $type ${instance.valueToString()}->${super.toString()}"
    }

    override fun getUse(int: Int) =
        if (int == 0) instance
        else arguments[int - 1]

    override fun setUse(int: Int, value: Value) =
        if (int == 0) instance = value
        else arguments[int - 1] = value

    override fun useCount() = arguments.size + 1
}

class SSAInstanceInvokeVoidNode(
    type: InvokeType,
    owner: ObjectType,
    name: String,
    descriptor: MethodType,
    isInterface: Boolean,
    arguments: MutableList<Value>,
    instance: Value
) : SSAInstanceInvokeNode(type, owner, name, descriptor, isInterface, arguments, instance)

class SSAInstanceInvokeValueNode(
    type: InvokeType,
    owner: ObjectType,
    name: String,
    descriptor: MethodType,
    isInterface: Boolean,
    arguments: MutableList<Value>,
    instance: Value
) : SSAInstanceInvokeNode(type, owner, name, descriptor, isInterface, arguments, instance), ValueNode {
    override fun toString() = "@$nodeIndex = ${super.toString()}"
}

abstract class SSAStaticInvokeNode(
    type: InvokeType,
    owner: ObjectType,
    name: String,
    descriptor: MethodType,
    isInterface: Boolean,
    arguments: MutableList<Value>,
) : SSAInvokeNode(type, owner, name, descriptor, isInterface, arguments) {
    override fun toString(): String {
        return "ssa_static_invoke $type ${super.toString()}"
    }

    override fun getUse(int: Int) = arguments[int]

    override fun setUse(int: Int, value: Value) {
        arguments[int] = value
    }

    override fun useCount() = arguments.size
}

class SSAStaticInvokeVoidNode(
    type: InvokeType,
    owner: ObjectType,
    name: String,
    descriptor: MethodType,
    isInterface: Boolean,
    arguments: MutableList<Value>
) : SSAStaticInvokeNode(type, owner, name, descriptor, isInterface, arguments)

class SSAStaticInvokeValueNode(
    type: InvokeType,
    owner: ObjectType,
    name: String,
    descriptor: MethodType,
    isInterface: Boolean,
    arguments: MutableList<Value>
) : SSAStaticInvokeNode(type, owner, name, descriptor, isInterface, arguments), ValueNode {
    override fun toString() = "@$nodeIndex = ${super.toString()}"
}
