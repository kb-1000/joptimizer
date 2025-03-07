package de.kb1000.joptimizer.ir.node.lowlevel

class LowLevelCompareNode(var type: CompareType) : LowLevelNode() {
    enum class CompareType(private val text: String) {
        // TODO: rename floating point comparison types to match NaN behavior more closely
        LONG("long"),
        FLOAT_LT("float_lt"),
        FLOAT_GT("float_gt"),
        DOUBLE_LT("double_lt"),
        DOUBLE_GT("double_gt"),
        ;

        override fun toString() = text
    }

    override fun toString() = "ll_compare $type"
}