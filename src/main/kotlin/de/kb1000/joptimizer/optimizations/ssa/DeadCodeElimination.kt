package de.kb1000.joptimizer.optimizations.ssa

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.Method
import de.kb1000.joptimizer.ir.node.ValueNode
import de.kb1000.joptimizer.optimizations.Pass

object DeadCodeElimination : Pass {
    override fun run(classPool: ClassPool, clazz: Class) {
        clazz.methods.forEach(::run)
    }

    fun run(method: Method) {
        val used = mutableSetOf<ValueNode>()
        var repeat = true
        for (block in method)
            for (node in block)
                if (node !is ValueNode || !node.isPure())
                    for (use in node.uses())
                        if (use is ValueNode)
                            used.add(use)
        var delta = mutableSetOf<ValueNode>()
        var delta2 = used.toMutableSet()
        while (repeat) {
            repeat = false
            for (node in delta2)
                for (use in Iterable(node::uses)) {
                    if (use is ValueNode && use !in used) {
                        delta.add(use)
                        used.add(use)
                    }
                }
            repeat = delta.isNotEmpty()
            delta2.clear()
            delta.let {
                delta = delta2
                delta2 = it
            }
        }

        for (block in method)
            for (node in block)
                if (node is ValueNode && node !in used && node.isPure())
                    block.remove(node)
    }
}