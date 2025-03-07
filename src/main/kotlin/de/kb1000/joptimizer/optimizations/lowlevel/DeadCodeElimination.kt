package de.kb1000.joptimizer.optimizations.lowlevel

import de.kb1000.joptimizer.ir.Block
import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.TryCatch
import de.kb1000.joptimizer.ir.node.TerminalNode
import de.kb1000.joptimizer.ir.node.lowlevel.JumpNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelConditionalJumpNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelSwitchNode
import de.kb1000.joptimizer.optimizations.Pass

class DeadCodeElimination : Pass {
    override fun run(classPool: ClassPool, clazz: Class) {
        for (method in clazz.methods) {
            if (!method.hasCode) continue
            // Eliminate all nodes in a block after a terminal node
            for (block in method) {
                var terminalNodeReached = false
                for (node in block) {
                    if (terminalNodeReached) {
                        block.remove(node)
                    }
                    if (node is TerminalNode) {
                        terminalNodeReached = true
                    }
                }
            }

            // Find and remove unused blocks
            val reachedBlocks = mutableSetOf<Block>()
            val reachedCatchBlocks = mutableSetOf<TryCatch>()
            fun visit(block: Block) {
                // TODO: handle catch handler blocks
                if (block in reachedBlocks) return
                reachedBlocks.add(block)
                for (catchBlock in method.catchBlocks) {
                    if (method.tryCatchContains(catchBlock, block)) {
                        reachedCatchBlocks.add(catchBlock)
                        visit(catchBlock.handlerBlock)
                    }
                }
                when (val lastNode = block.lastNode) {
                    // Since all nodes following a terminal node have been eliminated earlier, the last node not being
                    // a terminal node means there is none
                    !is TerminalNode -> throw IllegalStateException("No terminal node in a block!")

                    is JumpNode -> {
                        visit(lastNode.block)
                    }
                    is LowLevelConditionalJumpNode -> {
                        visit(lastNode.thenBlock)
                        visit(lastNode.elseBlock)
                    }
                    is LowLevelSwitchNode -> {
                        for (caseBlock in lastNode.blocks.values) {
                            visit(caseBlock)
                        }
                        visit(lastNode.default)
                    }
                }
            }
            visit(method.first())
            // the method's block list is modified here, so copy it first
            for (block in method.toList()) {
                if (block !in reachedBlocks) {
                    method.remove(block)
                    println("${clazz.internalName}->${method.name}${method.methodType}")
                }
            }

            for (catchBlock in method.catchBlocks.toTypedArray()) {
                if (catchBlock !in reachedCatchBlocks) {
                    method.catchBlocks.remove(catchBlock)
                }
            }
        }
    }
}