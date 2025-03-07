package de.kb1000.joptimizer.ir.node

import de.kb1000.joptimizer.ir.Block
import java.lang.ref.WeakReference

interface ValueNode : Value {
    val nodeIndex: Int
    val parentBlock: WeakReference<Block>?

    fun uses(): Iterator<Value>
    override fun valueToString() = if (parentBlock?.get() !== null) "@$nodeIndex" else this.toString()
}