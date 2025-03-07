package de.kb1000.joptimizer.ir.writer.asm

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelNode
import de.kb1000.joptimizer.optimizations.Pass

internal fun preprocess(clazz: Class) {
    val passes = arrayOf<Pass>()

    preprocessClass@ while (true) {
        for (pass in passes) {
            pass.run(clazz.classPool, clazz)
        }

        for (method in clazz.methods) {
            for (block in method) {
                for (node in block) {
                    if (node !is LowLevelNode) {
                        continue@preprocessClass
                    }
                }
            }
        }
        break@preprocessClass
    }
}