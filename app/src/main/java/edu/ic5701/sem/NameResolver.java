package edu.ic5701.sem;

import edu.ic5701.ast.Program;
import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.expr.*;
import edu.ic5701.ast.stmt.*;
import edu.ic5701.error.ErrorHandler;
import edu.ic5701.error.SemanticTokenErrorHandler;
import edu.ic5701.scanner.Token;
import edu.ic5701.sem.table.Category;
import edu.ic5701.sem.table.Scope;
import edu.ic5701.sem.table.Symbol;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import edu.ic5701.ast.type.*;

public class NameResolver implements Visitor<Record> {

    private static final String ERR_UNDECLARED_IDENTIFIER =
            "Identifier '%s' has not been declared in this scope.";
    private static final String ERR_INVALID_FUNCTION_CALL =
            "Identifier '%s' is incompatible with a function call.";
    private static final String ERR_UNDECLARED_FUNCTION =
            "Function '%s' has not been declared in this scope.";
    private static final String ERR_REDECLARED_IDENTIFIER =
            "Identifier '%s' has already been declared in this same scope.";
    private static final String ERR_DUPLICATED_PARAMETER_OR_VARIABLE =
            "Parameter or variable '%s' has already been declared in this same scope.";
    private static final String ERR_EMPTY_FOR_LIST =
            "The array or list used in the for loop is empty.";
    private static final String ERR_INVALID_ASSIGNMENT_TARGET =
            "The left-hand side of an assignment must be an identifier or an array access.";
    private static final String ERR_NOT_A_FUNCTION =
            "Identifier '%s' was declared, but not as a function.";
    private static final String ERR_INCORRECT_PARAMETER =
            "The variable or parameter '%s' is being assigned. Must only be declared.";

    private Scope currentScope;
    private final SemanticTokenErrorHandler errorHandler = new SemanticTokenErrorHandler();

    public Program solve(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public Record visit(Program program) {
        nextScope();
        for (FuncDecl function : program.body()) {
            declareFunctionHeader(function);
        }
        List<FuncDecl> functions = new ArrayList<>();
        for (FuncDecl function : program.body()) {
            functions.add((FuncDecl) function.accept(this));
        }
        pop();
        return new Program(functions);
    }

    @Override
    public Record visit(FuncDecl functionDeclaration) {
        Symbol functionSymbol = lookFor(functionDeclaration.name().lexeme()).get();
        nextScope();
        currentScope.add(functionSymbol);

        ParamDecl params = (ParamDecl) functionDeclaration.param().accept(this);
        Block body = (Block) functionDeclaration.body().accept(this);
        pop();

        return new FuncDecl(
                functionDeclaration.funcType(),
                params,
                functionDeclaration.name(),
                body
        );
    }

    @Override
    public Record visit(ParamDecl paramDecl) {
        List<DeclVariable> params = new ArrayList<>();
        for (DeclVariable param : paramDecl.params()) {
            if (param.assignValue().isPresent()) {
                throw errorHandler.error(
                        param.name(),
                        ERR_INCORRECT_PARAMETER,
                        param.name().lexeme()
                );
            }
            params.add((DeclVariable) param.accept(this));
        }
        return new ParamDecl(params);
    }

    @Override
    public Record visit(Block block) {
        nextScope();
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : block.statements()) {
            statements.add((Stmt) stmt.accept(this));
        }
        pop();
        return new Block(statements);
    }

    @Override
    public Record visit(DeclVariable variableDeclaration) {
        Token variableName = variableDeclaration.name();
        Type declType = variableDeclaration.type();
        if (declType instanceof ArrayType array) {
            List<Optional<Expr>> dimensions = new ArrayList<>();
            for (Optional<Expr> value : array.dimensions()) {
                dimensions.add(
                        Optional.ofNullable(
                                (Expr) value.map(expr -> expr.accept(this)).orElse(null)
                        )
                );
            }
            declType = new ArrayType(array.baseType(), dimensions);
        }

        Optional<Symbol> parentIdent;
        if (lookFor(variableName.lexeme()).isPresent()) {
            throw errorHandler.error(
                    variableName,
                    ERR_DUPLICATED_PARAMETER_OR_VARIABLE,
                    variableName.lexeme()
            );
        }

        Optional<Expr> value = Optional.empty();
        if (variableDeclaration.assignValue().isPresent()) {
            value = Optional.of(
                    (Expr) variableDeclaration.assignValue().get().accept(this)
            );
        }

        Symbol variableSymbol = new Symbol(
                variableName.lexeme(),
                variableDeclaration.type(),
                Category.VARIABLE
        );
        currentScope.add(variableSymbol);
        
        return new DeclVariable(
                declType,
                variableName,
                value,
                variableSymbol
        );
    }

