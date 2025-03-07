package de.kb1000.joptimizer.ir.node.constant

class StringConstant(val value: String) : Constant {
    override fun toString() = "string \"$value\""

    override fun equals(other: Any?): Boolean {
        return other === this || (other is StringConstant && other.value == value)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
