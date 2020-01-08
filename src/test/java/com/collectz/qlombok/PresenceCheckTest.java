package com.collectz.qlombok;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import lombok.javac.apt.LombokProcessor;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaFileObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.google.testing.compile.Compiler.javac;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class PresenceCheckTest {

  private static final String TEST_DATA_OUTPUT_PATH = "test-out";
  private final Logger log = LoggerFactory.getLogger(getClass());

  protected void testClass(String path, String className) throws IOException {
    String actualBytecodeInstructions = compileGiven(path, className);
    String expectedBytecodeInstructions = compileExpected(path, className);
    assertThat(actualBytecodeInstructions).isEqualTo(expectedBytecodeInstructions);
  }

  private String compileExpected(String path, String className) throws IOException {
    return compile(path, "expected", className, javac());
  }

  private String compileGiven(String path, String className) throws IOException {
    return compile(path, "given", className, javac().withProcessors(new LombokProcessor()));
  }

  private String compile(String path, String prefix, String className, Compiler compiler) throws IOException {
    // Use '-g:none' to omit line numbers because lombok generates new code without source line numbers
    // and it messes up diff checker
    Compilation compilation = compiler
        .withOptions("-g:none")
        .compile(JavaFileObjects.forResource(String.join("/", path, prefix, className + ".java")));
    Optional<JavaFileObject> javaFileObject = compilation
        .generatedFile(CLASS_OUTPUT, "test", className + ".class");

    if (javaFileObject.isPresent()) {
      byte[] bytes = IOUtils.toByteArray(javaFileObject.get().openInputStream());

      String buildDir = System.getProperty("gradleBuildDir");
      if (buildDir == null) {
        buildDir = "build";
      }

      Path outputPath = Paths.get(buildDir, TEST_DATA_OUTPUT_PATH, path, prefix);
      Files.createDirectories(outputPath);

      Path outputFile = Paths.get(buildDir, TEST_DATA_OUTPUT_PATH, path, prefix, className + ".class");
      log.info("Writing {} {} class file: {}", prefix, className, outputFile);
      IOUtils.write(bytes, new FileOutputStream(outputFile.toFile()));
      return disassemble(bytes);
    }
    else {
      throw new RuntimeException("Cannot find output");
    }
  }

  private String disassemble(byte[] classBytes) throws IOException {
    ClassReader cr = new ClassReader(new ByteArrayInputStream(classBytes));
    try (StringWriter out = new StringWriter()) {
      try (PrintWriter printWriter = new PrintWriter(out)) {
        cr.accept(new TraceClassVisitor(printWriter), 0);
        printWriter.flush();
        return out.toString();
      }
    }
  }
}
