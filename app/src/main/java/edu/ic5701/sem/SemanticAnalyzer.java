package edu.ic5701.sem;

import edu.ic5701.ast.Program;
import edu.ic5701.error.ErrorHandler;
import edu.ic5701.error.SemanticTokenErrorHandler;
import edu.ic5701.sem.factory.TokenPrimitiveFactory;
import edu.ic5701.sem.factory.PrimitiveFactory;
import edu.ic5701.sem.rules.TypeArithmeticRules;

public final class SemanticAnalyzer {
    private final Program program;
    private final NameResolver resolver;

    public SemanticAnalyzer(Program program) {
        this.program = program;
        this.resolver = new NameResolver();
    }

    public Program analyze() {
        System.out.println("NAMES");
        Program resolveProgram = resolveNames();
        System.out.println("checker");
        SemanticTypeChecker typeChecker = createSemanticTypeChecker(resolveProgram);
        return (Program) resolveProgram.accept(typeChecker);
    }

    private Program resolveNames() {
        return resolver.solve(program);
    }

    private SemanticTypeChecker createSemanticTypeChecker(Program resolvedAST) {
        final PrimitiveFactory primitiveFactory = new TokenPrimitiveFactory();
        final SemanticTokenErrorHandler errorHandler = new SemanticTokenErrorHandler();
        final TypeArithmeticRules arithmeticRules = new TypeArithmeticRules(errorHandler, primitiveFactory);
        return new SemanticTypeChecker(arithmeticRules, errorHandler, primitiveFactory);
    }
}
