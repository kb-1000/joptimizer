package de.kb1000.joptimizer.optimizations.lowlevel

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelCastNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelNullConstNode
import de.kb1000.joptimizer.optimizations.Pass

class DropNullCasts : Pass {
    override fun run(classPool: ClassPool, clazz: Class) {
        for (method in clazz.methods) {
            for (block in method) {
                for (node in block) {
                    if (node is LowLevelCastNode && node.prevNode is LowLevelNullConstNode) {
                        // FIXME: this can remove side effects, since this may load a class (which may also be non-present)
                        //        Maybe add a ll_class (and ll_pop) node?
                        block.remove(node)
                    }
                }
            }
        }
    }
}