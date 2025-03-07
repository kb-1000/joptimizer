@file:JvmName("Main")

package de.kb1000.joptimizer

import de.kb1000.joptimizer.ir.ClassPool
import de.kb1000.joptimizer.ir.parser.asm.ASMClassReader
import de.kb1000.joptimizer.optimizations.lowlevel.*
import de.kb1000.joptimizer.transformation.lowleveltossa.LowLevelToSSA2
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.CheckClassAdapter
import java.io.IOException
import java.io.PrintWriter
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.io.path.writeText

private fun readFile(classPool: ClassPool, path: Path) {
    val data = path.readBytes()
    val classReader = ClassReader(data)
    val loader = ASMClassReader(classPool)
    classReader.accept(loader, 0)
    classPool.add(loader.resultClass)
}

private fun readTree(classPool: ClassPool, path: Path) {
    Files.walk(path).filter(Files::isRegularFile).filter { it.toString().endsWith(".class") }.forEach {
        readFile(classPool, it)
    }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
    val classPool = ClassPool()
    for (arg in args) {
        val path = Path(arg)
        if (Files.isRegularFile(path)) {
            if (path.toString().endsWith(".class")) {
                readFile(classPool, path)
            } else {
                FileSystems.newFileSystem(path).use { fileSystem ->
                    fileSystem.rootDirectories.forEach { readTree(classPool, it) }
                }
            }
        } else {
            readTree(classPool, path)
        }
    }

    val lowLevelPasses = listOf(
        DropUnusedVars(),
        DropUnusedConstants(),
        DropLoadStore(),
        StoreLoadToDupStore(),
        DropDupPop(),
        DropNullCasts(),
        SimplifyControlFlowGraph(),
        DeadCodeElimination()
    )

    // TODO: should the pass loop be the inner or the outer loop?
    for (clazz in classPool.getClasses().values) {
        for (i in 0..4) {
            //if (false)
            for (pass in lowLevelPasses) {
                pass.run(classPool, clazz)
            }
        }
    }

    for (clazz in classPool.getClasses().values) {
        for (method in clazz.methods) {
            try {
                LowLevelToSSA2().transform(classPool, method)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    val outPath = Path("out")
    for (clazz in classPool.getClasses().values) {
        val path = outPath.resolve("${clazz.internalName}.txt")
        Files.createDirectories(path.parent)
        path.writeText(clazz.dump())
    }
}

object CatchTest {
    @JvmStatic
    fun main(vararg s: String) {
        val cv = ClassNode()
        cv.visit(Opcodes.V1_6, Opcodes.ACC_SUPER, "Test", null, "java/lang/Object", null)
        val mv = cv.visitMethod(Opcodes.ACC_STATIC, "test", "()V", null, null)
        mv.visitCode()
        mv.visitInsn(Opcodes.ICONST_0)
        mv.visitVarInsn(Opcodes.ISTORE, 0)
        mv.visitInsn(Opcodes.ICONST_0)
        mv.visitVarInsn(Opcodes.ISTORE, 1)
        mv.visitLdcInsn("")
        mv.visitVarInsn(Opcodes.ASTORE, 2)
        mv.visitInsn(Opcodes.ACONST_NULL)
        mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Object")
        mv.visitVarInsn(Opcodes.ASTORE, 3)
        val label1 = Label()
        mv.visitLabel(label1)
        mv.visitInsn(Opcodes.LCONST_0)
        mv.visitVarInsn(Opcodes.LSTORE, 0)
        mv.visitInsn(Opcodes.ICONST_0)
        mv.visitVarInsn(Opcodes.ISTORE, 0)
        mv.visitInsn(Opcodes.ICONST_0)
        mv.visitVarInsn(Opcodes.ISTORE, 1)
        mv.visitInsn(Opcodes.RETURN)
        val label2 = Label()
        mv.visitLabel(label2)
        mv.visitTryCatchBlock(label1, label2, label2, null)
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        cv.visitEnd()
        cv.accept(CheckClassAdapter(null))
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        cv.accept(classWriter)
        val ba = classWriter.toByteArray()
        ClassReader(ba).accept(CheckClassAdapter(null), 0)
        CheckClassAdapter.verify(ClassReader(ba), true, PrintWriter(System.err))
    }
}
