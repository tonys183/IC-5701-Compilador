package edu.ic5701.gen;

import edu.ic5701.ast.Program;
import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.expr.*;
import edu.ic5701.ast.stmt.*;
import edu.ic5701.ast.type.ArrayType;
import edu.ic5701.ast.type.ListType;
import edu.ic5701.ast.type.PrimitiveType;
import edu.ic5701.ast.type.Type;
import edu.ic5701.scanner.TokenType;
import edu.ic5701.sem.table.Symbol;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class StackvmCodeGenerator implements Visitor<Void> {

    private static final String FUNCTION_DEF = "\n.def function_%s: args = %d, locals = %d\n";
    private static final String FUNCTION_CALL = "call function_%s()\n";
    private static final String FUNCTION_MAIN = "\n.def main: args=0, locals=0\ncall function_main()\nhalt\n";
    private static final String LABEL = "label_%s_%d";
    private static final String IF_CONDITION = "call truthy()\niconst 1\nieq\njmpt %s\n";
    private static final String INSERT_COLLECTION = "iconst %d\niconst %d\nisub\nhstore %d\n";
    private static final String RANGE_LOOP_CHECK = "iconst 1\niadd\ndup\nload %d\nhlen \nilt\njmpt %s\n";
    private static final String NAME_FILE_FUNCTIONS_AUX = "/funAux.stkasm";
    private static final String FOR_COND_LOOP = "load %d\nload %d\n%slen\ncall igeq()\njmpt %s\n";
    private static final String FOR_LOOP_PART = " load %d\niconst 1\niadd\nstore %d\njmp %s\n";
    private static final String JUMP_TO_LABEL = "jmp %s\n";
    private static final String JUMP_TRUE_LABEL = "jmpt %s\n";

    private static final String INT_TO_FLOAT = "itof\n";
    private static final String CHAR_TO_INT = "ctoi\n";
    private static final String SWAP = "swap\n";
    private static final String CONST = "%sconst %s\n";
    private static final String ADD = "%sadd\n";
    private static final String SUB = "%ssub\n";
    private static final String MUL = "%smul\n";
    private static final String MOD = "imod\n";
    private static final String EQ = "%seq\n";
    private static final String LESS_THAN = "%slt\n";
    private static final String LIST_LEN = "%slen\n";
    private static final String SGET = "sget\n";
    private static final String HSTORE = "hstore %d\n";
    private static final String HLOAD = "h%sload %d\n";
    private static final String STORE = "store %d\n";
    private static final String RETURN = "ret\n";
    private static final String LOAD = "load %d\n";
    private static final String PRINT = "print\n";
    private static final String ALLOC = "alloc\n";
    public static final String POP = "pop\n";
    private static final String DUP = "dup\n";
    private static final String REALLOC = "call %srealloc()\n";

    private static final String GREATER_THAN = "call %sgt()\n";
    private static final String LESS_EQUAL = "call %sleq()\n";
    private static final String GREATER_EQUAL = "call %sgeq()\n";
    private static final String INT_TO_CHAR = "call itoc()\n";
    private static final String TRUTHY = "call truthy()\n";
    private static final String DIV = "call %sdiv()\n";
    private static final String FLOAT_TO_INT = "call ftoi()\n";

    private static final String NEWLINE = "\n";
    private static final String DOTS = ":\n";

    private final VariableResolver resolver;
    private final Writer stackvmWriter;
    private String currentFunction;
    private int currentLabel = 0;

    public StackvmCodeGenerator(VariableResolver resolver, Writer stackvmWriter) {
        this.resolver = resolver;
        this.stackvmWriter = stackvmWriter;
    }

    public void solve(Program program) {
        System.out.println("[GEN] solve(Program) -> adjuntando funciones auxiliares");
        attachFunctionsAux();
        program.accept(this);
        stackvmWriter.close();
    }

    @Override
    public Void visit(Program program) {
        System.out.println("[GEN] visit(Program)");

        for (FuncDecl function : program.body()) {
            currentFunction = function.name().lexeme();
            function.accept(this);
        }

        stackvmWriter.write(FUNCTION_MAIN);
        return null;
    }

    @Override
    public Void visit(FuncDecl functionDeclaration) {
        System.out.println("[GEN] visit(FuncDecl): " + functionDeclaration.name().lexeme());
        functionDeclaration.param().accept(this);
        functionDeclaration.funcType().accept(this);
        stackvmWriter.write(String.format(
                FUNCTION_DEF,
                functionDeclaration.name().lexeme(),
                resolver.parameters().get(currentFunction).size(),
                resolver.locals().get(currentFunction).size()
        ));
        functionDeclaration.body().accept(this);
        stackvmWriter.write(RETURN);
        return null;
    }

    @Override
    public Void visit(ParamDecl paramDecl) {
        System.out.println("[GEN] visit(ParamDecl): cantidad=" + paramDecl.params().size());
        for (DeclVariable param : paramDecl.params()) {
            param.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DeclVariable variableDeclaration) {
        System.out.println("[GEN] visit(DeclVariable): " + variableDeclaration.name().lexeme());
        final int storeValue = storeLocal(variableDeclaration.symbol());
        Optional<Expr> assignValue = variableDeclaration.assignValue();
        if (!(variableDeclaration.type() instanceof PrimitiveType)) {
            stackvmWriter.write(CONST.formatted("i", 0));
            stackvmWriter.write(ALLOC);
            stackvmWriter.write(STORE.formatted(storeValue));
        }
        if (assignValue.isPresent()) {
            assignValue.get().accept(this);
            stackvmWriter.write(STORE.formatted(storeValue));
        }

        return null;
    }

    @Override
    public Void visit(Block block) {
        System.out.println("[GEN] visit(Block): statements=" + block.statements().size());
        for (Stmt statement : block.statements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(StmtReturn stmtReturn) {
        System.out.println("[GEN] visit(StmtReturn)");
        stmtReturn.expr().accept(this);
        stackvmWriter.write(RETURN);
        return null;
    }

    @Override
    public Void visit(StmtFor stmtFor) {
        System.out.println("[GEN] visit(StmtFor): iterator=" + stmtFor.iterator().name().lexeme());
        stmtFor.iterator().accept(this);
        stmtFor.list().ifPresent(list -> list.accept(this));
        final int indexStoreValue = storeLocal(stmtFor.iterator());
        final int indexLoadValue = loadIndex(stmtFor.iterator());
        final int iteratorStoreValue = loadIndex(stmtFor.iterator().symbol());
        int listLoadIndex = loadIndex(stmtFor);
        int listStoreIndex = storeLocal(stmtFor);
        String condLabel = createLabel("for_cond");
        String endLabel = createLabel("for_end");

        stackvmWriter.write(STORE.formatted(listStoreIndex));
        stackvmWriter.write(CONST.formatted("i", 0));
        stackvmWriter.write(STORE.formatted(indexStoreValue));
        stackvmWriter.write(condLabel + DOTS);
        stackvmWriter.write(FOR_COND_LOOP.
                formatted(indexLoadValue,
                        listLoadIndex,
                        compareTypes(stmtFor.list().get().type()),
                        endLabel
                )
        );
        stackvmWriter.write(LOAD.formatted(indexLoadValue));
        TokenType listType = typeOf(stmtFor.list().get());
        if (listType.equals(TokenType.KEYWORD_STRING)) {
            stackvmWriter.write(HLOAD.formatted("c", listLoadIndex));
        }
        else {
            stackvmWriter.write(HLOAD.formatted(compareTypes(listType), listLoadIndex));
        }
        stackvmWriter.write(STORE.formatted(iteratorStoreValue));
        stmtFor.body().accept(this);
        stackvmWriter.write(FOR_LOOP_PART.formatted(indexLoadValue, indexStoreValue, condLabel));
        stackvmWriter.write(endLabel + DOTS);

        return null;
    }

    @Override
    public Void visit(StmtIf stmtIf) {
        System.out.println("[GEN] visit(StmtIf)");
        stmtIf.cond().accept(this);

        final String thenLabel = createLabel("then");
        final String endIf = createLabel("end_if");
        stackvmWriter.write(IF_CONDITION.formatted(thenLabel));

        if (stmtIf.elseBody().isPresent()) {
            final String elseLabel = createLabel("else");
            stackvmWriter.write(JUMP_TO_LABEL.formatted(elseLabel));
            stackvmWriter.write(thenLabel + DOTS);
            stmtIf.thenBody().accept(this);
            stackvmWriter.write(JUMP_TO_LABEL.formatted(endIf));
            stackvmWriter.write(elseLabel + DOTS);
            stmtIf.elseBody().get().accept(this);

        } else {
            stackvmWriter.write(JUMP_TO_LABEL.formatted(endIf));
            stackvmWriter.write(thenLabel + DOTS);
            stmtIf.thenBody().accept(this);
        }

        stackvmWriter.write(endIf + DOTS);

        return null;
    }

    @Override
    public Void visit(StmtWhile stmtWhile) {
        System.out.println("[GEN] visit(StmtWhile)");
        final String condLabel = createLabel("while_cond");
        final String whileBodyLabel = createLabel("while_body");
        final String endLabel = createLabel("end_while");

        stackvmWriter.write(condLabel + DOTS);
        stmtWhile.cond().accept(this);

        stackvmWriter.write(IF_CONDITION.formatted(whileBodyLabel));
        stackvmWriter.write(JUMP_TO_LABEL.formatted(endLabel));
        stackvmWriter.write(whileBodyLabel + DOTS);
        stmtWhile.body().accept(this);
        stackvmWriter.write(JUMP_TO_LABEL.formatted(condLabel));
        stackvmWriter.write(endLabel + DOTS);
        return null;
    }

    @Override
    public Void visit(StmtPrint stmtPrint) {
        System.out.println("[GEN] visit(StmtPrint)");
        stmtPrint.expr().accept(this);
        stackvmWriter.write(PRINT);
        return null;
    }

    @Override
    public Void visit(StmtExpr stmtExpr) {
        System.out.println("[GEN] visit(StmtExpr)");
        stmtExpr.expr().accept(this);
        return null;
    }

    @Override
    public Void visit(AssignExpr assignExpr) {
        System.out.println("[GEN] visit(AssignExpr): target=" + assignExpr.target());
        if (!(assignExpr.target() instanceof ArrayAccess access)) {
            assignExpr.expr().accept(this);

            final Record varName = assignExpr.target().symbol();
            stackvmWriter.write(STORE.formatted(storeLocal(varName)));

            return null;
        }
        if (access.target() instanceof LiteralIdent) {
            assignExpr.expr().accept(this);
            access.index().accept(this);

            stackvmWriter.write(HSTORE.formatted(
                    storeLocal(access.target().symbol())
            ));

            return null;
        }

        stackvmWriter.write(STORE.formatted(storeLocal(access)));
        assignExpr.expr().accept(this);
        access.index().accept(this);
        stackvmWriter.write(HSTORE.formatted(storeLocal(access)));

        return null;
    }

    @Override
    public Void visit(PushExpr pushExpr) {
        System.out.println("[GEN] visit(PushExpr): name=" + pushExpr.name());
        pushExpr.value().accept(this);
        pushExpr.name().accept(this);
        Type pushType = pushExpr.name().type();
        final Symbol name = pushExpr.name().symbol();
        stackvmWriter.write(DUP);
        stackvmWriter.write(LIST_LEN.formatted(compareTypes(pushType)));
        stackvmWriter.write(CONST.formatted("i", 1));
        stackvmWriter.write(ADD.formatted("i"));
        if (!(pushType instanceof PrimitiveType primitiveList)) {
            stackvmWriter.write(REALLOC.formatted("i"));
        } else {
            stackvmWriter.write(REALLOC.formatted(
                    name.type().token().type().equals(TokenType.KEYWORD_STRING) ?
                            "c" : compareTypes(primitiveList.token().type())
            ));
        }
        stackvmWriter.write(STORE.formatted(storeLocal(name)));
        stackvmWriter.write(LOAD.formatted(loadIndex(name)));
        stackvmWriter.write(LIST_LEN.formatted(compareTypes(pushType)));
        stackvmWriter.write(CONST.formatted("i", 1));
        stackvmWriter.write(SUB.formatted("i"));
        stackvmWriter.write(HSTORE.formatted(storeLocal(name)));
        stackvmWriter.write(LOAD.formatted(loadIndex(name)));
        return null;
    }

    @Override
    public Void visit(PopExpr popExpr) {
        System.out.println("[GEN] visit(PopExpr): name=" + popExpr.name());
        popExpr.name().accept(this);
        Type popType = popExpr.name().type();
        stackvmWriter.write(LOAD.formatted(loadIndex(popExpr.name().symbol())));
        stackvmWriter.write(DUP);
        stackvmWriter.write(LIST_LEN.formatted(compareTypes(popType)));
        stackvmWriter.write(CONST.formatted("i", 1));
        stackvmWriter.write(SUB.formatted("i"));
        stackvmWriter.write(REALLOC.formatted(compareTypes(popType.token().type())));
        stackvmWriter.write(SWAP);
        stackvmWriter.write(LIST_LEN.formatted(compareTypes(popType)));
        stackvmWriter.write(CONST.formatted("i", 1));
        stackvmWriter.write(SUB.formatted("i"));
        stackvmWriter.write(HLOAD.formatted(compareTypes(popType.token().type()),loadIndex(popExpr.name().symbol())));
        stackvmWriter.write(SWAP);
        stackvmWriter.write(STORE.formatted(storeLocal(popExpr.name().symbol())));
        return null;
    }

    @Override
    public Void visit(OrExpr orExpr) {
        System.out.println("[GEN] visit(OrExpr)");
        orExpr.left().accept(this);
        orExpr.right().accept(this);
        stackvmWriter.write(TRUTHY);
        stackvmWriter.write(SWAP);
        stackvmWriter.write(TRUTHY);
        stackvmWriter.write(ADD.formatted("i"));
        stackvmWriter.write(CONST.formatted("i", 0));
        stackvmWriter.write(GREATER_THAN.formatted("i"));
        return null;
    }

    @Override
    public Void visit(AndExpr andExpr) {
        System.out.println("[GEN] visit(AndExpr)");
        andExpr.left().accept(this);
        andExpr.right().accept(this);
        stackvmWriter.write(TRUTHY);
        stackvmWriter.write(SWAP);
        stackvmWriter.write(TRUTHY);
        stackvmWriter.write(MUL.formatted("i"));
        return null;
    }

    @Override
    public Void visit(RelExpr relExpr) {
        System.out.println("[GEN] visit(RelExpr): op=" + relExpr.cond().lexeme());
        relExpr.left().accept(this);
        relExpr.right().accept(this);

        TokenType leftType = typeOf(relExpr.left());
        TokenType rightType = typeOf(relExpr.right());
        compareTokenType(leftType, rightType);
        String opType = compareOpType(leftType, rightType);
        stackvmWriter.write(relOperatorOf(relExpr.cond().lexeme()).formatted(opType));
        return null;
    }

    @Override
    public Void visit(CompExpr compExpr) {
        System.out.println("[GEN] visit(CompExpr)");
        compExpr.left().accept(this);
        compExpr.right().accept(this);

        TokenType leftType = typeOf(compExpr.left());
        TokenType rightType = typeOf(compExpr.right());
        compareTokenType(leftType, rightType);
        String opType = compareOpType(leftType, rightType);
        stackvmWriter.write(EQ.formatted(opType));
        return null;
    }

    @Override
    public Void visit(AddExpr addExpr) {
        System.out.println("[GEN] visit(AddExpr): op=" + addExpr.operator().lexeme());
        addExpr.left().accept(this);
        addExpr.right().accept(this);

        TokenType leftType = typeOf(addExpr.left());
        TokenType rightType = typeOf(addExpr.right());
        compareTokenType(leftType, rightType);
        stackvmWriter.write(addOperatorOf(addExpr.operator().lexeme()).formatted(compareOpType(leftType, rightType)));
        if (isCharResult(leftType, rightType)) {
            stackvmWriter.write(INT_TO_CHAR);
        }

        return null;
    }

    @Override
    public Void visit(MulExpr mulExpr) {
        System.out.println("[GEN] visit(MulExpr): op=" + mulExpr.operator().lexeme());
        mulExpr.left().accept(this);
        mulExpr.right().accept(this);

        TokenType leftType = typeOf(mulExpr.left());
        TokenType rightType = typeOf(mulExpr.right());
        castNumber(leftType, rightType);
        if (mulExpr.operator().type().equals(TokenType.OP_MOD)) {
            stackvmWriter.write(MOD);
            return null;
        }

        String opType = compareOpType(leftType, rightType);
        stackvmWriter.write(mulOperatorOf(mulExpr.operator().lexeme()).formatted(opType));

        return null;
    }

    @Override
    public Void visit(UnaryNotExpr unaryExpr) {
        System.out.println("[GEN] visit(UnaryNotExpr)");
        unaryExpr.left().accept(this);
        stackvmWriter.write(CONST.formatted("i", 0));
        stackvmWriter.write(EQ.formatted("i"));
        return null;
    }

    @Override
    public Void visit(UnaryNegExpr unaryExpr) {
        System.out.println("[GEN] visit(UnaryNegExpr)");
        unaryExpr.left().accept(this);
        TokenType leftType = typeOf(unaryExpr.left());
        if (isInt(leftType)) {
            stackvmWriter.write(CONST.formatted("i", "-1"));
            stackvmWriter.write(MUL.formatted("i"));
        }
        else {
            stackvmWriter.write(CONST.formatted("f", "-1.0"));
            stackvmWriter.write(MUL.formatted("f"));
        }

        return null;

    }

    @Override
    public Void visit(FuncCall funcCall) {
        System.out.println("[GEN] visit(FuncCall): " + funcCall.name());
        for (Expr argument : funcCall.args()) {
            argument.accept(this);
        }
        stackvmWriter.write(FUNCTION_CALL.formatted(funcCall.symbol().name()));
        return null;
    }

    @Override
    public Void visit(ArrayAccess arrayAccess) {
        System.out.println("[GEN] visit(ArrayAccess)");
        boolean isString = typeOf(arrayAccess.target()) == TokenType.KEYWORD_STRING;

        if (arrayAccess.target() instanceof LiteralIdent) {
            if (isString){
                stackvmWriter.write(LOAD.formatted(loadIndex(arrayAccess.target().symbol())));
                arrayAccess.index().accept(this);
                stackvmWriter.write(SGET);
            } else {
                arrayAccess.index().accept(this);
                String loadType = compareTypes(typeOf(arrayAccess));
                if (arrayAccess.type() instanceof ArrayType || arrayAccess.type() instanceof ListType) {
                    loadType = "i";
                }
                stackvmWriter.write(HLOAD.formatted(loadType, loadIndex(arrayAccess.target().symbol())));
            }
        }
        else {
            arrayAccess.target().accept(this);
            if (isString) {
                arrayAccess.index().accept(this);
                stackvmWriter.write(SGET);
            } else {
                stackvmWriter.write(STORE.formatted(storeLocal(arrayAccess)));
                arrayAccess.index().accept(this);
                String loadType = compareTypes(typeOf(arrayAccess));
                if (arrayAccess.type() instanceof ArrayType || arrayAccess.type() instanceof ListType) {
                    loadType = "i";
                }
                stackvmWriter.write(HLOAD.formatted(loadType, loadIndex(arrayAccess)));
            }
        }

        return null;
    }

    @Override
    public Void visit(LiteralIdent identExpr) {
        System.out.println("[GEN] visit(LiteralIdent): " + identExpr.symbol());
        stackvmWriter.write(LOAD.formatted(loadIndex(identExpr.symbol())));
        return null;
    }

    @Override
    public Void visit(RangeExpr rangeExpr) {
        System.out.println("[GEN] visit(RangeExpr)");

        TokenType infType = typeOf(rangeExpr.inf());
        TokenType supType = typeOf(rangeExpr.sup());
        int rangeStoreIndex = storeLocal(rangeExpr);
        int infStoreIndex = storeLocal((Record) rangeExpr.inf());

        String loopLabel = createLabel("range_loop");
        rangeExpr.inf().accept(this);

        if (isChar(infType)) {
            stackvmWriter.write(CHAR_TO_INT);
        } else if (isFloat(infType)) {
            stackvmWriter.write(FLOAT_TO_INT);
        }
        stackvmWriter.write(STORE.formatted(infStoreIndex));
        rangeExpr.sup().accept(this);

        if (isChar(supType)) {
            stackvmWriter.write(CHAR_TO_INT);
        } else if (isFloat(supType)) {
            stackvmWriter.write(FLOAT_TO_INT);
        }
        stackvmWriter.write(LOAD.formatted(infStoreIndex));
        stackvmWriter.write(SUB.formatted("i"));
        stackvmWriter.write(CONST.formatted("i", 1));
        stackvmWriter.write(ADD.formatted("i"));
        stackvmWriter.write(ALLOC);
        stackvmWriter.write(STORE.formatted(rangeStoreIndex));

        String endLabel = createLabel("range_end");
        stackvmWriter.write(LOAD.formatted(rangeStoreIndex));
        stackvmWriter.write(LIST_LEN.formatted("h"));
        stackvmWriter.write(CONST.formatted("i", 0));
        stackvmWriter.write(LESS_EQUAL.formatted("i"));
        stackvmWriter.write(JUMP_TRUE_LABEL.formatted(endLabel));

        stackvmWriter.write(CONST.formatted("i", 0));
        stackvmWriter.write(loopLabel + DOTS);
        stackvmWriter.write(DUP);
        stackvmWriter.write(DUP);
        stackvmWriter.write(LOAD.formatted(infStoreIndex));
        stackvmWriter.write(SWAP);
        stackvmWriter.write(ADD.formatted("i"));

        if (isChar(infType)) {
            stackvmWriter.write(INT_TO_CHAR);
        }
        stackvmWriter.write(SWAP);
        stackvmWriter.write(HSTORE.formatted(rangeStoreIndex));
        stackvmWriter.write(RANGE_LOOP_CHECK.formatted(rangeStoreIndex, loopLabel));
        stackvmWriter.write(POP);
        stackvmWriter.write(endLabel + DOTS);
        stackvmWriter.write(LOAD.formatted(loadIndex(rangeExpr)));

        return null;
    }

    @Override
    public Void visit(CollectionLitExpr arrayLitExpr) {
        System.out.println("[GEN] visit(CollectionLitExpr): size=" + arrayLitExpr.value().size());
        for (Expr expr : arrayLitExpr.value())
            expr.accept(this);
        int valuesSize = arrayLitExpr.value().size();
        stackvmWriter.write(CONST.formatted("i",valuesSize));
        stackvmWriter.write(ALLOC);
        stackvmWriter.write(STORE.formatted(loadIndex(arrayLitExpr)));
        int offset = loadIndex(arrayLitExpr);
        for (int i = 1; i <= valuesSize; i++) {
            stackvmWriter.write(INSERT_COLLECTION.formatted(valuesSize, i, offset));
        }
        stackvmWriter.write(LOAD.formatted(offset));
        return null;
    }

    @Override
    public Void visit(IntLitExpr intLitExpr) {
        System.out.println("[GEN] visit(IntLitExpr): " + intLitExpr.value());
        stackvmWriter.write(CONST.formatted("i", intLitExpr.value()));
        return null;
    }

    @Override
    public Void visit(FloatLitExpr floatLitExpr) {
        System.out.println("[GEN] visit(FloatLitExpr): " + floatLitExpr.value());
        stackvmWriter.write(CONST.formatted("f", floatLitExpr.value()));
        return null;
    }

    @Override
    public Void visit(CharLitExpr charLitExpr) {
        System.out.println("[GEN] visit(CharLitExpr): " + charLitExpr.token().lexeme());
        stackvmWriter.write(CONST.formatted("c", charLitExpr.token().lexeme()));
        return null;
    }

    @Override
    public Void visit(StringLitExpr stringLitExpr) {
        System.out.println("[GEN] visit(StringLitExpr): " + stringLitExpr.value());
        stackvmWriter.write(CONST.formatted("s", stringLitExpr.value()));
        return null;
    }

    @Override
    public Void visit(PrimitiveType primitiveType) {
        System.out.println("[GEN] visit(PrimitiveType): " + primitiveType.token().lexeme());
        return null;
    }

    @Override
    public Void visit(ListType listType) {
        System.out.println("[GEN] visit(ListType)");
        listType.innerType().accept(this);
        return null;
    }

    @Override
    public Void visit(ArrayType arrayType) {
        System.out.println("[GEN] visit(ArrayType): dimensions=" + arrayType.dimensions().size());
        arrayType.baseType().accept(this);
        for (Optional<Expr> dimension : arrayType.dimensions()) {
            dimension.ifPresent(expr -> expr.accept(this));
        }
        return null;
    }

    private void attachFunctionsAux(){
        try {
            InputStream stream = getClass().getResourceAsStream(NAME_FILE_FUNCTIONS_AUX);
            assert stream != null;
            String functionsAux = new String(stream.readAllBytes());
            stackvmWriter.write(functionsAux + NEWLINE);
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createLabel(String prefix) {
        return LABEL.formatted(prefix, currentLabel++);
    }

    private int loadIndex(Record name) {
        if (resolver.locals().get(currentFunction).containsKey(name)) {
            return resolver.locals().get(currentFunction).get(name);
        }

        return resolver.parameters().get(currentFunction).get(name);
    }

    private int storeLocal(Record name) {
        if (resolver.locals().get(currentFunction).containsKey(name)) {
            return resolver.locals().get(currentFunction).get(name);
        }
        return resolver.parameters().get(currentFunction).get(name);
    }

    private TokenType typeOf(Expr expr) {
        return expr.type().token().type();
    }

    private boolean isInt(TokenType type) {
        return type.equals(TokenType.KEYWORD_INT) || type.equals(TokenType.LIT_INT);
    }

    private boolean isFloat(TokenType type) {
        return type.equals(TokenType.KEYWORD_FLOAT) || type.equals(TokenType.LIT_FLOAT);
    }

    private boolean isChar(TokenType type) {
        return type.equals(TokenType.KEYWORD_CHAR) || type.equals(TokenType.LIT_CHAR);
    }

    private boolean isCharResult(TokenType leftType, TokenType rightType) {
        return isChar(leftType) || isChar(rightType);
    }

    private void compareTokenType(TokenType leftType, TokenType rightType) {
        if (isInt(leftType) && isFloat(rightType)) {
            stackvmWriter.write(SWAP);
            stackvmWriter.write(INT_TO_FLOAT);
            stackvmWriter.write(SWAP);
            return;
        }

        if (isFloat(leftType) && isInt(rightType)) {
            stackvmWriter.write(INT_TO_FLOAT);
            return;
        }

        if (isInt(leftType) && isChar(rightType)) {
            stackvmWriter.write(CHAR_TO_INT);
            return;
        }

        if (isChar(leftType) && isInt(rightType)) {
            stackvmWriter.write(SWAP);
            stackvmWriter.write(CHAR_TO_INT);
            stackvmWriter.write(SWAP);
            return;
        }

        if (isChar(leftType) && isChar(rightType)) {
            stackvmWriter.write(CHAR_TO_INT);
            stackvmWriter.write(SWAP);
            stackvmWriter.write(CHAR_TO_INT);
            stackvmWriter.write(SWAP);
        }
    }

    private void castNumber(TokenType leftType, TokenType rightType) {
        if (isInt(leftType) && isFloat(rightType)) {
            stackvmWriter.write(SWAP);
            stackvmWriter.write(INT_TO_FLOAT);
            stackvmWriter.write(SWAP);
            return;
        }

        if (isFloat(leftType) && isInt(rightType)) {
            stackvmWriter.write(INT_TO_FLOAT);
        }
    }

    private String compareTypes(TokenType leftType) {
        if (isFloat(leftType)) {
            return "f";
        }
        if (isChar(leftType)) {
            return "c";
        }
        if (isInt(leftType)) {
            return "i";
        }
        return "s";
    }

    private String compareTypes(Type list) {
        return  list instanceof PrimitiveType ? "s" : "h";
    }

    private String compareOpType(TokenType leftType, TokenType rightType) {
        if (isFloat(leftType) || isFloat(rightType)) {
            return "f";
        }
        return "i";
    }

    private String addOperatorOf(String operator) {
        return "+".equals(operator) ? ADD : SUB;
    }

    private String mulOperatorOf(String operator) {
        return "*".equals(operator) ? MUL : DIV;
    }

    private String relOperatorOf(String operator) {
        return switch (operator) {
            case "<" -> LESS_THAN;
            case ">" -> GREATER_THAN;
            case "<=" -> LESS_EQUAL;
            default -> GREATER_EQUAL;
        };
    }
}
