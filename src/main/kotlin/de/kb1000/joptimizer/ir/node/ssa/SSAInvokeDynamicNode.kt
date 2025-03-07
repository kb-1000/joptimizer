package de.kb1000.joptimizer.ir.node.ssa

import de.kb1000.joptimizer.ir.MethodType
import de.kb1000.joptimizer.ir.node.NodeInfo
import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.ValueNode
import de.kb1000.joptimizer.ir.node.constant.Constant
import de.kb1000.joptimizer.ir.node.constant.MethodHandleConstant

abstract class SSAInvokeDynamicNode(
    var name: String,
    var methodType: MethodType,
    var bootstrapMethod: MethodHandleConstant,
    var bootstrapMethodArguments: List<Constant>,
    var arguments: MutableList<Value>
) : SSANode() {
    override fun toString(): String {
        return "ssa_invokedynamic \"$name\"$methodType from $bootstrapMethod(${bootstrapMethodArguments.joinToString()}) (${
            arguments.joinToString(
                transform = Value::valueToString
            )
        }"
    }

    override fun getUse(int: Int) = arguments[int]

    override fun setUse(int: Int, value: Value) {
        arguments[int] = value
    }

    override fun useCount() = arguments.size

    override val nodeInfo: NodeInfo
        get() = super.nodeInfo
}

class SSAInvokeDynamicVoidNode(
    name: String,
    methodType: MethodType,
    bootstrapMethod: MethodHandleConstant,
    bootstrapMethodArguments: List<Constant>,
    arguments: MutableList<Value>
) : SSAInvokeDynamicNode(name, methodType, bootstrapMethod, bootstrapMethodArguments, arguments)

class SSAInvokeDynamicValueNode(
    name: String,
    methodType: MethodType,
    bootstrapMethod: MethodHandleConstant,
    bootstrapMethodArguments: List<Constant>,
    arguments: MutableList<Value>
) : SSAInvokeDynamicNode(name, methodType, bootstrapMethod, bootstrapMethodArguments, arguments), ValueNode {
    override fun toString() = "@$nodeIndex = ${super.toString()}"
}