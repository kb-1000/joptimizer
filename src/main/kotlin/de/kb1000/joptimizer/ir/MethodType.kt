package de.kb1000.joptimizer.ir

data class MethodType(var argumentTypes: List<Type>, var returnType: Type) {
    override fun toString() =
        "(${argumentTypes.joinToString("", transform = Type::descriptor)})${returnType.descriptor}"
}
