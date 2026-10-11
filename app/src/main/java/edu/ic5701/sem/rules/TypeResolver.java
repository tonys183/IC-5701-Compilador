package edu.ic5701.sem.rules;

import edu.ic5701.ast.expr.Expr;
import edu.ic5701.ast.type.Type;

public interface TypeResolver {
    Type typeOf(Expr expr);
    Type typeAfterAccess(Type originalType, Expr expr);
    int accessDimension(Expr access);
}
