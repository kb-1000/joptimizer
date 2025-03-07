package de.kb1000.joptimizer.optimizations.lowlevel

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.node.lowlevel.VarNode
import de.kb1000.joptimizer.optimizations.Pass

class DropUnusedVars : Pass {
    override fun run(classPool: ClassPool, clazz: Class) {
        for (method in clazz.methods) {
            val usedVars = mutableListOf<Boolean>()
            for (block in method) {
                for (node in block) {
                    if (node is VarNode) {
                        for (i in usedVars.size..node.i) {
                            usedVars.add(false)
                        }
                        if (node.isUse) {
                            // TODO: handle double/long, shouldn't matter right now though
                            usedVars[node.i] = true
                        }
                    }
                }
            }

            for (block in method) {
                for (node in block) {
                    if (node is VarNode && !usedVars[node.i]) {
                        node.dropVarNode(block)
                    }
                }
            }
        }
    }
}