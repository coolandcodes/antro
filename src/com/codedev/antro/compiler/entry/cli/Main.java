package com.codedev.antro.compiler.entry.cli;

import info.picocli.CommandLine;
import info.picocli.CommandLine.Command;
import info.picocli.CommandLine.Parameters;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "interpret", mixinStandardHelpOptions = true, version = "1.0",
        description = "Interprets source files from the CLI.")
public class Main implements Callable<Integer> {

    @Parameters(index = "0", description = "The source file to run.", arity = "0..1")
    private File sourceFile;

    @Override
    public Integer call() throws Exception {
        if (sourceFile == null) {
            runRepl();
        } else {
            runFile(sourceFile);
        }
        return 0;
    }

    private void runRepl() {
        System.out.println("Welcome to the Antro Interactive Compiler (antroc)");
        // @HINT: REPL logic here
    }

    private void runFile(File file) throws Exception {
        System.out.println("Executing: " + file.getName());
        // @NOTE: Lexer -> Parser -> Evaluator logic here
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
