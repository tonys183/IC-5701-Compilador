package edu.ic5701.sem.comparator;

import edu.ic5701.ast.expr.CollectionLitExpr;
import edu.ic5701.ast.expr.Expr;
import edu.ic5701.ast.type.PrimitiveType;
import edu.ic5701.ast.type.Type;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;

public interface TypeComparator {
    void compare(Expr expr1, Expr expr2);
    void compare(Type type, Expr expr);
    void compare(Type expectedType, CollectionLitExpr collectionLitExpr);
    void compare(Type type, PrimitiveType... primitiveTypes);
    boolean isSameType(Type type1, Type type2);
}
