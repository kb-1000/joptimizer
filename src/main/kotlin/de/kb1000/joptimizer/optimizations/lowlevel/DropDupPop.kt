package de.kb1000.joptimizer.optimizations.lowlevel

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelDupNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelDupNode.DupLayout
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelPopNode
import de.kb1000.joptimizer.optimizations.Pass

class DropDupPop : Pass {
    override fun run(classPool: ClassPool, clazz: Class) {
        for (method in clazz.methods) {
            for (block in method) {
                for (node in block) {
                    if (node is LowLevelPopNode) {
                        val prevNode = node.prevNode
                        if (node.count >= 1 && prevNode is LowLevelDupNode && prevNode.layout == DupLayout.DUP) {
                            block.remove(prevNode)
                            node.count--
                        } else if (node.count >= 2 && prevNode is LowLevelDupNode && prevNode.layout == DupLayout.DUP2) {
                            block.remove(prevNode)
                            node.count -= 2
                        }
                        if (node.count == 0) {
                            block.remove(node)
                        }
                    }
                }
            }
        }
    }
}