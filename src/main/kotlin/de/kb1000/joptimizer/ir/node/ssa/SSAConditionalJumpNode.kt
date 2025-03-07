package de.kb1000.joptimizer.ir.node.ssa

import de.kb1000.joptimizer.ir.Block
import de.kb1000.joptimizer.ir.node.TerminalNode
import de.kb1000.joptimizer.ir.node.Value

class SSAConditionalJumpNode(
    var op1: Value,
    var op2: Value,
    var thenBlock: Block,
    var elseBlock: Block,
    var type: ComparisonType
) :
    SSANode(),
    TerminalNode {
    /**
     * If the comparison returns true, then the jump goes to thenBlock, else to elseBlock
     */
    enum class ComparisonType(val type: String, val op: String) {
        INTEGER_EQUAL("int", "=="),
        INTEGER_NOT_EQUAL("int", "!="),
        INTEGER_LESS_THAN("int", ">"),
        INTEGER_GREATER_OR_EQUAL("int", "<="),
        INTEGER_GREATER_THAN("int", "<"),
        INTEGER_LESS_OR_EQUAL("int", ">="),
        OBJECT_EQUAL("object", "=="),
        OBJECT_NOT_EQUAL("object", "!="),
        ;
    }

    override fun toString() =
        "ssa_if ${type.type} ${op1.valueToString()} ${type.op} ${op2.valueToString()} then &${thenBlock.i} else &${elseBlock.i}"

    override fun getUse(int: Int) = when (int) {
        0 -> op1
        1 -> op2
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun setUse(int: Int, value: Value) = when (int) {
        0 -> op1 = value
        1 -> op2 = value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun useCount() = 2
}
class SSAOneArgConditionalJumpNode(
    var value: Value,
    var thenBlock: Block,
    var elseBlock: Block,
    var type: ComparisonType
) :
    SSANode(),
    TerminalNode {
    /**
     * If the comparison returns true, then the jump goes to thenBlock, else to elseBlock
     */
    enum class ComparisonType(val type: String, val op: String) {
        INTEGER_EQUAL_0("int", "== 0"),
        INTEGER_NOT_EQUAL_0("int", "!= 0"),
        INTEGER_LESS_THAN_0("int", "< 0"),
        INTEGER_GREATER_OR_EQUAL_0("int", ">= 0"),
        INTEGER_GREATER_THAN_0("int", "> 0"),
        INTEGER_LESS_OR_EQUAL_0("int", "<= 0"),
        OBJECT_NULL("object", "== null"),
        OBJECT_NOT_NULL("object", "!= null"),
        ;
    }

    override fun toString() =
        "ssa_if ${type.type} ${value.valueToString()} ${type.op} then &${thenBlock.i} else &${elseBlock.i}"

    override fun getUse(int: Int) = when (int) {
        0 -> value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun setUse(int: Int, value: Value) = when (int) {
        0 -> this.value = value
        else -> throw IndexOutOfBoundsException(int)
    }

    override fun useCount() = 1
}