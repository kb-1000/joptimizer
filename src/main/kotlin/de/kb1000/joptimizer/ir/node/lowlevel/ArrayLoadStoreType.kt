package de.kb1000.joptimizer.ir.node.lowlevel

enum class ArrayLoadStoreType(val type: String) {
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),

    /**
     * Byte or boolean
     */
    BYTE("byte"),
    CHAR("char"),
    SHORT("short"),
    OBJECT("object"),
    ;

    override fun toString() = type
}