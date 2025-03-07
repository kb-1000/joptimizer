package de.kb1000.joptimizer.ir.node.lowlevel

enum class VarType(private val text: String, val size: Int) {
    INTEGER("integer", 1),
    LONG("long", 2),
    FLOAT("float", 1),
    DOUBLE("double", 2),
    OBJECT("object", 1),
    ;

    override fun toString() = text
}
