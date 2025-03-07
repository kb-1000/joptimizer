package de.kb1000.joptimizer.optimizations

import de.kb1000.joptimizer.ir.Class
import de.kb1000.joptimizer.ir.ClassPool

interface Pass {
    fun run(classPool: ClassPool, clazz: Class)
}