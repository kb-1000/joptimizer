package de.kb1000.joptimizer.ir.parser.asm

import de.kb1000.joptimizer.ir.*
import de.kb1000.joptimizer.ir.node.Node
import de.kb1000.joptimizer.ir.node.TerminalNode
import de.kb1000.joptimizer.ir.node.constant.MethodHandleConstant
import de.kb1000.joptimizer.ir.node.lowlevel.*
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelConditionalJumpNode.ComparisonType
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelDupNode.DupLayout
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelFieldNode.FieldNodeType
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelInvokeNode.InvokeType
import de.kb1000.joptimizer.ir.node.lowlevel.LowLevelMonitorNode.MonitorNodeType
import de.kb1000.joptimizer.util.computeIfAbsent
import mu.KLogging
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.JSRInlinerAdapter
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodNode

class ASMClassReader(private val classPool: ClassPool) : ClassVisitor(ASM9) {
    companion object : KLogging()

    private lateinit var clazz: Class

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        clazz = classPool.findClass(name, create = Class.ClassSource.APPLICATION)
        clazz.initSuperClass(classPool.findClass(superName!!), interfaces?.map(classPool::findClass) ?: listOf())
    }

    override fun visitSource(sourceFile: String?, smap: String?) {
        clazz.sourceFile = sourceFile
        if (smap != null && !smap.startsWith("SMAP")) {
            logger.warn("Warning: extra debug info is not in SMAP format")
        }
        // TODO: parse smap
    }

    override fun visitEnd() {
        clazz.isLoaded = true
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodNode = object : MethodNode(ASM9, access, name, descriptor, signature, exceptions) {
            override fun getLabelNode(label: Label): LabelNode {
                if (label.info !is LabelNode) {
                    label.info = object : LabelNode(label) {
                        override fun resetLabel() {
                        }
                    }
                }
                return label.info as LabelNode
            }
        }
        return JSRInlinerAdapter(object : MethodVisitor(ASM9, methodNode) {
            override fun visitEnd() {
                super.visitEnd()
                // These maps use identity comparison, which is correct for Label
                val labelBlocks = mutableMapOf<Label, Block>()
                val labelLines = mutableMapOf<Label, Int>()
                methodNode.accept(object : MethodVisitor(ASM9) {
                    override fun visitJumpInsn(opcode: Int, label: Label) {
                        labelBlocks.computeIfAbsent(label, ::Block)
                    }

                    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label) {
                        labelBlocks.computeIfAbsent(dflt, ::Block)
                        for (label in labels) {
                            labelBlocks.computeIfAbsent(label, ::Block)
                        }
                    }

                    override fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<Label>) {
                        labelBlocks.computeIfAbsent(dflt, ::Block)
                        for (label in labels) {
                            labelBlocks.computeIfAbsent(label, ::Block)
                        }
                    }

                    override fun visitLineNumber(line: Int, start: Label) {
                        labelLines[start] = line
                    }

                    override fun visitTryCatchBlock(start: Label, end: Label, handler: Label, type: String?) {
                        labelBlocks.computeIfAbsent(start, ::Block)
                        labelBlocks.computeIfAbsent(end, ::Block)
                        labelBlocks.computeIfAbsent(handler, ::Block)
                    }
                })
                methodNode.accept(object : MethodVisitor(ASM9) {
                    private val method = Method(name, convertMethodType(classPool, descriptor)).apply {
                        isAbstract = (access and ACC_ABSTRACT) != 0
                        isNative = (access and ACC_NATIVE) != 0
                        isStatic = (access and ACC_STATIC) != 0
                    }

                    private var lineNumber = -1

                    private var lastNode: Node? = null
                    private var lastLabelBlock: Block? = null

                    private var block: Block? = null
                        set(value) {
                            val lastNode = field?.lastNode
                            if (lastNode != null && lastNode !is TerminalNode) {
                                logger.warn("Missing terminal node!")
                                emitNode(LowLevelNullConstNode())
                                emitNode(LowLevelThrowNode())
                            }
                            method.addBlock(value!!)
                            field = value
                        }

                    override fun visitLabel(label: Label) {
                        val line = labelLines[label]
                        if (line != null) {
                            lineNumber = line
                        }

                        val lastNode = lastNode
                        val labelBlock = labelBlocks[label]
                        if (labelBlock != null) {
                            if (lastNode != null && lastNode !is TerminalNode) {
                                emitNode(JumpNode(labelBlock))
                            }
                            if (lastNode is LowLevelConditionalJumpNode) {
                                lastNode.elseBlock = labelBlock
                            }
                            val block = this.block
                            if (block != null)
                                if (block === lastLabelBlock) {
                                    emitNode(JumpNode(labelBlock))
                                } else if (block.firstNode == null) {
                                    // empty block
                                    method.removeLastBlock()
                                }
                            this.lastLabelBlock = labelBlock
                            this.block = labelBlock
                        }
                    }

                    private fun emitNode(node: LowLevelNode) {
                        lastNode = node
                        node.line = lineNumber
                        val block = getBlock()
                        block.add(node)
                    }

                    @JvmName("getBlock2")
                    private fun getBlock() = block ?: Block().also { block = it }

                    override fun visitIntInsn(opcode: Int, operand: Int) {
                        emitNode(
                            when (opcode) {
                                BIPUSH, SIPUSH -> LowLevelIntConstNode(operand)
                                NEWARRAY -> LowLevelNewArrayNode(
                                    when (operand) {
                                        T_BOOLEAN -> PrimitiveType.BOOLEAN
                                        T_CHAR -> PrimitiveType.CHAR
                                        T_FLOAT -> PrimitiveType.FLOAT
                                        T_DOUBLE -> PrimitiveType.DOUBLE
                                        T_BYTE -> PrimitiveType.BYTE
                                        T_SHORT -> PrimitiveType.SHORT
                                        T_INT -> PrimitiveType.INT
                                        T_LONG -> PrimitiveType.LONG
                                        else -> throw IllegalArgumentException("Unexpected NEWARRAY operand $operand")
                                    }
                                )
                                else -> throw IllegalArgumentException("Unexpected visitIntInsn opcode $opcode")
                            }
                        )
                    }

                    override fun visitLdcInsn(value: Any) {
                        emitNode(
                            when (value) {
                                is Int -> LowLevelIntConstNode(value)
                                is Float -> LowLevelFloatConstNode(value)
                                is Long -> LowLevelLongConstNode(value)
                                is Double -> LowLevelDoubleConstNode(value)
                                is String -> LowLevelStringConstNode(value)
                                is Type -> {
                                    when (value.sort) {
                                        Type.ARRAY, Type.OBJECT -> LowLevelClassConstNode(
                                            convertType(
                                                classPool,
                                                value
                                            ) as ObjectType
                                        )
                                        else -> TODO()
                                    }
                                }
                                is Handle -> TODO()
                                is ConstantDynamic -> TODO()
                                else -> throw IllegalArgumentException("Unexpected visitLdcInsn argument $value")
                            }
                        )
                    }

                    override fun visitInsn(opcode: Int) {
                        emitNode(
                            when (opcode) {
                                NOP -> null
                                ACONST_NULL -> LowLevelNullConstNode()
                                ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5 -> LowLevelIntConstNode(
                                    opcode - ICONST_0
                                )
                                LCONST_0 -> LowLevelLongConstNode(0)
                                LCONST_1 -> LowLevelLongConstNode(1)
                                FCONST_0, FCONST_1, FCONST_2 -> LowLevelFloatConstNode((opcode - FCONST_0).toFloat())
                                DCONST_0 -> LowLevelDoubleConstNode(0.0)
                                DCONST_1 -> LowLevelDoubleConstNode(1.0)
                                IALOAD -> LowLevelArrayLoadNode(ArrayLoadStoreType.INT)
                                LALOAD -> LowLevelArrayLoadNode(ArrayLoadStoreType.LONG)
                                FALOAD -> LowLevelArrayLoadNode(ArrayLoadStoreType.FLOAT)
                                DALOAD -> LowLevelArrayLoadNode(ArrayLoadStoreType.DOUBLE)
                                AALOAD -> LowLevelArrayLoadNode(ArrayLoadStoreType.OBJECT)
                                BALOAD -> LowLevelArrayLoadNode(ArrayLoadStoreType.BYTE)
                                CALOAD -> LowLevelArrayLoadNode(ArrayLoadStoreType.CHAR)
                                SALOAD -> LowLevelArrayLoadNode(ArrayLoadStoreType.SHORT)
                                IASTORE -> LowLevelArrayStoreNode(ArrayLoadStoreType.INT)
                                LASTORE -> LowLevelArrayStoreNode(ArrayLoadStoreType.LONG)
                                FASTORE -> LowLevelArrayStoreNode(ArrayLoadStoreType.FLOAT)
                                DASTORE -> LowLevelArrayStoreNode(ArrayLoadStoreType.DOUBLE)
                                AASTORE -> LowLevelArrayStoreNode(ArrayLoadStoreType.OBJECT)
                                BASTORE -> LowLevelArrayStoreNode(ArrayLoadStoreType.BYTE)
                                CASTORE -> LowLevelArrayStoreNode(ArrayLoadStoreType.CHAR)
                                SASTORE -> LowLevelArrayStoreNode(ArrayLoadStoreType.SHORT)
                                POP -> LowLevelPopNode(1)
                                POP2 -> LowLevelPopNode(2)
                                DUP -> LowLevelDupNode(DupLayout.DUP)
                                DUP_X1 -> LowLevelDupNode(DupLayout.DUP_X1)
                                DUP_X2 -> LowLevelDupNode(DupLayout.DUP_X2)
                                DUP2 -> LowLevelDupNode(DupLayout.DUP2)
                                DUP2_X1 -> LowLevelDupNode(DupLayout.DUP2_X1)
                                DUP2_X2 -> LowLevelDupNode(DupLayout.DUP2_X2)
                                SWAP -> LowLevelDupNode(DupLayout.SWAP)
                                IADD -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_ADD)
                                LADD -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_ADD)
                                FADD -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.FLOAT_ADD)
                                DADD -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.DOUBLE_ADD)
                                ISUB -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_SUBTRACT)
                                LSUB -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_SUBTRACT)
                                FSUB -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.FLOAT_SUBTRACT)
                                DSUB -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.DOUBLE_SUBTRACT)
                                IMUL -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_MULTIPLY)
                                LMUL -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_MULTIPLY)
                                FMUL -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.FLOAT_MULTIPLY)
                                DMUL -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.DOUBLE_MULTIPLY)
                                IDIV -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_DIVIDE)
                                LDIV -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_DIVIDE)
                                FDIV -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.FLOAT_DIVIDE)
                                DDIV -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.DOUBLE_DIVIDE)
                                IREM -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_MODULO)
                                LREM -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_MODULO)
                                FREM -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.FLOAT_MODULO)
                                DREM -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.DOUBLE_MODULO)
                                INEG -> LowLevelUnaryOpNode(LowLevelUnaryOpNode.Operation.INTEGER_NEGATE)
                                LNEG -> LowLevelUnaryOpNode(LowLevelUnaryOpNode.Operation.LONG_NEGATE)
                                FNEG -> LowLevelUnaryOpNode(LowLevelUnaryOpNode.Operation.FLOAT_NEGATE)
                                DNEG -> LowLevelUnaryOpNode(LowLevelUnaryOpNode.Operation.DOUBLE_NEGATE)
                                ISHL -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_LEFT_SHIFT)
                                LSHL -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_LEFT_SHIFT)
                                ISHR -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_RIGHT_SHIFT)
                                LSHR -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_RIGHT_SHIFT)
                                IUSHR -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_UNSIGNED_RIGHT_SHIFT)
                                LUSHR -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_UNSIGNED_RIGHT_SHIFT)
                                IAND -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_AND)
                                LAND -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_AND)
                                IOR -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_OR)
                                LOR -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_OR)
                                IXOR -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.INTEGER_XOR)
                                LXOR -> LowLevelBinOpNode(LowLevelBinOpNode.Operation.LONG_XOR)
                                I2L -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.INT_TO_LONG)
                                I2F -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.INT_TO_FLOAT)
                                I2D -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.INT_TO_DOUBLE)
                                L2I -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.LONG_TO_INT)
                                L2F -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.LONG_TO_FLOAT)
                                L2D -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.LONG_TO_DOUBLE)
                                F2I -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.FLOAT_TO_INT)
                                F2L -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.FLOAT_TO_LONG)
                                F2D -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.FLOAT_TO_DOUBLE)
                                D2I -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.DOUBLE_TO_INT)
                                D2L -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.DOUBLE_TO_LONG)
                                D2F -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.DOUBLE_TO_FLOAT)
                                I2B -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.INT_TO_BYTE)
                                I2C -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.INT_TO_CHAR)
                                I2S -> LowLevelNumberConversionNode(LowLevelNumberConversionNode.ConversionType.INT_TO_SHORT)
                                LCMP -> LowLevelCompareNode(LowLevelCompareNode.CompareType.LONG)
                                FCMPL -> LowLevelCompareNode(LowLevelCompareNode.CompareType.FLOAT_LT)
                                FCMPG -> LowLevelCompareNode(LowLevelCompareNode.CompareType.FLOAT_GT)
                                DCMPL -> LowLevelCompareNode(LowLevelCompareNode.CompareType.DOUBLE_LT)
                                DCMPG -> LowLevelCompareNode(LowLevelCompareNode.CompareType.DOUBLE_GT)
                                IRETURN -> LowLevelReturnNode(VarType.INTEGER)
                                LRETURN -> LowLevelReturnNode(VarType.LONG)
                                FRETURN -> LowLevelReturnNode(VarType.FLOAT)
                                DRETURN -> LowLevelReturnNode(VarType.DOUBLE)
                                ARETURN -> LowLevelReturnNode(VarType.OBJECT)
                                RETURN -> ReturnVoidNode()
                                ARRAYLENGTH -> LowLevelArrayLengthNode()
                                ATHROW -> LowLevelThrowNode()
                                MONITORENTER -> LowLevelMonitorNode(MonitorNodeType.ENTER)
                                MONITOREXIT -> LowLevelMonitorNode(MonitorNodeType.EXIT)
                                else -> throw IllegalArgumentException("Unexpected visitInsn opcode $opcode")
                            } ?: return
                        )
                    }

                    override fun visitJumpInsn(opcode: Int, label: Label) {
                        val block = labelBlocks[label]
                            ?: throw IllegalStateException("Label $label is not associated with a block, cannot jump to it")
                        emitNode(
                            when (opcode) {
                                GOTO -> JumpNode(block)
                                JSR -> throw IllegalArgumentException("JSR is not supported, JSRInlinerAdapter failed")
                                else -> {
                                    val node = when (opcode) {
                                        IFEQ -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_EQUAL_0
                                        )
                                        IFNE -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_NOT_EQUAL_0
                                        )
                                        IFLT -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_LESS_THAN_0
                                        )
                                        IFGE -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_GREATER_OR_EQUAL_0
                                        )
                                        IFGT -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_GREATER_THAN_0
                                        )
                                        IFLE -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_LESS_OR_EQUAL_0
                                        )
                                        IF_ICMPEQ -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_EQUAL
                                        )
                                        IF_ICMPNE -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_NOT_EQUAL
                                        )
                                        IF_ICMPLT -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_LESS_THAN
                                        )
                                        IF_ICMPGE -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_GREATER_OR_EQUAL
                                        )
                                        IF_ICMPGT -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_GREATER_THAN
                                        )
                                        IF_ICMPLE -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.INTEGER_LESS_OR_EQUAL
                                        )
                                        IF_ACMPEQ -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.OBJECT_EQUAL
                                        )
                                        IF_ACMPNE -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.OBJECT_NOT_EQUAL
                                        )
                                        IFNULL -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.OBJECT_NULL
                                        )
                                        IFNONNULL -> LowLevelConditionalJumpNode(
                                            block,
                                            block,
                                            ComparisonType.OBJECT_NOT_NULL
                                        )
                                        else -> throw IllegalArgumentException("Unexpected visitJumpInsn opcode $opcode")
                                    }
                                    emitNode(node)
                                    val newBlock = Block()
                                    node.elseBlock = newBlock
                                    this.block = newBlock
                                    return
                                }
                            }
                        )
                    }

                    override fun visitVarInsn(opcode: Int, `var`: Int) {
                        emitNode(
                            when (opcode) {
                                ILOAD -> LowLevelVarLoadNode(`var`, VarType.INTEGER)
                                LLOAD -> LowLevelVarLoadNode(`var`, VarType.LONG)
                                FLOAD -> LowLevelVarLoadNode(`var`, VarType.FLOAT)
                                DLOAD -> LowLevelVarLoadNode(`var`, VarType.DOUBLE)
                                ALOAD -> LowLevelVarLoadNode(`var`, VarType.OBJECT)
                                ISTORE -> LowLevelVarStoreNode(`var`, VarType.INTEGER)
                                LSTORE -> LowLevelVarStoreNode(`var`, VarType.LONG)
                                FSTORE -> LowLevelVarStoreNode(`var`, VarType.FLOAT)
                                DSTORE -> LowLevelVarStoreNode(`var`, VarType.DOUBLE)
                                ASTORE -> LowLevelVarStoreNode(`var`, VarType.OBJECT)
                                RET -> throw IllegalArgumentException("JSR is not supported, JSRInlinerAdapter failed")
                                else -> throw IllegalArgumentException("Unexpected visitVarInsn opcode $opcode")
                            }
                        )
                    }

                    override fun visitTypeInsn(opcode: Int, type: String) {
                        emitNode(
                            when (opcode) {
                                NEW -> LowLevelNewNode(classPool.findClass(type))
                                ANEWARRAY -> LowLevelNewArrayNode(convertType(classPool, Type.getObjectType(type)))
                                CHECKCAST -> LowLevelCastNode(
                                    convertType(
                                        classPool,
                                        Type.getObjectType(type)
                                    ) as ObjectType
                                )
                                INSTANCEOF -> LowLevelInstanceofNode(
                                    convertType(
                                        classPool,
                                        Type.getObjectType(type)
                                    ) as ObjectType
                                )
                                else -> throw IllegalArgumentException("Unexpected visitTypeInsn opcode $opcode")
                            }
                        )
                    }

                    override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
                        emitNode(
                            LowLevelFieldNode(
                                when (opcode) {
                                    GETSTATIC -> FieldNodeType.GET_STATIC
                                    PUTSTATIC -> FieldNodeType.PUT_STATIC
                                    GETFIELD -> FieldNodeType.GET_INSTANCE
                                    PUTFIELD -> FieldNodeType.PUT_INSTANCE
                                    else -> throw IllegalArgumentException("Unexpected visitFieldInsn opcode $opcode")
                                }, classPool.findClass(owner), name, convertType(classPool, Type.getType(descriptor))
                            )
                        )
                    }

                    override fun visitMethodInsn(
                        opcode: Int,
                        owner: String,
                        name: String,
                        descriptor: String,
                        isInterface: Boolean
                    ) {
                        emitNode(
                            LowLevelInvokeNode(
                                when (opcode) {
                                    INVOKEVIRTUAL -> InvokeType.VIRTUAL
                                    INVOKESPECIAL -> InvokeType.SPECIAL
                                    INVOKESTATIC -> InvokeType.STATIC
                                    INVOKEINTERFACE -> InvokeType.INTERFACE
                                    else -> throw IllegalArgumentException("Unexpected visitMethodInsn opcode $opcode")
                                },
                                convertType(classPool, Type.getObjectType(owner)) as ObjectType,
                                name,
                                convertMethodType(classPool, descriptor),
                                isInterface
                            )
                        )
                    }

                    override fun visitInvokeDynamicInsn(
                        name: String,
                        descriptor: String,
                        bootstrapMethodHandle: Handle,
                        vararg bootstrapMethodArguments: Any
                    ) {
                        emitNode(
                            LowLevelInvokeDynamicNode(
                                name,
                                convertMethodType(classPool, descriptor),
                                convertConstant(classPool, bootstrapMethodHandle) as MethodHandleConstant,
                                bootstrapMethodArguments.map { convertConstant(classPool, it) })
                        )
                    }

                    override fun visitIincInsn(`var`: Int, increment: Int) {
                        emitNode(LowLevelVarIncrementNode(`var`, increment))
                    }

                    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label) {
                        val blocks = mutableMapOf<Int, Block>()
                        for (i in labels.indices) {
                            val label = labels[i]
                            if (label !== dflt) {
                                blocks[i + min] = labelBlocks[label]!!
                            }
                        }
                        emitNode(LowLevelSwitchNode(labelBlocks[dflt]!!, blocks))
                    }

                    override fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<Label>) {
                        val blocks = mutableMapOf<Int, Block>()
                        for ((i, label) in keys.zip(labels)) {
                            if (label !== dflt) {
                                blocks[i] = labelBlocks[label]!!
                            }
                        }
                        emitNode(LowLevelSwitchNode(labelBlocks[dflt]!!, blocks))
                    }

                    override fun visitMultiANewArrayInsn(descriptor: String, numDimensions: Int) {
                        emitNode(
                            LowLevelMultiNewArrayNode(
                                convertType(classPool, Type.getType(descriptor)),
                                numDimensions
                            )
                        )
                    }

                    override fun visitTryCatchBlock(start: Label, end: Label, handler: Label, type: String?) {
                        method.catchBlocks.add(
                            TryCatch(
                                labelBlocks[start]!!,
                                labelBlocks[end]!!,
                                labelBlocks[handler]!!,
                                type?.let { classPool.findClass(it) })
                        )
                    }

                    override fun visitEnd() {
                        clazz.methods.add(method)
                    }
                })
            }
        }, access, name, descriptor, signature, exceptions)
    }

    val resultClass
        get() = clazz
}
