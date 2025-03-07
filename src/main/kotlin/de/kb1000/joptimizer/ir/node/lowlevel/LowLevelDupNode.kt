package de.kb1000.joptimizer.ir.node.lowlevel

class LowLevelDupNode(var layout: DupLayout) : LowLevelNode() {
    enum class DupLayout(private val text: String) {
        DUP("\$stack0 \$stack0"),
        DUP_X1("\$stack0 \$stack1 \$stack0"),
        DUP_X2("\$stack0 \$stack1 \$stack2 \$stack0"),
        DUP2("\$stack0 \$stack1 \$stack0 \$stack1"),
        DUP2_X1("\$stack0 \$stack1 \$stack2 \$stack0 \$stack1"),
        DUP2_X2("\$stack0 \$stack1 \$stack2 \$stack3 \$stack0 \$stack1"),
        SWAP("\$stack1 \$stack0"), // technically, this is not a "duplication" operation, but it still fits here
        ;

        override fun toString() = text
    }

    override fun toString() = "ll_dup $layout"
}