    @Override
    public Record visit(StmtPrint stmtPrint) {
        Expr expr = (Expr) stmtPrint.expr().accept(this);
        return new StmtPrint(expr);
    }

    @Override
    public Record visit(StmtFor stmtFor) {
        if (stmtFor.list().isEmpty()) {
            throw errorHandler.error(stmtFor.iterator().name(), ERR_EMPTY_FOR_LIST);
        }
        Expr list = (Expr) stmtFor.list().get().accept(this);

        if (stmtFor.iterator().assignValue().isPresent()) {
            throw errorHandler.error(
                    stmtFor.iterator().name(),
                    ERR_INCORRECT_PARAMETER,
                    stmtFor.iterator().name().lexeme()
            );
        }
        nextScope();
        DeclVariable iterator = (DeclVariable) stmtFor.iterator().accept(this);
        Block body = (Block) stmtFor.body().accept(this);
        pop();
        return new StmtFor(iterator, Optional.of(list), body);
    }

    @Override
    public Record visit(StmtWhile stmtWhile) {
        Expr cond = (Expr) stmtWhile.cond().accept(this);
        Block body = (Block) stmtWhile.body().accept(this);
        return new StmtWhile(cond, body);
    }

    @Override
    public Record visit(StmtIf stmtIf) {
        Expr cond = (Expr) stmtIf.cond().accept(this);
        Block thenBody = (Block) stmtIf.thenBody().accept(this);
        Optional<Block> elseBody = Optional.empty();
        if (stmtIf.elseBody().isPresent()) {
            elseBody = Optional.of((Block) stmtIf.elseBody().get().accept(this));
        }
        return new StmtIf(cond, thenBody, elseBody);
    }

    @Override
    public Record visit(StmtReturn stmtReturn) {
        Expr expr = (Expr) stmtReturn.expr().accept(this);
        return new StmtReturn(expr);
    }

    @Override
    public Record visit(StmtExpr stmtExpr) {
        Expr expr = (Expr) stmtExpr.expr().accept(this);
        return new StmtExpr(expr);
    }

    @Override
    public Record visit(AssignExpr assignExpr) {
        Expr target = assignExpr.target();
        if (!isAssignableTarget(target)) {
            throw errorHandler.error(target.token(), ERR_INVALID_ASSIGNMENT_TARGET);
        }
        Expr resolvedTarget = (Expr) target.accept(this);
        Expr resolvedExpr = (Expr) assignExpr.expr().accept(this);
        Symbol targetSymbol = symbolOf(resolvedTarget);

        return new AssignExpr(
                resolvedTarget,
                resolvedExpr,
                targetSymbol.type(),
                targetSymbol
        );
    }

    @Override
    public Record visit(OrExpr orExpr) {
        Expr left = (Expr) orExpr.left().accept(this);
        Expr right = (Expr) orExpr.right().accept(this);
        return new OrExpr(left, right, orExpr.type());
    }

    @Override
    public Record visit(AndExpr andExpr) {
        Expr left = (Expr) andExpr.left().accept(this);
        Expr right = (Expr) andExpr.right().accept(this);
        return new AndExpr(left, right, andExpr.type());
    }

    @Override
    public Record visit(CompExpr compExpr) {
        Expr left = (Expr) compExpr.left().accept(this);
        Expr right = (Expr) compExpr.right().accept(this);
        return new CompExpr(left, right, compExpr.type());
    }

