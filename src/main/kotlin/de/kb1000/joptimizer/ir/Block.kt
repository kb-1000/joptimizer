package de.kb1000.joptimizer.ir

import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelNullConstNode
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelThrowNode
import java.lang.ref.WeakReference

class Block : Iterable<Node> {
    @JvmField
    var firstNode: Node? = null

    @JvmField
    var lastNode: Node? = null

    @JvmField
    var method: WeakReference<Method>? = null

    @JvmField
    var i = -1

    fun add(node: Node) {
        val lastNode = lastNode
        node.nextNode = null
        if (lastNode == null) {
            node.prevNode = null
            firstNode = node
        } else {
            lastNode.nextNode = node
            node.prevNode = lastNode
        }
        this.lastNode = node
        node.parentBlock = WeakReference(this)
        method?.get()?.markDirty()
    }

    fun set(oldNode: Node, newNode: Node) {
        if (oldNode === firstNode) {
            firstNode = newNode
        }

        if (oldNode === lastNode) {
            lastNode = newNode
        }

        oldNode.prevNode?.nextNode = newNode
        oldNode.nextNode?.prevNode = newNode
        newNode.prevNode = oldNode.prevNode
        newNode.nextNode = oldNode.nextNode
        oldNode.prevNode = null
        oldNode.nextNode = null
        oldNode.parentBlock = null
        newNode.parentBlock = WeakReference(this)
        method?.get()?.markDirty()
    }

    fun insertBefore(node: Node, newNode: Node) {
        if (node === firstNode) {
            firstNode = newNode
        }
        node.prevNode?.nextNode = newNode
        newNode.prevNode = node.prevNode
        node.prevNode = newNode
        newNode.nextNode = node
        newNode.parentBlock = WeakReference(this)
        method?.get()?.markDirty()
    }

    fun remove(node: Node) {
        if (node === firstNode) {
            firstNode = node.nextNode
        }

        if (node === lastNode) {
            lastNode = node.prevNode
        }

        node.prevNode?.nextNode = node.nextNode
        node.nextNode?.prevNode = node.prevNode
        node.prevNode = null
        node.nextNode = null
        node.parentBlock = null
        method?.get()?.markDirty()
    }

    override fun toString(): String {
        return "${if (i >= 0) "        $i" else ""}:\n${this.joinToString(separator = "\n") { "            $it" }}"
    }

    override fun iterator() = BlockIterator()

    inner class BlockIterator : Iterator<Node> {
        private var currentNode = firstNode
        override fun hasNext() = currentNode != null

        override fun next(): Node {
            val node = currentNode ?: throw NoSuchElementException()
            currentNode = node.nextNode
            return node
        }
    }

    companion object {
        internal fun constructStubBlock(): Block {
            val block = Block()
            block.add(LowLevelNullConstNode())
            block.add(LowLevelThrowNode())
            return block
        }
    }
}
