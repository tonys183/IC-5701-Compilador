package edu.ic5701.sem;

import edu.ic5701.ast.Program;
import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.expr.*;
import edu.ic5701.ast.stmt.*;
import edu.ic5701.ast.type.ArrayType;
import edu.ic5701.ast.type.ListType;
import edu.ic5701.ast.type.PrimitiveType;
import edu.ic5701.ast.type.Type;
import edu.ic5701.error.ErrorHandler;
import edu.ic5701.error.TokenErrorHandler;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;
import edu.ic5701.sem.comparator.SemanticTypeComparator;
import edu.ic5701.sem.comparator.TypeComparator;
import edu.ic5701.sem.factory.PrimitiveFactory;
import edu.ic5701.sem.rules.ArithmeticRules;
import edu.ic5701.sem.rules.ExpressionTypeResolver;
import edu.ic5701.sem.rules.TypeResolver;
import edu.ic5701.sem.table.Symbol;

import java.util.*;

public final class SemanticTypeChecker implements Visitor<Record> {

    private static final String ERR_INCOMPATIBLE_DECL_OP =
            "The identificators '%s' and '%s' have not been declared with the same data type";
    private static final String ERR_INCOMPATIBLE_LITERAL_OP =
            "The values '%s' and '%s' must be the same type for this operation";
    private static final String ERR_FOR_INVALID_COLLECTION =
            "The literal or identifier '%s' is invalid in the for stament";
    private static final String ERR_DIFF_AMOUNT_ARGS_IN_FUNCALL =
            "The amount of args in the function call is invalid. Expected '%s' but got '%s'";
    private static final String ERR_MAIN_INVALID_TYPE =
            "The main function must be declared as Int and is currently declared as '%s'";

    private final Map<String, FuncDecl> declFunctions;
    private final ArithmeticRules arithmeticRules;
    private final TokenErrorHandler errorHandler;
    private final TypeResolver typeResolver;
    private final TypeComparator typeComparator;
    private final PrimitiveFactory primitiveFactory;
    private FuncDecl currentFunc;

    public SemanticTypeChecker(
            ArithmeticRules arithmeticRules,
            TokenErrorHandler errorHandler,
            PrimitiveFactory primitiveFactory
    ) {
        this.arithmeticRules = arithmeticRules;
        this.errorHandler = errorHandler;
        this.primitiveFactory = primitiveFactory;
        this.typeResolver = new ExpressionTypeResolver(primitiveFactory, this, errorHandler);
        this.typeComparator = new SemanticTypeComparator(typeResolver, this, errorHandler);
        this.declFunctions = new HashMap<>();
    }

    @Override
    public Record visit(Program program) {
        for (FuncDecl function : program.body()) {
            declFunctions.put(function.name().lexeme(), function);
        }

        List<FuncDecl> functions = new ArrayList<>();
        for (FuncDecl function : program.body()) {
            currentFunc = function;
            functions.add((FuncDecl) function.accept(this));
        }

        return new Program(functions);
    }

    @Override
    public Record visit(FuncDecl functionDeclaration) {
        if (functionDeclaration.name().type().equals(TokenType.KEYWORD_MAIN)
                && !functionDeclaration.funcType().token().type().equals(TokenType.KEYWORD_INT)) {
            throw errorHandler.error(
                    functionDeclaration.name(),
                    ERR_MAIN_INVALID_TYPE,
                    functionDeclaration.funcType().token().lexeme()
            );
        }

        ParamDecl params = (ParamDecl) functionDeclaration.param().accept(this);
        Block body = (Block) functionDeclaration.body().accept(this);

        return new FuncDecl(
                functionDeclaration.funcType(),
                params,
                functionDeclaration.name(),
                body
        );
    }

    @Override
    public Record visit(ParamDecl paramDecl) {
        List<DeclVariable> declarations = new ArrayList<>();

        for (DeclVariable param : paramDecl.params()) {
            declarations.add((DeclVariable) param.accept(this));
        }

        return new ParamDecl(declarations);
    }

