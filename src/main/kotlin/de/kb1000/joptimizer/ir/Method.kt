package de.kb1000.joptimizer.ir

import de.kb1000.joptimizer.ir.node.Value
import de.kb1000.joptimizer.ir.node.constant.Constant
import java.lang.ref.WeakReference

class Method(var name: String, var methodType: MethodType) : Iterable<Block> {
    private val blocks: MutableList<Block> = mutableListOf()
    var catchBlocks: MutableList<TryCatch> = mutableListOf()
    var isAbstract = false
    var isNative = false
    var isStatic = false
    val hasCode
        get() = !isAbstract && !isNative

    private var isDirty = false

    // good old fake enum
    sealed interface Type {
        object CONSTRUCTOR : Type
        object METHOD : Type

        @Suppress("ClassName")
        object STATIC_CONSTRUCTOR : Type
    }

    fun addBlock(block: Block) {
        block.method = WeakReference(this)
        blocks.add(block)
        numberBlocks()
        markDirty()
    }

    private fun numberBlocks() {
        if (isDirty) {
            blocks.forEachIndexed { index, block -> block.i = index }
            blocks.flatten().forEachIndexed {index, node -> node.nodeIndex = index}
        }
        isDirty = false
    }

    override fun toString(): String {
        numberBlocks()
        return "    method $name$methodType {\n${
            blocks.joinToString(
                separator = "\n",
                postfix = "\n"
            )
        }${
            if (catchBlocks.isNotEmpty()) ("\n${
                catchBlocks.joinToString(
                    separator = "\n",
                    postfix = "\n"
                ) { "        $it" }
            }") else ""
        }    }"
    }

    override fun iterator(): Iterator<Block> = blocks.iterator()

    internal fun removeLastBlock() {
        if (blocks.isNotEmpty()) {
            val block = blocks.removeAt(blocks.size - 1)
            block.method = null
            markDirty()
        }
    }

    /**
     * Can return false when the blocks are not in the method
     */
    fun tryCatchContains(tryCatch: TryCatch, block: Block): Boolean {
        if (tryCatch.startBlock === block) return true
        if (tryCatch.endBlock === block) return false
        var reachedStart = false
        for (currentBlock in blocks) {
            if (reachedStart) {
                if (currentBlock === tryCatch.endBlock) return false
                if (currentBlock === block) return true
            } else {
                if (currentBlock === tryCatch.startBlock)
                    reachedStart = true
            }
        }
        return false
    }

    /**
     * Remove a block from this method.
     *
     * This has the side effect of removing all empty try/catch blocks, and all try/catch blocks that have this block as
     * the start block if this is the last block
     */
    fun remove(block: Block) {
        val idx = blocks.indexOf(block)
        var addStubBlock = false
        if (idx < 0) {
            throw IllegalStateException("Block not found!")
        }
        // TODO: does this properly update startBlock/endBlock if this block is the startBlock/endBlock?
        val it = catchBlocks.iterator()
        while (it.hasNext()) {
            val catchBlock = it.next()
            if (catchBlock.startBlock === catchBlock.endBlock || (catchBlock.startBlock === block && idx == blocks.lastIndex)) {
                it.remove()
            } else if (catchBlock.endBlock === block && idx == blocks.lastIndex) {
                // TODO: maybe make endBlock nullable to avoid this being necessary?
                addStubBlock = true
            }
        }

        if (addStubBlock) {
            val stubBlock = Block.constructStubBlock()
            addBlock(stubBlock)
            for (catchBlock in catchBlocks) {
                if (catchBlock.endBlock === block) {
                    catchBlock.endBlock = stubBlock
                }
            }
        }

        blocks.removeAt(idx)
        markDirty()
    }

    fun replaceUses(oldValue: Value, with: Value) {
        if (oldValue is Constant) throw IllegalArgumentException("Can't replace uses of constant")

        for (block in this)
            for (node in block)
                for (i in 0 until node.useCount())
                    if (node.getUse(i) === oldValue)
                        node.setUse(i, with)
    }

    fun isEmpty() = blocks.isEmpty()

    fun first() = blocks.first()

    internal fun markDirty() {
        isDirty = true
    }
}
