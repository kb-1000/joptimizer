package de.kb1000.joptimizer.transformation.lowleveltossa

import de.kb1000.joptimizer.ir.Block
import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.Method
import de.kb1000.joptimizer.ir.node.TerminalNode
import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.lowlevel.*
import de.kb1000.joptimizer.ir.node.ssa.SSAConditionalJumpNode
import de.kb1000.joptimizer.ir.node.ssa.SSAOneArgConditionalJumpNode

private fun countLocals(method: Method): Int {
    var i = if (method.isStatic) 0 else 1 + method.methodType.argumentTypes.sumOf { it.length }
    for (block in method) {
        for (node in block) {
            if (node is VarNode) i = i.coerceAtLeast(node.i - 1 + node.type.size)
        }
    }
    return i
}

private inline fun enumerateTargets(block: Block, callback: (Block) -> Unit) {
    when (val node = block.lastNode as TerminalNode) {
        is JumpNode -> callback(node.block)
        is LowLevelConditionalJumpNode -> {
            callback(node.thenBlock)
            callback(node.elseBlock)
        }
        is SSAConditionalJumpNode -> {
            callback(node.thenBlock)
            callback(node.elseBlock)
        }
        is SSAOneArgConditionalJumpNode -> {
            callback(node.thenBlock)
            callback(node.elseBlock)
        }
        is LowLevelSwitchNode -> {
            callback(node.default)
            node.blocks.values.forEach(callback)
        }
    }
}

class LowLevelToSSA2 {
    private class TempValue(val kind: Kind, val index: Int, val block: Block, val varType: VarType) : Value {
        enum class Kind(val str: String) {
            STACK("stack"), REGISTER("register");

            override fun toString() = str
        }
        override fun toString(): String {
            return "phi of &${block.i} at $kind $index"
        }

        override fun valueToString() = "($this)"
    }
    private data class Frame(val stack: List<VarType>, val registers: List<VarType>)
    private class FrameVarType(var varType: VarType?) {
        var isUsed = false

        fun poison() {
            varType = null
        }
    }
    fun transform(pool: ClassPool, method: Method) {
        if (!method.hasCode) return
        val edges = mutableMapOf<Block, MutableList<Block>>()
        for (block in method) {
            enumerateTargets(block) {
                edges.getOrPut(it, ::mutableListOf).add(block)
            }
        }
        val localCount = countLocals(method)
        val startBlock = Block()
        edges[method.first()] = mutableListOf(startBlock)
        val todo = ArrayDeque<Block>()
        todo.addLast(method.first())
        val frames = hashMapOf<Block, Frame>()
        val processed = hashSetOf<Block>()
        while (!todo.isEmpty()) {
            val block = todo.removeFirst()
            if (block in processed) continue
            enumerateTargets(block) {
                todo.add(it)
            }
            processed.add(block)
        }
    }
}