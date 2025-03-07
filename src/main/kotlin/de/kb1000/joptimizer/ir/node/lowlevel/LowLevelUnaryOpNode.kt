package de.kb1000.joptimizer.ir.node.lowlevel

class LowLevelUnaryOpNode(var operation: Operation) : LowLevelNode() {
    enum class Operation(val text: String) {
        INTEGER_NEGATE("integer -"), // TODO: this isn't a binop
        LONG_NEGATE("long -"), // TODO: this isn't a binop
        FLOAT_NEGATE("float -"), // TODO: this isn't a binop
        DOUBLE_NEGATE("double -"), // TODO: this isn't a binop
        ;

        override fun toString() = text
    }

    override fun toString() = "ll_unaryop $operation"
}