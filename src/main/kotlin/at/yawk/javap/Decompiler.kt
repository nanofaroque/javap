package at.yawk.javap

import com.strobel.assembler.metadata.Buffer
import com.strobel.assembler.metadata.ClassFileReader
import com.strobel.assembler.metadata.IMetadataResolver
import com.strobel.assembler.metadata.MetadataSystem
import com.strobel.decompiler.DecompilerContext
import com.strobel.decompiler.DecompilerSettings
import com.strobel.decompiler.PlainTextOutput
import com.strobel.decompiler.languages.java.BraceStyle
import com.strobel.decompiler.languages.java.JavaFormattingOptions
import com.strobel.decompiler.languages.java.ast.AstBuilder
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author yawkat
 */
enum class Decompiler {
    PROCYON {
        private val settings = DecompilerSettings()

        init {
            // this ensures correct initialization order - removing this line will lead to an Exception in procyon code
            // (try it)
            MetadataSystem.instance()

            settings.forceExplicitImports = true
            settings.showSyntheticMembers = true
            settings.javaFormattingOptions = JavaFormattingOptions.createDefault()
            settings.javaFormattingOptions.ClassBraceStyle = BraceStyle.EndOfLine
            settings.javaFormattingOptions.EnumBraceStyle = BraceStyle.EndOfLine
        }

        override fun decompile(classDir: Path): String {
            val astBuilder = AstBuilder(DecompilerContext(settings))
            Files.newDirectoryStream(classDir).use {
                for (classFile in it) {
                    if (!classFile.toString().endsWith(".class")) continue
                    astBuilder.addType(ClassFileReader.readClass(
                            ClassFileReader.OPTION_PROCESS_ANNOTATIONS or ClassFileReader.OPTION_PROCESS_CODE,
                            IMetadataResolver.EMPTY,
                            Buffer(Files.readAllBytes(classFile))
                    ))
                }
            }
            val output = PlainTextOutput()
            astBuilder.generateCode(output)
            return output.toString()
        }
    };

    @Throws(Exception::class)
    abstract fun decompile(classDir: Path): String
}