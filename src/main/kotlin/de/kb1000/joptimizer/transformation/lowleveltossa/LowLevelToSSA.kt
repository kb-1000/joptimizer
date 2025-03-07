package de.kb1000.joptimizer.transformation.lowleveltossa

import de.kb1000.joptimizer.ir.Method
import de.kb1000.joptimizer.ir.PrimitiveType
import de.kb1000.joptimizer.ir.node.TerminalNode
import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.adapter.*
import de.kb1000.joptimizer.ir.node.constant.IntConstant
import de.kb1000.joptimizer.ir.node.constant.StringConstant
import de.kb1000.joptimizer.ir.node.lowlevel.*
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelDupNode.DupLayout
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelFieldNode.FieldNodeType
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelInvokeNode.InvokeType
import de.kb1000.joptimizer.ir.node.ssa.*
import de.kb1000.joptimizer.optimizations.ssa.DeadCodeElimination
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelBinOpNode.Operation as BinOpOperation

// XXX: there's something really wrong in double/long handling here
//  should probably introduce a double/long "merging"/"unmerging" node (how would we handle 2 node return values?)

class LowLevelToSSA {
    fun transform(method: Method) {
        for (block in method) {
            for (node in block) {
                when (node) {
                    is AdapterNode, is SSANode, is GenericNode -> {}
                    is LowLevelVarLoadNode -> {
                        val newNode = AdapterGetRegister(node.type, node.i).apply { line = node.line }
                        block.insertBefore(node, newNode)
                        if (node.type.size == 1)
                            block.set(node, AdapterPushNode(newNode).apply { line = node.line })
                        else {
                            val part1 = AdapterSplitLongValueNode(
                                if (node.type == VarType.LONG) AdapterMergeLongValueNode.Kind.LONG else AdapterMergeLongValueNode.Kind.DOUBLE,
                                0,
                                newNode
                            ).apply { line = node.line }
                            block.insertBefore(node, part1)
                            val part2 = AdapterSplitLongValueNode(
                                if (node.type == VarType.LONG) AdapterMergeLongValueNode.Kind.LONG else AdapterMergeLongValueNode.Kind.DOUBLE,
                                1,
                                newNode
                            ).apply { line = node.line }
                            block.insertBefore(node, part2)
                            block.insertBefore(node, AdapterPushNode(part1).apply { line = node.line })
                            block.set(node, AdapterPushNode(part2).apply { line = node.line })
                        }
                    }
                    is LowLevelFieldNode -> {
                        // XXX: long/double handling
                        when (node.nodeType) {
                            FieldNodeType.GET_STATIC -> {
                                val valueNode =
                                    SSAGetStaticFieldNode(node.owner, node.name, node.type).apply { line = node.line }
                                block.insertBefore(node, valueNode)
                                if (node.type.length == 1)
                                    block.set(node, AdapterPushNode(valueNode).apply { line = node.line })
                                else {
                                    val part1 = AdapterSplitLongValueNode(
                                        if (PrimitiveType.LONG == node.type) AdapterMergeLongValueNode.Kind.LONG else AdapterMergeLongValueNode.Kind.DOUBLE,
                                        0,
                                        valueNode
                                    ).apply { line = node.line }
                                    block.insertBefore(node, part1)
                                    val part2 = AdapterSplitLongValueNode(
                                        if (PrimitiveType.LONG == node.type) AdapterMergeLongValueNode.Kind.LONG else AdapterMergeLongValueNode.Kind.DOUBLE,
                                        1,
                                        valueNode
                                    ).apply { line = node.line }
                                    block.insertBefore(node, part2)
                                    block.insertBefore(node, AdapterPushNode(part1).apply { line = node.line })
                                    block.set(node, AdapterPushNode(part2).apply { line = node.line })
                                }
                            }
                            FieldNodeType.PUT_STATIC -> TODO()
                            FieldNodeType.GET_INSTANCE -> {
                                val instancePopNode = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, instancePopNode)
                                val valueNode = SSAGetInstanceFieldNode(
                                    instancePopNode,
                                    node.owner,
                                    node.name,
                                    node.type,
                                ).apply { line = node.line }
                                block.insertBefore(
                                    node,
                                    valueNode
                                )
                                if (node.type.length == 1)
                                    block.set(node, AdapterPushNode(valueNode).apply { line = node.line })
                                else {
                                    val part1 = AdapterSplitLongValueNode(
                                        if (PrimitiveType.LONG == node.type) AdapterMergeLongValueNode.Kind.LONG else AdapterMergeLongValueNode.Kind.DOUBLE,
                                        0,
                                        valueNode
                                    ).apply { line = node.line }
                                    block.insertBefore(node, part1)
                                    val part2 = AdapterSplitLongValueNode(
                                        if (PrimitiveType.LONG == node.type) AdapterMergeLongValueNode.Kind.LONG else AdapterMergeLongValueNode.Kind.DOUBLE,
                                        1,
                                        valueNode
                                    ).apply { line = node.line }
                                    block.insertBefore(node, part2)
                                    block.insertBefore(node, AdapterPushNode(part1).apply { line = node.line })
                                    block.set(node, AdapterPushNode(part2).apply { line = node.line })
                                }
                            }
                            FieldNodeType.PUT_INSTANCE -> {
                                val instancePopNode = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, instancePopNode)
                                val valueNode = if (node.type.length != 2)
                                    AdapterPopNode().apply { line = node.line }
                                else {
                                    val part2 = AdapterPopNode().apply { line = node.line }
                                    block.insertBefore(node, part2)
                                    val part1 = AdapterPopNode().apply { line = node.line }
                                    block.insertBefore(node, part1)
                                    AdapterMergeLongValueNode(
                                        if (PrimitiveType.LONG == node.type) AdapterMergeLongValueNode.Kind.LONG
                                        else AdapterMergeLongValueNode.Kind.DOUBLE,
                                        part1, part2
                                    ).apply { line = node.line }
                                }
                                block.insertBefore(node, valueNode)
                                block.set(
                                    node,
                                    SSAPutInstanceFieldNode(
                                        instancePopNode,
                                        node.owner,
                                        node.name,
                                        node.type,
                                        valueNode
                                    ).apply { line = node.line }
                                )
                            }
                        }
                    }
                    is LowLevelInvokeNode -> {
                        val arguments = node.descriptor.argumentTypes.asReversed().map { type ->
                            val argument = if (type.length == 2) {
                                val part2 = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, part2)
                                val part1 = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, part1)
                                AdapterMergeLongValueNode(
                                    if (PrimitiveType.LONG == type) AdapterMergeLongValueNode.Kind.LONG
                                    else AdapterMergeLongValueNode.Kind.DOUBLE,
                                    part1, part2
                                ).apply { line = node.line }
                            } else
                                AdapterPopNode().apply { line = node.line }
                            block.insertBefore(node, argument)
                            argument
                        }.toMutableList<Value>()
                        val instance: Value?
                        when (node.type) {
                            InvokeType.VIRTUAL, InvokeType.SPECIAL, InvokeType.INTERFACE -> {
                                instance = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, instance)
                            }
                            else -> instance = null
                        }
                        val newNode = if (instance != null)
                            if (node.descriptor.returnType == PrimitiveType.VOID)
                                SSAInstanceInvokeVoidNode(
                                    node.type,
                                    node.owner,
                                    node.name,
                                    node.descriptor,
                                    node.isInterface,
                                    arguments,
                                    instance
                                )
                            else
                                SSAInstanceInvokeValueNode(
                                    node.type,
                                    node.owner,
                                    node.name,
                                    node.descriptor,
                                    node.isInterface,
                                    arguments,
                                    instance
                                )
                        else
                            if (node.descriptor.returnType == PrimitiveType.VOID)
                                SSAStaticInvokeVoidNode(
                                    node.type,
                                    node.owner,
                                    node.name,
                                    node.descriptor,
                                    node.isInterface,
                                    arguments
                                )
                            else
                                SSAStaticInvokeValueNode(
                                    node.type,
                                    node.owner,
                                    node.name,
                                    node.descriptor,
                                    node.isInterface,
                                    arguments
                                )
                        newNode.line = node.line
                        if (newNode is Value) {
                            block.insertBefore(node, newNode)
                            if (node.descriptor.returnType.length == 2) {
                                val kind =
                                    if (PrimitiveType.LONG == node.descriptor.returnType) AdapterMergeLongValueNode.Kind.LONG
                                    else AdapterMergeLongValueNode.Kind.DOUBLE
                                val part1 = AdapterSplitLongValueNode(kind, 0, newNode).apply { line = node.line }
                                block.insertBefore(node, part1)
                                block.insertBefore(node, AdapterPushNode(part1).apply { line = node.line })
                                val part2 = AdapterSplitLongValueNode(kind, 1, newNode).apply { line = node.line }
                                block.insertBefore(node, part2)
                                block.set(node, AdapterPushNode(part2).apply { line = node.line })
                            } else
                                block.set(node, AdapterPushNode(newNode).apply { line = node.line })
                        } else
                            block.set(node, newNode)
                    }
                    is LowLevelNewNode -> {
                        val newNode = SSANewNode(node.clazz).apply { line = node.line }
                        block.insertBefore(node, newNode)
                        block.set(node, AdapterPushNode(newNode).apply { line = node.line })
                    }
                    is LowLevelDupNode -> {
                        when (node.layout) {
                            DupLayout.DUP -> {
                                val pop = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, pop)
                                block.insertBefore(node, AdapterPushNode(pop).apply { line = node.line })
                                block.set(node, AdapterPushNode(pop).apply { line = node.line })
                            }
                            DupLayout.DUP_X1 -> TODO()
                            DupLayout.DUP_X2 -> TODO()
                            DupLayout.DUP2 -> TODO()
                            DupLayout.DUP2_X1 -> TODO()
                            DupLayout.DUP2_X2 -> TODO()
                            DupLayout.SWAP -> TODO()
                        }
                    }
                    is LowLevelIntConstNode -> block.set(
                        node,
                        AdapterPushNode(IntConstant(node.value)).apply { line = node.line })
                    is LowLevelBinOpNode -> {
                        when (node.operation) {
                            BinOpOperation.INTEGER_ADD -> {
                                val op0 = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, op0)
                                val op1 = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, op1)
                                val newNode = SSABinOpNode(node.operation, op0, op1).apply { line = node.line }
                                block.insertBefore(node, newNode)
                                block.set(node, AdapterPushNode(newNode).apply { line = node.line })
                            }
                            BinOpOperation.LONG_ADD -> TODO()
                            BinOpOperation.FLOAT_ADD -> TODO()
                            BinOpOperation.DOUBLE_ADD -> TODO()
                            BinOpOperation.INTEGER_SUBTRACT -> TODO()
                            BinOpOperation.LONG_SUBTRACT -> TODO()
                            BinOpOperation.FLOAT_SUBTRACT -> TODO()
                            BinOpOperation.DOUBLE_SUBTRACT -> TODO()
                            BinOpOperation.INTEGER_MULTIPLY -> TODO()
                            BinOpOperation.LONG_MULTIPLY -> TODO()
                            BinOpOperation.FLOAT_MULTIPLY -> TODO()
                            BinOpOperation.DOUBLE_MULTIPLY -> TODO()
                            BinOpOperation.INTEGER_DIVIDE -> TODO()
                            BinOpOperation.LONG_DIVIDE -> TODO()
                            BinOpOperation.FLOAT_DIVIDE -> TODO()
                            BinOpOperation.DOUBLE_DIVIDE -> TODO()
                            BinOpOperation.INTEGER_MODULO -> TODO()
                            BinOpOperation.LONG_MODULO -> TODO()
                            BinOpOperation.FLOAT_MODULO -> TODO()
                            BinOpOperation.DOUBLE_MODULO -> TODO()
                            BinOpOperation.INTEGER_LEFT_SHIFT -> TODO()
                            BinOpOperation.LONG_LEFT_SHIFT -> TODO()
                            BinOpOperation.INTEGER_RIGHT_SHIFT -> TODO()
                            BinOpOperation.LONG_RIGHT_SHIFT -> TODO()
                            BinOpOperation.INTEGER_UNSIGNED_RIGHT_SHIFT -> TODO()
                            BinOpOperation.LONG_UNSIGNED_RIGHT_SHIFT -> TODO()
                            BinOpOperation.INTEGER_AND -> TODO()
                            BinOpOperation.LONG_AND -> TODO()
                            BinOpOperation.INTEGER_OR -> TODO()
                            BinOpOperation.LONG_OR -> TODO()
                            BinOpOperation.INTEGER_XOR -> TODO()
                            BinOpOperation.LONG_XOR -> TODO()
                        }
                    }
                    is LowLevelPopNode -> {
                        if (node.count > 0) {
                            for (i in 0 until (node.count - 1)) {
                                block.insertBefore(node, AdapterPopNode().apply { line = node.line })
                            }
                            block.set(node, AdapterPopNode().apply { line = node.line })
                        }
                    }
                    is LowLevelVarStoreNode -> {
                        if (node.type.size != 2) {
                            val value = AdapterPopNode().apply { line = node.line }
                            block.insertBefore(node, value)
                            block.set(node, AdapterSetRegister(node.type, node.i, value).apply { line = node.line })
                        } else
                            TODO()
                    }
                    is LowLevelReturnNode -> {
                        when (node.type) {
                            VarType.INTEGER, VarType.FLOAT, VarType.OBJECT -> {
                                val value = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, value)
                                block.set(node, SSAReturnNode(node.type, value).apply { line = node.line })
                            }
                            VarType.LONG, VarType.DOUBLE -> {
                                val part2 = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, part2)
                                val part1 = AdapterPopNode().apply { line = node.line }
                                block.insertBefore(node, part1)
                                val value = AdapterMergeLongValueNode(
                                    if (node.type == VarType.LONG) AdapterMergeLongValueNode.Kind.LONG else AdapterMergeLongValueNode.Kind.DOUBLE,
                                    part1,
                                    part2
                                )
                                block.insertBefore(node, value)
                                block.set(node, SSAReturnNode(node.type, value).apply { line = node.line })
                            }
                        }
                    }
                    is LowLevelStringConstNode -> block.set(
                        node,
                        AdapterPushNode(StringConstant(node.value)).apply { line = node.line })
                    is LowLevelConditionalJumpNode -> {
                        when (node.type) {
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_EQUAL_0,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_NOT_EQUAL_0,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_LESS_THAN_0,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_GREATER_OR_EQUAL_0,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_GREATER_THAN_0,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_LESS_OR_EQUAL_0,
                            LowLevelConditionalJumpNode.ComparisonType.OBJECT_NULL,
                            LowLevelConditionalJumpNode.ComparisonType.OBJECT_NOT_NULL -> {
                                val value = AdapterPopNode()
                                block.insertBefore(node, value)
                                TODO()
                            }
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_EQUAL,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_NOT_EQUAL,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_LESS_THAN,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_GREATER_OR_EQUAL,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_GREATER_THAN,
                            LowLevelConditionalJumpNode.ComparisonType.INTEGER_LESS_OR_EQUAL,
                            LowLevelConditionalJumpNode.ComparisonType.OBJECT_EQUAL,
                            LowLevelConditionalJumpNode.ComparisonType.OBJECT_NOT_EQUAL -> TODO()
                        }
                    }
                    else -> throw IllegalStateException("${node.javaClass.simpleName} is not an SSA node")
                }
            }
            for (node in block.reversed()) {
                if (node is AdapterPushNode) {
                    val nextNode = node.nextNode
                    if (nextNode is AdapterPopNode) {
                        method.replaceUses(nextNode, node.value)
                        block.remove(node)
                        block.remove(nextNode)
                    }
                }
            }

            for (i in 0..3)
                for (node in block) {
                    if (node is AdapterPushNode) {
                        var nextNode = node.nextNode
                        while (nextNode != null && nextNode !is TerminalNode && (nextNode !is LowLevelNode || nextNode is GenericNode) && nextNode !is AdapterPushNode && nextNode !is AdapterPopNode)
                            nextNode = nextNode.nextNode

                        if (nextNode is AdapterPopNode) {
                            method.replaceUses(nextNode, node.value)
                            block.remove(node)
                            block.remove(nextNode)
                        }
                    }
                }

            for (i in 0..1)
                for (node in block) {
                    if (node is AdapterMergeLongValueNode) {
                        val part1 = node.part1 as? AdapterSplitLongValueNode ?: continue
                        val part2 = node.part2 as? AdapterSplitLongValueNode ?: continue

                        if (part1.value == part2.value) {
                            method.replaceUses(node, part1.value)
                            // the splits will be removed by a DCE pass
                            block.remove(node)
                        }
                    }
                }
        }
        DeadCodeElimination.run(method)
    }
}