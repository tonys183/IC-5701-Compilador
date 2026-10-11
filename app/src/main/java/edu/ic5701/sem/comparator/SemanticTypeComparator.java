package edu.ic5701.sem.comparator;

import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.expr.CollectionLitExpr;
import edu.ic5701.ast.expr.Expr;
import edu.ic5701.ast.type.ArrayType;
import edu.ic5701.ast.type.ListType;
import edu.ic5701.ast.type.PrimitiveType;
import edu.ic5701.ast.type.Type;
import edu.ic5701.error.ErrorHandler;
import edu.ic5701.error.TokenErrorHandler;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;
import edu.ic5701.sem.SemanticTypeChecker;
import edu.ic5701.sem.rules.TypeResolver;
import java.util.ArrayList;
import java.util.Optional;

public final class SemanticTypeComparator implements TypeComparator {


    private static final String ERR_COND_VALUE =
            "The type of '%s' must be a '%s'";
    private static final String ERR_INCOMPATIBLE_LITERAL_TYPE =
            "The type '%s' must be the same as the type of '%s'";
    private static final String ERR_INCOMPATIBLE_LITERAL_OP =
            "The values '%s' and '%s' must be the same type for this operation";
    private static final String ERR_INCOMPATIBLE_ARRAY =
            "The type or the dimensions of de array '%s' mismatches with the value '%s'";

    private final TypeResolver typeResolver;
    private final TokenErrorHandler errorHandler;
    private final Visitor<Record> typeChecker;

    public SemanticTypeComparator(TypeResolver typeResolver, SemanticTypeChecker typeChecker, TokenErrorHandler errorHandler) {
        this.typeResolver = typeResolver;
        this.typeChecker = typeChecker;
        this.errorHandler = errorHandler;
    }

    @Override
    public void compare(Type type, PrimitiveType... primitiveTypes) {

        if (!(type instanceof PrimitiveType actualPrimitive)) {
            String expectedTypes = primitiveNamesOf(primitiveTypes);
            throw errorHandler.error(type.token(), ERR_COND_VALUE,
                    type.token().lexeme(), expectedTypes
            );
        }

        for (PrimitiveType expectedPrimitive : primitiveTypes) {
            if (checkSameDataType(
                    expectedPrimitive.token().type(),
                    actualPrimitive.token().type()
            )) {
                return;
            }
        }

        String expectedTypes = primitiveNamesOf(primitiveTypes);

        throw errorHandler.error(
                type.token(),
                ERR_COND_VALUE,
                type.token().lexeme(),
                expectedTypes
        );
    }

    @Override
    public void compare(Expr expr1, Expr expr2) {
        Type type1 = typeResolver.typeOf(expr1);
        Type type2 = typeResolver.typeOf(expr2);
        if (!isSameType(type1, type2)) {
            throw errorHandler.error(expr1.token(), ERR_INCOMPATIBLE_LITERAL_OP,
                                     expr1.token().lexeme(), expr2.token().lexeme()
            );
        }
    }

    @Override
    public void compare(Type type, Expr expr) {
        if (expr instanceof CollectionLitExpr collectionLitExpr) {
            compare(type, collectionLitExpr);
            return;
        }
        Type exprType = typeResolver.typeOf(expr);
        if (!isSameType(type, exprType)) {
            throw errorHandler.error(type.token(), ERR_INCOMPATIBLE_LITERAL_TYPE,
                                     type.token().lexeme(), expr.token().lexeme()
            );
        }
    }

    @Override
    public void compare(Type expectedType, CollectionLitExpr collectionLitExpr) {
        collectionLitExpr.accept(typeChecker);
        if (expectedType instanceof ArrayType arrayType) {
            int expectedDimensions = arrayType.dimensions().size();
            int actualDimensions = dimensionSizeOf(collectionLitExpr);

            if (expectedDimensions != actualDimensions) {
                throw errorHandler.error(expectedType.token(),
                        ERR_INCOMPATIBLE_ARRAY,
                        expectedType.token().lexeme(),
                        collectionLitExpr.token().lexeme());
            }
            compareCollectionElements(arrayType.baseType(), collectionLitExpr);
            return;
        }

        if (expectedType instanceof ListType listType) {
            compareCollectionElements(listType.innerType(), collectionLitExpr);
            return;
        }

        for (Expr value : collectionLitExpr.value()) {
            Expr checkedValue = (Expr) value.accept(typeChecker);
            compare(expectedType, checkedValue);
        }
    }

    @Override
    public boolean isSameType(Type type1, Type type2) {
        return type1.equals(type2);
    }

    private void compareCollectionElements(Type expectedElementType, CollectionLitExpr collectionLitExpr) {
        for (Expr value : collectionLitExpr.value()) {
            Expr checkedValue = (Expr) value.accept(typeChecker);
            if (checkedValue instanceof CollectionLitExpr collectionVal) {
                compare(expectedElementType, collectionVal);
            } else {
                compare(expectedElementType, checkedValue);
            }
        }
    }

    private Boolean checkSameDataType(TokenType tokenType1, TokenType tokenType2) {
        return tokenType1.equals(tokenType2)
                || tokenType1.equals(TokenType.KEYWORD_STRING) && tokenType2.equals(TokenType.LIT_STRING)
                || tokenType1.equals(TokenType.KEYWORD_INT) && tokenType2.equals(TokenType.LIT_INT)
                || tokenType1.equals(TokenType.KEYWORD_CHAR) && tokenType2.equals(TokenType.LIT_CHAR)
                || tokenType1.equals(TokenType.KEYWORD_FLOAT) && tokenType2.equals(TokenType.LIT_FLOAT);
    }

    private int dimensionSizeOf(CollectionLitExpr collection) {
        if (collection.value().isEmpty()) {
            return 1;
        }
        if (collection.value().getFirst() instanceof CollectionLitExpr nestedCollection) {
            return 1 + dimensionSizeOf(nestedCollection);
        }
        return 1;
    }
    private String primitiveNamesOf(PrimitiveType... primitiveTypes) {
        ArrayList<String> expectedTokenTypes = new ArrayList<>();

        for (PrimitiveType primitive : primitiveTypes) {
            expectedTokenTypes.add(
                    primitive.token().type().name().replace("KEYWORD_", "")
            );
        }

        return String.join(" or ", expectedTokenTypes);
    }
}
