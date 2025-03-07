package de.kb1000.joptimizer.ir

enum class PrimitiveType(val descriptorChar: Char, val hasValue: Boolean, override val humanName: String,
                         override val length: Int) : Type {
    VOID('V', false, "void", 0),
    BOOLEAN('Z', true, "boolean", 1),
    BYTE('B', true, "byte", 1),
    CHAR('C', true, "char", 1),
    SHORT('S', true, "short", 1),
    INT('I', true, "int", 1),
    LONG('J', true, "long", 2),
    FLOAT('F', true, "float", 1),
    DOUBLE('D', true, "double", 2);

    override val descriptor get() = "$descriptorChar"
}
