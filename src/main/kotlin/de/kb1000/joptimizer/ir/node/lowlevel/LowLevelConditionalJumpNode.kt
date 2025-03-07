package de.kb1000.joptimizer.ir.node.lowlevel

import de.kb1000.joptimizer.ir.Block
import de.kb1000.joptimizer.ir.node.TerminalNode

class LowLevelConditionalJumpNode(var thenBlock: Block, var elseBlock: Block, var type: ComparisonType) :
    LowLevelNode(),
    TerminalNode {
    /**
     * If the comparison returns true, then the jump goes to thenBlock, else to elseBlock
     */
    enum class ComparisonType(val type: String, private val text: String, val operand2: String? = null) {
        INTEGER_EQUAL_0("int", "==", "0"),
        INTEGER_NOT_EQUAL_0("int", "!=", "0"),
        INTEGER_LESS_THAN_0("int", "<", "0"),
        INTEGER_GREATER_OR_EQUAL_0("int", ">=", "0"),
        INTEGER_GREATER_THAN_0("int", ">", "0"),
        INTEGER_LESS_OR_EQUAL_0("int", "<=", "0"),
        INTEGER_EQUAL("int", "=="),
        INTEGER_NOT_EQUAL("int", "!="),
        INTEGER_LESS_THAN("int", "<"),
        INTEGER_GREATER_OR_EQUAL("int", ">="),
        INTEGER_GREATER_THAN("int", ">"),
        INTEGER_LESS_OR_EQUAL("int", "<="),
        OBJECT_EQUAL("object", "=="),
        OBJECT_NOT_EQUAL("object", "!="),
        OBJECT_NULL("object", "==", "null"),
        OBJECT_NOT_NULL("object", "!=", "null"),
        ;

        override fun toString() = if (operand2 != null) "$type \$stack0 $text $operand2" else "$type \$stack1 $text \$stack0"
    }

    override fun toString(): String {
        return "ll_if $type then &${thenBlock.i} else &${elseBlock.i}"
    }
}
