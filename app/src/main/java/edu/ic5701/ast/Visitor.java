package edu.ic5701.ast;

import edu.ic5701.ast.expr.*;
import edu.ic5701.ast.stmt.*;
import edu.ic5701.ast.type.ArrayType;
import edu.ic5701.ast.type.ListType;
import edu.ic5701.ast.type.PrimitiveType;

public interface Visitor<R> {
    R visit(Program program);
    R visit(FuncDecl functionDeclaration);
    R visit(ParamDecl paramDecl);
    R visit(DeclVariable variableDeclaration);
    R visit(Block block);
    R visit(StmtReturn stmtReturn);
    R visit(StmtFor stmtFor);
    R visit(StmtIf stmtIf);
    R visit(StmtWhile stmtWhile);
    R visit(StmtPrint stmtPrint);
    R visit(StmtExpr stmtExpr);
    R visit(AssignExpr assignExpr);
    R visit(PushExpr pushExpr);
    R visit(PopExpr popExpr);
    R visit(OrExpr orExpr);
    R visit(AndExpr andExpr);
    R visit(RelExpr relExpr);
    R visit(CompExpr compExpr);
    R visit(AddExpr addExpr);
    R visit(MulExpr mulExpr);
    R visit(UnaryNotExpr unaryExpr);
    R visit(UnaryNegExpr unaryExpr);
    R visit(FuncCall funcCall);
    R visit(ArrayAccess arrayAccess);
    R visit(LiteralIdent identExpr);
    R visit(RangeExpr rangeExpr);
    R visit(CollectionLitExpr arrayLitExpr);
    R visit(IntLitExpr intLitExpr);
    R visit(FloatLitExpr floatLitExpr);
    R visit(CharLitExpr charLitExpr);
    R visit(StringLitExpr stringLitExpr);
    R visit(PrimitiveType primitiveType);
    R visit(ListType listType);
    R visit(ArrayType arrayType);
}
