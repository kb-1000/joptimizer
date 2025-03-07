package de.kb1000.joptimizer.optimizations.lowlevel

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelNoSideEffectsConstNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelPopNode
import de.kb1000.joptimizer.optimizations.Pass

class DropUnusedConstants : Pass {
    override fun run(classPool: ClassPool, clazz: Class) {
        for (method in clazz.methods) {
            for (block in method) {
                for (node in block) {
                    if (node is LowLevelPopNode) {
                        val prevNode = node.prevNode
                        if (prevNode is LowLevelNoSideEffectsConstNode) {
                            block.remove(prevNode)
                            node.count -= prevNode.size
                            if (node.count == 0) {
                                block.remove(node)
                            } else if (node.count < 0) {
                                throw IllegalStateException()
                            }
                        }
                    }
                }
            }
        }
    }
}