    @Override
    public Record visit(Block block) {
        List<Stmt> statements = new ArrayList<>();

        for (Stmt stmt : block.statements()) {
            statements.add((Stmt) stmt.accept(this));
        }

        return new Block(statements);
    }

    @Override
    public Record visit(DeclVariable variableDeclaration) {
        Optional<Expr> val = Optional.empty();

        Type declType = variableDeclaration.type();

        if (declType instanceof ArrayType array) {
            List<Optional<Expr>> dimensions = new ArrayList<>();

            for (Optional<Expr> value : array.dimensions()) {
                dimensions.add(value.map(this::checkForDimension));
            }

            declType = new ArrayType(array.baseType(), dimensions);
        }

        if (variableDeclaration.assignValue().isPresent()) {
            Expr value = (Expr) variableDeclaration.assignValue().get().accept(this);
            Type varType = variableDeclaration.symbol().type();
            typeComparator.compare(varType, value);

            val = Optional.of(value);
        }

        return new DeclVariable(
                declType,
                variableDeclaration.name(),
                val,
                variableDeclaration.symbol()
        );
    }

    @Override
    public Record visit(StmtPrint stmtPrint) {
        return new StmtPrint((Expr) stmtPrint.expr().accept(this));
    }

    @Override
    public Record visit(StmtFor stmtFor) {
        Token iteratorVal = stmtFor.iterator().name();

        DeclVariable iterator = (DeclVariable) stmtFor.iterator().accept(this);
        Type iteratorType = stmtFor.iterator().symbol().type();

        Expr listVal = (Expr) stmtFor.list().get().accept(this);

        validateForCollection(listVal, iteratorType, iteratorVal);

        Block body = (Block) stmtFor.body().accept(this);

        return new StmtFor(iterator, Optional.of(listVal), body);
    }

    @Override
    public Record visit(StmtWhile stmtWhile) {
        Expr cond = (Expr) stmtWhile.cond().accept(this);

        comparePrimitive(cond, TokenType.KEYWORD_INT);

        Block body = (Block) stmtWhile.body().accept(this);

        return new StmtWhile(cond, body);
    }

    @Override
    public Record visit(StmtIf stmtIf) {
        Expr cond = (Expr) stmtIf.cond().accept(this);

        comparePrimitive(cond, TokenType.KEYWORD_INT);

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

        typeComparator.compare(currentFunc.funcType(), expr);

        return new StmtReturn(expr);
    }

    @Override
    public Record visit(StmtExpr stmtExpr) {
        return stmtExpr.expr().accept(this);
    }

    @Override
    public Record visit(AssignExpr assignExpr) {
        Expr target = (Expr) assignExpr.target().accept(this);
        Expr expr = (Expr) assignExpr.expr().accept(this);

        Symbol targetSymbol = target.symbol();
        Type targetType = typeResolver.typeAfterAccess(targetSymbol.type(), target);

        typeComparator.compare(targetType, expr);

        return new AssignExpr(target, expr, targetType, targetSymbol);
    }

    @Override
    public Record visit(OrExpr orExpr) {
        Expr left = (Expr) orExpr.left().accept(this);
        Expr right = (Expr) orExpr.right().accept(this);

        comparePrimitive(left, TokenType.KEYWORD_INT);
        comparePrimitive(right, TokenType.KEYWORD_INT);

        typeComparator.compare(left, right);

        return new OrExpr(
                left,
                right,
                primitiveFactory.createPrimitive(TokenType.KEYWORD_INT, orExpr.token())
        );
    }

    @Override
    public Record visit(AndExpr andExpr) {
        Expr left = (Expr) andExpr.left().accept(this);
        Expr right = (Expr) andExpr.right().accept(this);

        comparePrimitive(left, TokenType.KEYWORD_INT);
        comparePrimitive(right, TokenType.KEYWORD_INT);

        typeComparator.compare(left, right);

        return new AndExpr(
                left,
                right,
                primitiveFactory.createPrimitive(TokenType.KEYWORD_INT, andExpr.token())
        );
    }

