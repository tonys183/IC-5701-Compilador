package edu.ic5701.sem.rules;

import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.expr.*;
import edu.ic5701.ast.type.ArrayType;
import edu.ic5701.ast.type.ListType;
import edu.ic5701.ast.type.PrimitiveType;
import edu.ic5701.ast.type.Type;
import edu.ic5701.error.ErrorHandler;
import edu.ic5701.error.TokenErrorHandler;
import edu.ic5701.scanner.TokenType;
import edu.ic5701.sem.factory.PrimitiveFactory;
import edu.ic5701.sem.table.Symbol;
import java.util.Optional;

public final class ExpressionTypeResolver implements TypeResolver {

    private static final String ERR_INCOMPATIBLE_ARRAY =
            "The type or the dimensions of de array '%s' mismatches with the value '%s'";
    private static final int MAX_STRING_ACCESS = 1;

    private final PrimitiveFactory primitiveFactory;
    private final Visitor<Record> typeChecker;
    private final TokenErrorHandler errorHandler;

    public ExpressionTypeResolver(PrimitiveFactory primitiveFactory,
                                  Visitor<Record> typeChecker,
                                  TokenErrorHandler errorHandler) {
        this.primitiveFactory = primitiveFactory;
        this.typeChecker = typeChecker;
        this.errorHandler = errorHandler;
    }

    @Override
    public Type typeOf(Expr expr) {
        Optional<Symbol> symbol = Optional.ofNullable(expr.symbol());

        if (symbol.isPresent()) {
            return typeAfterAccess(symbol.get().type(), expr);
        }

        return switch (expr) {
            case LiteralIdent literalIdent -> literalIdent.type();
            case FuncCall funcCall -> funcCall.type();
            case ArrayAccess arrayAccess -> arrayAccess.type();
            case AssignExpr assignExpr -> assignExpr.type();
            case OrExpr orExpr -> orExpr.type();
            case PopExpr popExpr -> popExpr.type();
            case PushExpr pushExpr -> pushExpr.type();
            case AndExpr andExpr -> andExpr.type();
            case CompExpr compExpr -> compExpr.type();
            case RelExpr relExpr -> relExpr.type();
            case AddExpr addExpr -> addExpr.type();
            case MulExpr mulExpr -> mulExpr.type();
            case UnaryNegExpr unaryNegExpr -> unaryNegExpr.type();
            case UnaryNotExpr unaryNotExpr -> unaryNotExpr.type();
            case RangeExpr rangeExpr -> rangeExpr.type();
            case CollectionLitExpr collectionLitExpr -> {
                if (collectionLitExpr.type() != null) {
                    yield collectionLitExpr.type();
                }

                if (collectionLitExpr.value().isEmpty()) {
                    yield primitiveFactory.createPrimitive(TokenType.KEYWORD_INT, collectionLitExpr.token());
                }

                Expr first = (Expr) collectionLitExpr.value().getFirst().accept(typeChecker);
                yield typeOf(first);
            }
            case IntLitExpr intLitExpr ->
                    primitiveFactory.createPrimitive(TokenType.KEYWORD_INT, intLitExpr.token());
            case FloatLitExpr floatLitExpr ->
                    primitiveFactory.createPrimitive(TokenType.KEYWORD_FLOAT, floatLitExpr.token());
            case CharLitExpr charLitExpr ->
                    primitiveFactory.createPrimitive(TokenType.KEYWORD_CHAR, charLitExpr.token());
            case StringLitExpr stringLitExpr ->
                    primitiveFactory.createPrimitive(TokenType.KEYWORD_STRING, stringLitExpr.token());
            default ->
                    primitiveFactory.createPrimitive(expr.token().type(), expr.token());
        };
    }

    @Override
    public Type typeAfterAccess(Type originalType, Expr expr) {
        System.out.println("typeAfterAccess"+originalType.token());
        if (!(expr instanceof ArrayAccess)) {
            return originalType;
        }
        int access = accessDimension(expr);
        if (originalType instanceof PrimitiveType primitiveType
                && primitiveType.token().type().equals(TokenType.KEYWORD_STRING)) {
            return typeAfterStringAccess(originalType, expr, access);
        }

        int dimensions = originalType.dimensionSizeOf();
        if (access > dimensions) {
            throw errorHandler.error(expr.token(),
                    ERR_INCOMPATIBLE_ARRAY,
                    originalType.token().lexeme(),
                    expr.token().lexeme()
            );
        }

        if (originalType instanceof ArrayType arrayType) {
            return typeAfterArrayAccess(arrayType, dimensions, access);
        }

        if (originalType instanceof ListType listType) {
            return typeAfterListAccess(originalType, expr, listType, access);
        }
        return originalType;
    }

    @Override
    public int accessDimension(Expr access) {
        if (!(access instanceof ArrayAccess arrayAccess)) {
            return 0;
        }
        return 1 + accessDimension(arrayAccess.target());
    }

    private Type typeAfterListAccess(Type originalType, Expr expr, ListType listType, int access) {
        Type current = listType;
        for (int i = 0; i < access; i++) {
            if (current instanceof ListType currentList) {
                current = currentList.innerType();
            } else {
                throw errorHandler.error(expr.token(),
                        ERR_INCOMPATIBLE_ARRAY,
                        originalType.token().lexeme(),
                        expr.token().lexeme());
            }
        }
        return current;
    }

    private Type typeAfterArrayAccess(ArrayType arrayType, int dimensions, int access) {
        if ((dimensions - access) == 0) {
            return arrayType.baseType();
        }
        return new ArrayType(
                arrayType.baseType(),
                arrayType.dimensions().subList(access, arrayType.dimensions().size())
        );
    }

    private Type typeAfterStringAccess(Type originalType, Expr expr, int access) {
        if (access == MAX_STRING_ACCESS) {
            return primitiveFactory.createPrimitive(TokenType.KEYWORD_CHAR, expr.token());
        }
        throw errorHandler.error(expr.token(),
                ERR_INCOMPATIBLE_ARRAY,
                originalType.token().lexeme(),
                expr.token().lexeme()
        );
    }
}
