package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.node.constant.Constant

interface LowLevelConstNode {
    val size: Int
    fun asConstant(): Constant
}