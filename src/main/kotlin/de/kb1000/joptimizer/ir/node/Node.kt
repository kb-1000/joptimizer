package de.kb1000.joptimizer.ir.node

import de.kb1000.joptimizer.ir.Block
import java.lang.ref.WeakReference

abstract class Node internal constructor() {
    companion object {
        @JvmField
        val nodeInfo = NodeInfo()
    }

    @JvmField
    var line: Int = -1

    @JvmField
    var nextNode: Node? = null

    @JvmField
    var prevNode: Node? = null

    abstract override fun toString(): String

    open fun getUse(int: Int): Value = throw NotImplementedError()
    open fun setUse(int: Int, value: Value): Unit = throw NotImplementedError()
    open fun useCount() = 0

    fun uses() = object : Iterator<Value> {
        var i = 0

        override fun hasNext() = i < useCount()
        override fun next() = getUse(i++)
    }

    open val nodeInfo
        get() = Node.nodeInfo

    var nodeIndex: Int = -1
    var parentBlock: WeakReference<Block>? = null
}
