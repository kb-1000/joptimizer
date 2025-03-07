package de.kb1000.joptimizer.optimizations.lowlevel

import de.kb1000.joptimizer.ir.Block
import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.TryCatch
import de.kb1000.joptimizer.ir.node.lowlevel.*
import de.kb1000.joptimizer.optimizations.Pass
import java.util.*

class SimplifyControlFlowGraph : Pass {
    private fun getTarget(block: Block): Block {
        @Suppress("NAME_SHADOWING") var block = block
        val reachedBlocks = mutableSetOf(block)
        do {
            val firstNode = block.first()
            if (firstNode is JumpNode) {
                block = firstNode.block
                reachedBlocks.add(block)
            }
            // prevent infinite loops
            if (block in reachedBlocks) return block
        } while (firstNode is JumpNode)
        return block
    }

    override fun run(classPool: ClassPool, clazz: Class) {
        for (method in clazz.methods) {
            if (!method.hasCode) continue
            for (block in method.toList()) {
                when (val node = block.lastNode!!) {
                    is LowLevelSwitchNode -> {
                        val prevNode = node.prevNode
                        if (prevNode is LowLevelIntConstNode) {
                            block.set(
                                node,
                                JumpNode(node.blocks[prevNode.value] ?: node.default).apply { line = node.line })
                        }
                    }
                    is LowLevelConditionalJumpNode -> {
                        val prevNode = node.prevNode
                        when (prevNode) {
                            is LowLevelNullConstNode -> {
                                when (node.type) {
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
            val catchBlockStack = mutableListOf<TryCatch>()
            for (block in method) {
                for (catchBlock in method.catchBlocks.toList()) {
                    if (catchBlock.startBlock === block) {
                        catchBlockStack.add(catchBlock)
                    }
                    if (catchBlock.endBlock === block) {
                        catchBlockStack.remove(catchBlock)
                    }
                }
                for (node in block) {
                    if (node is LowLevelThrowNode) {
                        for (catchBlock in catchBlockStack) {
                            if (catchBlock.matches(null) == true) {
                                block.set(node, JumpNode(catchBlock.endBlock).apply { line = node.line })
                            }
                        }
                    }
                }
            }
            catchBlockStack.clear()
            val catchBlockHasThrowingNodes = IdentityHashMap<TryCatch, Boolean>()
            for (block in method) {
                for (catchBlock in method.catchBlocks.toList()) {
                    if (catchBlock.startBlock === block) {
                        catchBlockStack.add(catchBlock)
                    }
                    if (catchBlock.endBlock === block) {
                        if (catchBlockHasThrowingNodes[catchBlock] != true) {
                            method.catchBlocks.remove(catchBlock)
                        }
                        catchBlockStack.remove(catchBlock)
                    }
                }
                for (node in block) {
                    if (node.nodeInfo.canThrow) {
                        for (catchBlock in catchBlockStack) {
                            catchBlockHasThrowingNodes[catchBlock] = true
                        }
                    }
                }
            }
            for (block in method.toList()) {
                when (val node = block.lastNode!!) {
                    is JumpNode -> {
                        node.block = getTarget(node.block)
                    }
                    is LowLevelConditionalJumpNode -> {
                        node.thenBlock = getTarget(node.thenBlock)
                        node.elseBlock = getTarget(node.elseBlock)
                    }
                }
            }
            for (catchBlock in method.catchBlocks) {
                catchBlock.handlerBlock = getTarget(catchBlock.handlerBlock)
            }
        }
    }

}