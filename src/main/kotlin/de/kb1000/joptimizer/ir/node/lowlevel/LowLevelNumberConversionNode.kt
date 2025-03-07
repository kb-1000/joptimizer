package de.kb1000.joptimizer.ir.node.lowlevel

class LowLevelNumberConversionNode(var type: ConversionType) : LowLevelNode() {
    enum class ConversionType(private val text: String) {
        INT_TO_LONG("int to long"),
        INT_TO_FLOAT("int to float"),
        INT_TO_DOUBLE("int to double"),
        LONG_TO_INT("long to int"),
        LONG_TO_FLOAT("long to float"),
        LONG_TO_DOUBLE("long to double"),
        FLOAT_TO_INT("float to int"),
        FLOAT_TO_LONG("float to long"),
        FLOAT_TO_DOUBLE("float to double"),
        DOUBLE_TO_INT("double to int"),
        DOUBLE_TO_LONG("double to long"),
        DOUBLE_TO_FLOAT("double to float"),
        INT_TO_BYTE("int to byte"),
        INT_TO_CHAR("int to char"),
        INT_TO_SHORT("int to short"),
        ;

        override fun toString() = text
    }

    override fun toString() = "ll_number_convert $type"
}