    @Override
    public Record visit(CompExpr compExpr) {
        Expr left = (Expr) compExpr.left().accept(this);
        Expr right = (Expr) compExpr.right().accept(this);

        comparePrimitive(
                left,
                TokenType.KEYWORD_INT,
                TokenType.KEYWORD_FLOAT,
                TokenType.KEYWORD_CHAR
        );

        typeComparator.compare(left, right);

        return new CompExpr(
                left,
                right,
                primitiveFactory.createPrimitive(TokenType.KEYWORD_INT, compExpr.token())
        );
    }

    @Override
    public Record visit(RelExpr relExpr) {
        Expr left = (Expr) relExpr.left().accept(this);
        Expr right = (Expr) relExpr.right().accept(this);

        comparePrimitive(
                left,
                TokenType.KEYWORD_INT,
                TokenType.KEYWORD_FLOAT,
                TokenType.KEYWORD_CHAR
        );

        typeComparator.compare(left, right);

        return new RelExpr(
                left,
                right,
                relExpr.cond(),
                primitiveFactory.createPrimitive(TokenType.KEYWORD_INT, relExpr.token())
        );
    }

    @Override
    public Record visit(AddExpr addExpr) {
        Expr left = (Expr) addExpr.left().accept(this);
        Expr right = (Expr) addExpr.right().accept(this);

        Type leftType = typeResolver.typeOf(left);
        Type rightType = typeResolver.typeOf(right);

        comparePrimitive(
                leftType,
                left.token(),
                TokenType.KEYWORD_INT,
                TokenType.KEYWORD_FLOAT,
                TokenType.KEYWORD_CHAR
        );

        comparePrimitive(
                rightType,
                right.token(),
                TokenType.KEYWORD_INT,
                TokenType.KEYWORD_FLOAT,
                TokenType.KEYWORD_CHAR
        );

        Type resultType = arithmeticRules.additive(leftType, rightType, addExpr.operator());

        return new AddExpr(left, right, addExpr.operator(), resultType);
    }

    @Override
    public Record visit(MulExpr mulExpr) {
        Expr left = (Expr) mulExpr.left().accept(this);
        Expr right = (Expr) mulExpr.right().accept(this);

        Type leftType = typeResolver.typeOf(left);
        Type rightType = typeResolver.typeOf(right);

        if (mulExpr.operator().type().equals(TokenType.OP_MOD)) {
            comparePrimitive(leftType, left.token(), TokenType.KEYWORD_INT);
            typeComparator.compare(left, right);

            return new MulExpr(
                    left,
                    right,
                    mulExpr.operator(),
                    primitiveFactory.createPrimitive(TokenType.KEYWORD_INT, mulExpr.token())
            );
        }

        comparePrimitive(
                leftType,
                left.token(),
                TokenType.KEYWORD_INT,
                TokenType.KEYWORD_FLOAT
        );

        comparePrimitive(
                rightType,
                right.token(),
                TokenType.KEYWORD_INT,
                TokenType.KEYWORD_FLOAT
        );

        return new MulExpr(
                left,
                right,
                mulExpr.operator(),
                arithmeticRules.multiplicative(leftType, rightType, mulExpr.operator())
        );
    }

    @Override
    public Record visit(UnaryNegExpr unaryExpr) {
        Expr left = (Expr) unaryExpr.left().accept(this);

        Type leftType = typeResolver.typeOf(left);

        comparePrimitive(
                leftType,
                left.token(),
                TokenType.KEYWORD_INT,
                TokenType.KEYWORD_FLOAT
        );

        return new UnaryNegExpr(left, leftType);
    }

    @Override
    public Record visit(UnaryNotExpr unaryExpr) {
        Expr left = (Expr) unaryExpr.left().accept(this);

        comparePrimitive(left, TokenType.KEYWORD_INT);

        return new UnaryNotExpr(
                left,
                primitiveFactory.createPrimitive(TokenType.KEYWORD_INT, unaryExpr.token())
        );
    }

    @Override
    public Record visit(LiteralIdent identExpr) {
        Symbol symbol = identExpr.symbol();

        return new LiteralIdent(
                identExpr.token(),
                symbol.type(),
                symbol
        );
    }