    @Override
    public Record visit(RelExpr relExpr) {
        Expr left = (Expr) relExpr.left().accept(this);
        Expr right = (Expr) relExpr.right().accept(this);
        return new RelExpr(left, right, relExpr.cond(), relExpr.type());
    }

    @Override
    public Record visit(AddExpr addExpr) {
        Expr left = (Expr) addExpr.left().accept(this);
        Expr right = (Expr) addExpr.right().accept(this);
        return new AddExpr(left, right, addExpr.operator(), addExpr.type());
    }

    @Override
    public Record visit(MulExpr mulExpr) {
        Expr left = (Expr) mulExpr.left().accept(this);
        Expr right = (Expr) mulExpr.right().accept(this);
        return new MulExpr(left, right, mulExpr.operator(), mulExpr.type());
    }

    @Override
    public Record visit(UnaryNegExpr unaryExpr) {
        Expr left = (Expr) unaryExpr.left().accept(this);
        return new UnaryNegExpr(left, unaryExpr.type());
    }

    @Override
    public Record visit(UnaryNotExpr unaryExpr) {
        Expr left = (Expr) unaryExpr.left().accept(this);
        return new UnaryNotExpr(left, unaryExpr.type());
    }

    @Override
    public Record visit(LiteralIdent identExpr) {
        Token name = identExpr.token();
        Optional<Symbol> symbol = lookFor(name.lexeme());
        if (symbol.isEmpty()) {
            throw errorHandler.error(name, ERR_UNDECLARED_IDENTIFIER, name.lexeme());
        }

        return new LiteralIdent(
                identExpr.token(),
                symbol.get().type(),
                symbol.get()
        );
    }

    @Override
    public Record visit(IntLitExpr intLitExpr) {
        return intLitExpr;
    }

    @Override
    public Record visit(FloatLitExpr floatLitExpr) {
        return floatLitExpr;
    }

    @Override
    public Record visit(CharLitExpr charLitExpr) {
        return charLitExpr;
    }

    @Override
    public Record visit(StringLitExpr stringLitExpr) {
        return stringLitExpr;
    }

    @Override
    public Record visit(CollectionLitExpr collectionLitExpr) {
        List<Expr> values = new ArrayList<>();
        for (Expr value : collectionLitExpr.value()) {
            values.add((Expr) value.accept(this));
        }

        return new CollectionLitExpr(
                collectionLitExpr.token(),
                values,
                collectionLitExpr.type()
        );
    }

    @Override
    public Record visit(RangeExpr rangeExpr) {
        Expr inf = (Expr) rangeExpr.inf().accept(this);
        Expr sup = (Expr) rangeExpr.sup().accept(this);
        return new RangeExpr(inf, sup, null);
    }

    @Override
    public Record visit(ArrayAccess arrayAccess) {
        Expr target = (Expr) arrayAccess.target().accept(this);
        Expr index = (Expr) arrayAccess.index().accept(this);
        String name = arrayAccess.target().token().lexeme();
        Optional<Symbol> symbol = lookFor(name);
        if (symbol.isEmpty()) {
            throw errorHandler.error(arrayAccess.token(), ERR_UNDECLARED_IDENTIFIER, name);
        }

        return new ArrayAccess(
                target,
                index,
                symbol.get().type(),
                symbol.get()
        );
    }

    @Override
    public Record visit(FuncCall funcCall) {
        Token name = funcCall.token();
        if (!(funcCall.name() instanceof LiteralIdent ident)) {
            throw errorHandler.error(
                    name,
                    ERR_INVALID_FUNCTION_CALL,
                    name.lexeme()
            );
        }
        
        Token functionName = ident.token();
        Optional<Symbol> symbol = lookFor(functionName.lexeme());
        if (symbol.isEmpty()) {
            throw errorHandler.error(
                    functionName,
                    ERR_UNDECLARED_FUNCTION,
                    functionName.lexeme()
            );
        }

        if (symbol.get().category() != Category.FUNCTION) {
            throw errorHandler.error(
                    functionName,
                    ERR_NOT_A_FUNCTION,
                    functionName.lexeme()
            );
        }

        Expr resolvedName = new LiteralIdent(
                functionName,
                symbol.get().type(),
                symbol.get()
        );
        List<Expr> args = new ArrayList<>();
        for (Expr arg : funcCall.args()) {
            args.add((Expr) arg.accept(this));
        }

        return new FuncCall(
                resolvedName,
                args,
                symbol.get().type(),
                symbol.get()
        );
    }

