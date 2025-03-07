package de.kb1000.joptimizer.optimizations.lowlevel

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelDupNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelDupNode.DupLayout
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelVarLoadNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelVarStoreNode
import de.kb1000.joptimizer.optimizations.Pass

class StoreLoadToDupStore : Pass {
    override fun run(classPool: ClassPool, clazz: Class) {
        for (method in clazz.methods) {
            for (block in method) {
                for (node in block) {
                    if (node is LowLevelVarLoadNode) {
                        val prevNode = node.prevNode
                        if (prevNode is LowLevelVarStoreNode) {
                            if (node.i == prevNode.i && node.type === prevNode.type) {
                                block.remove(node)
                                block.insertBefore(
                                    prevNode,
                                    (if (node.type.size == 1) LowLevelDupNode(DupLayout.DUP) else LowLevelDupNode(
                                        DupLayout.DUP2
                                    )).apply {
                                        line = node.line
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}