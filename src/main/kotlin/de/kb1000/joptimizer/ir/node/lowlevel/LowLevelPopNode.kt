package de.kb1000.joptimizer.ir.node.lowlevel

class LowLevelPopNode(var count: Int) : LowLevelNode() {
    override fun toString() = "ll_pop $count"
}