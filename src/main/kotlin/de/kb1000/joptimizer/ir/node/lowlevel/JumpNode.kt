package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Block
import de.kb1000.joptimizer.ir.node.TerminalNode

/**
 * An unconditional jump to [block]
 */
class JumpNode(var block: Block) : LowLevelNode(), TerminalNode, GenericNode {
    override fun toString(): String {
        return "jump &${block.i}"
    }
}