    @Override
    public Record visit(PopExpr popExpr) {
        Token target = popExpr.token();
        Optional<Symbol> symbol = lookFor(target.lexeme());
        if (symbol.isEmpty()) {
            throw errorHandler.error(target, ERR_UNDECLARED_IDENTIFIER, target.lexeme());
        }
        Expr name = (Expr) popExpr.name().accept(this);

        return new PopExpr(
                name,
                symbol.get().type(),
                symbol.get()
        );
    }

    @Override
    public Record visit(PushExpr pushExpr) {
        Token target = pushExpr.token();
        Optional<Symbol> symbol = lookFor(target.lexeme());
        if (symbol.isEmpty()) {
            throw errorHandler.error(target, ERR_UNDECLARED_IDENTIFIER, target.lexeme());
        }

        Expr name = (Expr) pushExpr.name().accept(this);
        Expr value = (Expr) pushExpr.value().accept(this);

        return new PushExpr(
                name,
                value,
                symbol.get().type(),
                symbol.get()
        );
    }

    @Override
    public Record visit(PrimitiveType primitiveType) {
        return primitiveType;
    }

    @Override
    public Record visit(ListType listType) {
        return new ListType((Type) listType.innerType().accept(this));
    }

    @Override
    public Record visit(ArrayType arrayType) {
        List<Optional<Expr>> dimensions = new ArrayList<>();
        for (Optional<Expr> dimension : arrayType.dimensions()) {
            if (dimension.isPresent()) {
                dimensions.add(Optional.of((Expr) dimension.get().accept(this)));
            } else {
                dimensions.add(Optional.empty());
            }
        }

        return new ArrayType(
                (Type) arrayType.baseType().accept(this),
                dimensions
        );
    }

    private void declareFunctionHeader(FuncDecl functionDeclaration) {
        Token functionName = functionDeclaration.name();
        if (lookCurrent(functionName.lexeme())) {
            throw errorHandler.error(
                    functionName,
                    ERR_REDECLARED_IDENTIFIER,
                    functionName.lexeme()
            );
        }

        Symbol functionSymbol = new Symbol(
                functionName.lexeme(),
                functionDeclaration.funcType(),
                Category.FUNCTION
        );
        currentScope.add(functionSymbol);
    }

    private Symbol symbolOf(Expr expr) {
        if (expr instanceof LiteralIdent ident) {
            return ident.symbol();
        }
        if (expr instanceof ArrayAccess access) {
            return access.symbol();
        }
        if (expr instanceof FuncCall call) {
            return call.symbol();
        }
        if (expr instanceof AssignExpr assignExpr) {
            return assignExpr.symbol();
        }
        if (expr instanceof PushExpr pushExpr) {
            return pushExpr.symbol();
        }
        if (expr instanceof PopExpr popExpr) {
            return popExpr.symbol();
        }

        throw errorHandler.error(
                expr.token(),
                ERR_UNDECLARED_IDENTIFIER,
                expr.token().lexeme()
        );
    }

    private void nextScope() {
        currentScope = new Scope(currentScope);
    }

    private void pop() {
        currentScope = currentScope.getParent();
    }

    private boolean lookCurrent(String name) {
        return currentScope != null && currentScope.contains(name);
    }

    private Optional<Symbol> lookFor(String name) {
        Scope temp = currentScope;
        while (temp != null) {
            if (temp.contains(name)) {
                return Optional.ofNullable(temp.get(name));
            }
            temp = temp.getParent();
        }
        return Optional.empty();
    }

    private boolean isAssignableTarget(Expr expr) {
        return expr instanceof LiteralIdent || expr instanceof ArrayAccess;
    }
}