    @Override
    public Record visit(IntLitExpr intLitExpr) {
        return new IntLitExpr(
                intLitExpr.intToken(),
                intLitExpr.value()
        );
    }

    @Override
    public Record visit(FloatLitExpr floatLitExpr) {
        return new FloatLitExpr(
                floatLitExpr.floatToken(),
                floatLitExpr.value()
        );
    }

    @Override
    public Record visit(CharLitExpr charLitExpr) {
        return new CharLitExpr(
                charLitExpr.charToken(),
                charLitExpr.value()
        );
    }

    @Override
    public Record visit(StringLitExpr stringLitExpr) {
        return new StringLitExpr(
                stringLitExpr.token(),
                stringLitExpr.value()
        );
    }

    @Override
    public Record visit(CollectionLitExpr collectionLitExpr) {
        List<Expr> values = new ArrayList<>();
        Type collectionType = collectionLitExpr.type();

        if (!collectionLitExpr.value().isEmpty()) {
            Expr first = (Expr) collectionLitExpr.value().getFirst().accept(this);
            Type firstType = typeResolver.typeOf(first);

            values.add(first);
            collectionType = firstType;

            for (int i = 1; i < collectionLitExpr.value().size(); i++) {
                Expr current = (Expr) collectionLitExpr.value().get(i).accept(this);
                Type currentType = typeResolver.typeOf(current);

                if (!typeComparator.isSameType(firstType, currentType)) {
                    throw errorHandler.error(
                            current.token(),
                            ERR_INCOMPATIBLE_LITERAL_OP,
                            first.token().lexeme(),
                            current.token().lexeme()
                    );
                }

                values.add(current);
            }
        }

        return new CollectionLitExpr(
                collectionLitExpr.token(),
                values,
                collectionType
        );
    }

    @Override
    public Record visit(RangeExpr rangeExpr) {
        Expr inf = (Expr) rangeExpr.inf().accept(this);
        Expr sup = (Expr) rangeExpr.sup().accept(this);

        comparePrimitive(
                inf,
                TokenType.KEYWORD_INT,
                TokenType.KEYWORD_FLOAT,
                TokenType.KEYWORD_CHAR
        );

        typeComparator.compare(inf, sup);

        Type rangeType = typeResolver.typeOf(inf);
        return new RangeExpr(inf, sup, new ArrayType(rangeType, List.of(Optional.empty())));
    }

    @Override
    public Record visit(ArrayAccess arrayAccess) {
        Expr target = (Expr) arrayAccess.target().accept(this);
        Expr index = (Expr) arrayAccess.index().accept(this);

        comparePrimitive(index, TokenType.KEYWORD_INT);

        Symbol symbol = arrayAccess.symbol();
        Type accessType = typeResolver.typeAfterAccess(symbol.type(), arrayAccess);

        return new ArrayAccess(
                target,
                index,
                accessType,
                symbol
        );
    }

    @Override
    public Record visit(FuncCall funcCall) {
        Expr name = (Expr) funcCall.name().accept(this);

        Symbol func = funcCall.symbol();
        Token funcIdent = funcCall.token();

        ParamDecl funcParam = declFunctions.get(func.name()).param();

        if (funcCall.args().size() != funcParam.params().size()) {
            throw errorHandler.error(
                    funcIdent,
                    ERR_DIFF_AMOUNT_ARGS_IN_FUNCALL,
                    funcParam.params().size(),
                    funcCall.args().size()
            );
        }

        List<Expr> newArgs = new ArrayList<>();

        for (int i = 0; i < funcCall.args().size(); i++) {
            Expr value = (Expr) funcCall.args().get(i).accept(this);
            Type expectedParamType = funcParam.params().get(i).type();

            typeComparator.compare(expectedParamType, value);

            newArgs.add(value);
        }

        return new FuncCall(
                name,
                newArgs,
                func.type(),
                func
        );
    }

