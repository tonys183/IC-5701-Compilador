package edu.ic5701;

import edu.ic5701.ast.AstFormatter;
import edu.ic5701.ast.Program;
import edu.ic5701.gen.*;
import edu.ic5701.parser.Parser;
import edu.ic5701.parser.RecursiveDescentParser;
import edu.ic5701.scanner.MappingScanner;
import edu.ic5701.scanner.Scanner;
import edu.ic5701.sem.SemanticAnalyzer;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Map;

public class Compiler {

    public static void main(final String[] args) {

        Path path = Path.of(args[0]);
        String outputFileName = "out.stkasm";
        Path outputPath = Path.of(System.getProperty("user.dir")).resolve(outputFileName);

        try {
            final Scanner scanner = MappingScanner.createScanner(path);
            System.out.print("Tokens: \n");
            final Parser parser = new RecursiveDescentParser(scanner);
            Program program = parser.parse();

            System.out.print("\nProgram AST Resolved: ");
            final SemanticAnalyzer analyzer = new SemanticAnalyzer(program);
            final VariableResolver variableResolver = new FunctionVisitor();
            final Program analyzedProgram =  analyzer.analyze();
            System.out.println(AstFormatter.format(analyzedProgram));

            variableResolver.solve(analyzedProgram);
            System.out.println(formatMapOfMaps("\nParametros por funcion", variableResolver.parameters()));
            System.out.println(formatMapOfMaps("Locales por funcion", variableResolver.locals()));
            final FileWriter writer = new FileWriter(outputPath.toFile());
            final Writer stackWriter = new StackvmFileWriter(writer);
            final StackvmCodeGenerator generator = new StackvmCodeGenerator(variableResolver, stackWriter);
            generator.solve(analyzedProgram);

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String formatMapOfMaps(String title, Map<String, Map<Record, Integer>> map) {
        StringBuilder sb = new StringBuilder();

        sb.append(title).append(":\n");

        for (Map.Entry<String, Map<Record, Integer>> functionEntry : map.entrySet()) {
            sb.append("  ").append(functionEntry.getKey()).append(":\n");

            for (Map.Entry<Record, Integer> entry : functionEntry.getValue().entrySet()) {
                sb.append("    ")
                        .append(entry.getKey())
                        .append(" -> ")
                        .append(entry.getValue())
                        .append("\n");
            }
        }

        return sb.toString();
    }
}
