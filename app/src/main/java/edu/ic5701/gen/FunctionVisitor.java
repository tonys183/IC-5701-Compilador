package edu.ic5701.gen;

import edu.ic5701.ast.Program;
import edu.ic5701.ast.expr.*;
import edu.ic5701.ast.stmt.*;
import edu.ic5701.ast.type.ArrayType;
import edu.ic5701.ast.type.ListType;
import edu.ic5701.ast.type.PrimitiveType;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class FunctionVisitor implements VariableResolver {

    private Integer currentLoadVar;
    private Integer currentParamSize;
    private Map<Record, Integer> parameters;
    private Map<Record, Integer> locals;
    private final Map<String, Map<Record, Integer>> funcParameters;
    private final Map<String, Map<Record, Integer>> funcLocals;

    public FunctionVisitor() {
        this.funcParameters = new IdentityHashMap<>();
        this.funcLocals = new IdentityHashMap<>();
    }

    @Override
    public Map<String, Map<Record, Integer>> locals() {
        return this.funcLocals;
    }

    @Override
    public Map<String, Map<Record, Integer>> parameters() {
        return this.funcParameters;
    }

    @Override
    public void solve(Program program) {
        program.accept(this);
    }

    @Override
    public Void visit(Program program) {
        for (FuncDecl function : program.body()) {
            String currentFunc = function.name().lexeme();
            this.currentLoadVar = 0;
            this.currentParamSize = 0;
            this.parameters = new IdentityHashMap<>();
            this.locals = new IdentityHashMap<>();
            function.accept(this);
            this.funcParameters.put(currentFunc, this.parameters);
            this.funcLocals.put(currentFunc, this.locals);
        }

        return null;
    }

    @Override
    public Void visit(FuncDecl functionDeclaration) {
        functionDeclaration.param().accept(this);
        functionDeclaration.body().accept(this);
        functionDeclaration.funcType().accept(this);
        return null;
    }

    @Override
    public Void visit(ParamDecl paramDecl) {
        this.currentParamSize = paramDecl.params().size();
        for (DeclVariable param : paramDecl.params()) {
            param.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(DeclVariable variableDeclaration) {
        storeValue(variableDeclaration.symbol());
        variableDeclaration.type().accept(this);
        variableDeclaration.assignValue().ifPresent(value -> value.accept(this));
        return null;
    }

    @Override
    public Void visit(Block block) {
        for (Stmt stmt : block.statements()) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(StmtReturn stmtReturn) {
        stmtReturn.expr().accept(this);
        return null;
    }

    @Override
    public Void visit(StmtFor stmtFor) {
        stmtFor.iterator().accept(this);
        storeValue(stmtFor.iterator());
        stmtFor.list().ifPresent(list -> list.accept(this));
        storeValue(stmtFor);
        stmtFor.body().accept(this);
        return null;
    }

    @Override
    public Void visit(StmtIf stmtIf) {
        stmtIf.cond().accept(this);
        stmtIf.thenBody().accept(this);
        stmtIf.elseBody().ifPresent(elseBody -> elseBody.accept(this));
        return null;
    }

    @Override
    public Void visit(StmtWhile stmtWhile) {
        stmtWhile.cond().accept(this);
        stmtWhile.body().accept(this);
        return null;
    }

    @Override
    public Void visit(StmtPrint stmtPrint) {
        stmtPrint.expr().accept(this);
        return null;
    }

    @Override
    public Void visit(StmtExpr stmtExpr) {
        stmtExpr.expr().accept(this);
        return null;
    }

    @Override
    public Void visit(AssignExpr assignExpr) {
        assignExpr.expr().accept(this);
        assignExpr.target().accept(this);
        return null;
    }

    @Override
    public Void visit(PushExpr pushExpr) {
        pushExpr.type().accept(this);
        pushExpr.name().accept(this);
        pushExpr.value().accept(this);
        return null;
    }

    @Override
    public Void visit(PopExpr popExpr) {
        popExpr.type().accept(this);
        popExpr.name().accept(this);
        return null;
    }

    @Override
    public Void visit(OrExpr orExpr) {
        orExpr.left().accept(this);
        orExpr.right().accept(this);
        return null;
    }

    @Override
    public Void visit(AndExpr andExpr) {
        andExpr.left().accept(this);
        andExpr.right().accept(this);
        return null;
    }

    @Override
    public Void visit(RelExpr relExpr) {
        relExpr.left().accept(this);
        relExpr.right().accept(this);
        return null;
    }

    @Override
    public Void visit(CompExpr compExpr) {
        compExpr.left().accept(this);
        compExpr.right().accept(this);
        return null;
    }

    @Override
    public Void visit(AddExpr addExpr) {
        addExpr.left().accept(this);
        addExpr.right().accept(this);
        return null;
    }

    @Override
    public Void visit(MulExpr mulExpr) {
        mulExpr.left().accept(this);
        mulExpr.right().accept(this);
        return null;
    }

    @Override
    public Void visit(UnaryNotExpr unaryExpr) {
        unaryExpr.left().accept(this);
        return null;
    }

    @Override
    public Void visit(UnaryNegExpr unaryExpr) {
        unaryExpr.left().accept(this);
        return null;
    }

    @Override
    public Void visit(FuncCall funcCall) {
        funcCall.type().accept(this);
        funcCall.name().accept(this);
        for (Expr arg : funcCall.args()) {
            arg.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ArrayAccess arrayAccess) {
        arrayAccess.target().accept(this);
        arrayAccess.index().accept(this);
        arrayAccess.type().accept(this);
        storeValue(arrayAccess);
        return null;
    }

    @Override
    public Void visit(LiteralIdent identExpr) {
        identExpr.type().accept(this);
        return null;
    }

    @Override
    public Void visit(RangeExpr rangeExpr) {
        rangeExpr.inf().accept(this);
        rangeExpr.sup().accept(this);
        storeValue(rangeExpr);
        storeValue((Record) rangeExpr.inf());
        return null;
    }

    @Override
    public Void visit(CollectionLitExpr arrayLitExpr) {
        storeValue(arrayLitExpr);
        arrayLitExpr.type().accept(this);
        for (Expr element : arrayLitExpr.value()) {
            element.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(IntLitExpr intLitExpr) {
        return null;
    }

    @Override
    public Void visit(FloatLitExpr floatLitExpr) {
        return null;
    }

    @Override
    public Void visit(CharLitExpr charLitExpr) {
        return null;
    }

    @Override
    public Void visit(StringLitExpr stringLitExpr) {
        return null;
    }

    @Override
    public Void visit(PrimitiveType primitiveType) {
        return null;
    }

    @Override
    public Void visit(ListType listType) {
        listType.innerType().accept(this);
        return null;
    }

    @Override
    public Void visit(ArrayType arrayType) {
        arrayType.baseType().accept(this);
        for (Optional<Expr> dimension : arrayType.dimensions()) {
            dimension.ifPresent(expr -> expr.accept(this));
        }
        return null;
    }
    
    private void storeValue(Record name) {
        if (this.currentLoadVar < this.currentParamSize) {
            this.parameters.put(name, this.currentLoadVar);
        } else {
            this.locals.put(name, this.currentLoadVar);
        }
        this.currentLoadVar++;
    }
}