    @Override
    public Record visit(PopExpr popExpr) {
        Expr name = (Expr) popExpr.name().accept(this);

        Symbol symbol = name.symbol();

        Type type;
        if (symbol.type() instanceof ListType listType) {
            type = listType.innerType();
        }
        else if (symbol.type().token().type().equals(TokenType.KEYWORD_STRING)) {
            type = primitiveFactory.createPrimitive(TokenType.KEYWORD_CHAR, name.token());
        }
        else {
            type = symbol.type();
        }

        return new PopExpr(name, type, symbol);
    }

    @Override
    public Record visit(PushExpr pushExpr) {
        Expr name = (Expr) pushExpr.name().accept(this);

        Symbol list = name.symbol();

        Expr value = (Expr) pushExpr.value().accept(this);

        Type listType = list.type();

        if (listType instanceof ListType listTypeNode) {
            typeComparator.compare(listTypeNode.innerType(), value);
        }
        else if (listType.token().type().equals(TokenType.KEYWORD_STRING)) {
            Type type = primitiveFactory.createPrimitive(TokenType.KEYWORD_CHAR, name.token());
            typeComparator.compare(type, value);
        }
        else {
            typeComparator.compare(listType, value);
        }

        return new PushExpr(
                name,
                value,
                listType,
                list
        );
    }

    @Override
    public Record visit(PrimitiveType primitiveType) {
        return new PrimitiveType(primitiveType.token());
    }

    @Override
    public Record visit(ListType listType) {
        return new ListType(listType.innerType());
    }

    @Override
    public Record visit(ArrayType arrayType) {
        return new ArrayType(
                arrayType.baseType(),
                arrayType.dimensions()
        );
    }

    private void validateForCollection(Expr listVal, Type iteratorType, Token iteratorVal) {
        if (listVal.symbol() != null) {
            Symbol listSymbol = listVal.symbol();

            Type collectionType = listSymbol.type();

            int collectionDimension = collectionType.dimensionSizeOf();
            int access = typeResolver.accessDimension(listVal);

            if (collectionDimension == 0 || access != collectionDimension - 1) {
                throw errorHandler.error(
                        collectionType.token(),
                        ERR_FOR_INVALID_COLLECTION,
                        listVal
                );
            }

            Type elementType = typeResolver.typeAfterAccess(collectionType, listVal);
            boolean isPrimitiveElement = elementType instanceof PrimitiveType;

            boolean isInvalidArrayElement = elementType instanceof ArrayType arrayType
                    && (!typeComparator.isSameType(iteratorType, arrayType.baseType())
                    || arrayType.dimensionSizeOf() > 1);

            boolean isInvalidListElement = elementType instanceof ListType listTypeNode
                    && !typeComparator.isSameType(listTypeNode.innerType(), iteratorType);

            if (isPrimitiveElement || isInvalidArrayElement || isInvalidListElement) {
                throw errorHandler.error(
                        iteratorType.token(),
                        ERR_INCOMPATIBLE_DECL_OP,
                        iteratorType.token().lexeme(),
                        listSymbol.name()
                );
            }

        } else if (listVal instanceof RangeExpr rangeExpr) {
            typeComparator.compare(iteratorType, rangeExpr.inf());
        } else if (listVal instanceof CollectionLitExpr collectionLitExpr) {
            typeComparator.compare(iteratorType, collectionLitExpr);
        } else {
            throw errorHandler.error(
                    iteratorVal,
                    ERR_FOR_INVALID_COLLECTION,
                    listVal
            );
        }
    }

    private Expr checkForDimension(Expr expr) {
        expr = (Expr) expr.accept(this);
        comparePrimitive(expr, TokenType.KEYWORD_INT);
        return expr;
    }

    private void comparePrimitive(Expr expr, TokenType... tokenTypes) {
        Type type = typeResolver.typeOf(expr);
        comparePrimitive(type, expr.token(), tokenTypes);
    }

    private void comparePrimitive(Type type, Token token, TokenType... tokenTypes) {
        PrimitiveType[] primitiveTypes = new PrimitiveType[tokenTypes.length];
        for (int i = 0; i < tokenTypes.length; i++) {
            primitiveTypes[i] = primitiveFactory.createPrimitive(tokenTypes[i], token);
        }
        typeComparator.compare(type, primitiveTypes);
    }
}
