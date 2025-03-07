package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Block
import de.kb1000.joptimizer.ir.node.TerminalNode

class LowLevelSwitchNode(var default: Block, var blocks: MutableMap<Int, Block>) : LowLevelNode(), TerminalNode {
    override fun toString() = "${javaClass.name}@${hashCode().toString(16)}" // TODO
}