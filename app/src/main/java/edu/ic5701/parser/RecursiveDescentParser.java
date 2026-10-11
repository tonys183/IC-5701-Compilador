package edu.ic5701.parser;

import edu.ic5701.ast.Program;
import edu.ic5701.ast.expr.*;
import edu.ic5701.ast.stmt.*;
import edu.ic5701.ast.type.ArrayType;
import edu.ic5701.ast.type.ListType;
import edu.ic5701.ast.type.PrimitiveType;
import edu.ic5701.ast.type.Type;
import edu.ic5701.error.SyntaticTokenErrorHandler;
import edu.ic5701.scanner.Scanner;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public final class RecursiveDescentParser implements Parser {

    private static final EnumSet<TokenType> TYPES = EnumSet.of(
            TokenType.KEYWORD_INT,
            TokenType.KEYWORD_STRING,
            TokenType.KEYWORD_CHAR,
            TokenType.KEYWORD_LIST,
            TokenType.KEYWORD_FLOAT
    );

    private static final EnumSet<TokenType> RELATIONAL = EnumSet.of(
            TokenType.OP_GE,
            TokenType.OP_LE,
            TokenType.OP_GEQ,
            TokenType.OP_LEQ
    );

    private static final EnumSet<TokenType> ADDITIVE_OPS = EnumSet.of(
            TokenType.OP_SUB,
            TokenType.OP_SUM
    );

    private static final EnumSet<TokenType> MULTIPLICATIVE_OPS = EnumSet.of(
            TokenType.OP_MOD,
            TokenType.OP_MUL,
            TokenType.OP_DIV
    );

    private static final EnumSet<TokenType> UNARY_OPS = EnumSet.of(
            TokenType.OP_NEGATION,
            TokenType.OP_SUB
    );

    private static final EnumSet<TokenType> LITERAL = EnumSet.of(
            TokenType.LIT_CHAR,
            TokenType.LIT_INT,
            TokenType.LIT_STRING,
            TokenType.LIT_FLOAT
    );
    private static final EnumSet<TokenType> EXPR_FIRST = EnumSet.of(
            TokenType.LIT_CHAR,
            TokenType.LIT_INT,
            TokenType.LIT_STRING,
            TokenType.LIT_FLOAT,
            TokenType.IDENT,
            TokenType.PARENT_L,
            TokenType.SQRBRACKET_L,
            TokenType.OP_NEGATION,
            TokenType.OP_SUB
    );

    private static final EnumSet<TokenType> STMT_FIRST = EnumSet.of(
            TokenType.KEYWORD_RETURN,
            TokenType.KEYWORD_FOR,
            TokenType.KEYWORD_WHILE,
            TokenType.KEYWORD_IF,
            TokenType.KEYWORD_PRINT,
            TokenType.IDENT,
            TokenType.PARENT_L,
            TokenType.SQRBRACKET_L,
            TokenType.OP_NEGATION,
            TokenType.OP_SUB,
            TokenType.LIT_CHAR,
            TokenType.LIT_INT,
            TokenType.LIT_STRING,
            TokenType.LIT_FLOAT
    );

    private static final EnumSet<TokenType> FUNTIONS_OPS = EnumSet.of(
            TokenType.KEYWORD_MAIN,
            TokenType.IDENT
    );

    private static final String TOKEN_TYPE_ERROR_FORMAT =
            "Expected type: %s but found: %s";
    private static final String TOKEN_DATA_FORMAT =
            "[Recognized token] Type: %s, Lexeme: '%s', Line: %d, Column: %d\n";

    private final Scanner lexer;
    private final SyntaticTokenErrorHandler errorHandler = new SyntaticTokenErrorHandler();

    public RecursiveDescentParser(final Scanner scanner) {
        this.lexer = scanner;
    }

    @Override
    public Program parse() {
        return parseProgram();
    }

    private Token lookahead() {
        return this.lexer.peek();
    }

    private Token doubleLookahead() { return this.lexer.doublePeek(); }

    private Token consume() {
        Token token = this.lexer.next();
        System.out.printf(TOKEN_DATA_FORMAT, token.type(), token.lexeme(), token.line(), token.offset());
        return token;
    }

    private Token match(final TokenType expected) {
        Token token = consume();
        if (!token.type().equals(expected)) {
            throw errorHandler.error(
                    token,
                    String.format(TOKEN_TYPE_ERROR_FORMAT,expected, token.type())
            );
        }
        return token;
    }

    private Token matchAny(final EnumSet<TokenType> expected) {
        Token token = lookahead();
        if (!expected.contains(token.type())) {
            throw errorHandler.error(
                    token,
                    String.format(TOKEN_TYPE_ERROR_FORMAT,expected, token.type())
            );
        }
        return consume();
    }

    // Program -> Func Func*
    private Program parseProgram() {
        ArrayList<FuncDecl> funcDeclList = new ArrayList<>();
        do {
            funcDeclList.add(parseFunction());
        } while (FUNTIONS_OPS.contains(lookahead().type()));
        match(TokenType.EOF);
        return new Program(funcDeclList);
    }

    // Func -> ident "(" ParamList ")" ":" Type Block
    private FuncDecl parseFunction() {
        Token name = matchAny(FUNTIONS_OPS);
        match(TokenType.PARENT_L);
        ParamDecl param = parseParamList();
        match(TokenType.PARENT_R);
        match(TokenType.TYPING);
        Type type = parseType();
        Block body = parseBlock();
        return new FuncDecl(type, param, name, body);
    }

    // Type -> BaseType ArraySuffix*
    private Type parseType() {
        boolean isArray = false;
        List<Optional<Expr>> dimensions = new ArrayList<>();
        Type type = parseBaseType();
        while (lookahead().type() == TokenType.SQRBRACKET_L) {
            isArray = true;
            dimensions.add(parseArraySuffix());
        }
        return isArray ? new ArrayType(type, dimensions) : type;
    }

    // ArraySuffix -> "[" ArrExpr "]"
    private Optional<Expr> parseArraySuffix() {
        match(TokenType.SQRBRACKET_L);
        Optional<Expr> exprVisitor = parseArrExpr();
        match(TokenType.SQRBRACKET_R);
        return exprVisitor;
    }

    // ArrExpr -> Expr | ε
    private Optional<Expr> parseArrExpr() {
        return EXPR_FIRST.contains(lookahead().type()) ? Optional.of(parseExpr()) : Optional.empty();
    }

    // BaseType -> "Int" | "Char" | "String" | "Float" | "list" "<" BaseType ">"
    private Type parseBaseType() {
        Token token = matchAny(TYPES);
        if (token.type() == TokenType.KEYWORD_LIST) {
            match(TokenType.OP_LE);
            Type type = parseBaseType();
            match(TokenType.OP_GE);
            return new ListType(type);
        }
        return new PrimitiveType(token);
    }

    // Block -> "{" Stmt* "}"
    private Block parseBlock() {
        List<Stmt> statements = new ArrayList<>();
        match(TokenType.BRACKET_L);
        while (STMT_FIRST.contains(lookahead().type())) {
            statements.add(parseStmt());
        }
        match(TokenType.BRACKET_R);
        return new Block(statements);
    }

    // Stmt -> Stmt_Return | Stmt_For | Stmt_While | Stmt_If | Stmt_Print | Stmt_Decl | Expr ";"
    private Stmt parseStmt() {
        Token token = lookahead();
        switch (token.type()) {
            case KEYWORD_RETURN -> {
                return parseStmtReturn();
            }
            case KEYWORD_IF -> {
                return parseStmtIf();
            }
            case KEYWORD_WHILE -> {
                return parseStmtWhile();
            }
            case KEYWORD_FOR -> {
                return parseStmtFor();
            }
            case KEYWORD_PRINT -> {
                return parseStmtPrint();
            }
            case TokenType.IDENT -> {
                if (doubleLookahead().type() == TokenType.TYPING) {
                    return parseStmtDecl();
                }
                Expr expr = parseExpr();
                match(TokenType.SEMICOLON);
                return expr;
            }
            default -> {
                Expr expr = parseExpr();
                match(TokenType.SEMICOLON);
                return expr;
            }
        }
    }

    // Stmt_Decl ->  ident ":" Type (":=" Expr)? ";"
    private Stmt parseStmtDecl() {
        Token ident = match(TokenType.IDENT);
        match(TokenType.TYPING);
        Type type = parseType();
        Optional<Expr> value = Optional.empty();
        if (lookahead().type() == TokenType.OP_ASSIGN) {
            match(TokenType.OP_ASSIGN);
            value = Optional.of(parseExpr());
        }
        match(TokenType.SEMICOLON);
        return new DeclVariable(type, ident, value, null);
    }

    // Stmt_Print -> "print" "(" Expr ")" ";"
    private StmtPrint parseStmtPrint() {
        match(TokenType.KEYWORD_PRINT);
        match(TokenType.PARENT_L);
        Expr expr = parseExpr();
        match(TokenType.PARENT_R);
        match(TokenType.SEMICOLON);
        return new StmtPrint(expr);
    }

    // Stmt_For -> "for" "(" ident ":" Type ";" "in" ("[" BracketExpr "]" | ident FactorTail) ")" Block
    private StmtFor parseStmtFor() {
        match(TokenType.KEYWORD_FOR);
        match(TokenType.PARENT_L);
        Token iteratorName = match(TokenType.IDENT);
        match(TokenType.TYPING);
        Type type = parseType();
        match(TokenType.SEMICOLON);
        match(TokenType.KEYWORD_IN);
        Optional<Expr> expr;
        if (lookahead().type().equals(TokenType.SQRBRACKET_L)) {
            match(TokenType.SQRBRACKET_L);
            expr = parseBracketExpr();
            match(TokenType.SQRBRACKET_R);
        } else {
            expr = Optional.of(parseFactorTail(match(TokenType.IDENT)));
        }
        match(TokenType.PARENT_R);
        Block body = parseBlock();
        return new StmtFor(
               new DeclVariable(type, iteratorName, Optional.empty(), null),
               expr,
               body
        );
    }

    // Stmt_While -> "while" "(" Expr ")" Block
    private StmtWhile parseStmtWhile() {
        match(TokenType.KEYWORD_WHILE);
        match(TokenType.PARENT_L);
        Expr condition = parseExpr();
        match(TokenType.PARENT_R);
        Block body = parseBlock();
        return new StmtWhile(condition, body);
    }

    // Stmt_If -> "if" "(" Expr ")" Block Stmt_Else
    private StmtIf parseStmtIf() {
        match(TokenType.KEYWORD_IF);
        match(TokenType.PARENT_L);
        Expr condition = parseExpr();
        match(TokenType.PARENT_R);
        Block thenBody = parseBlock();
        Optional<Block> elseBody = Optional.empty();
        if (lookahead().type() == TokenType.KEYWORD_ELSE) {
            elseBody = Optional.of(parseStmtElse());
        }
        return new StmtIf(condition, thenBody, elseBody);
    }

    // Stmt_Else -> "else" Block | ε
    private Block parseStmtElse() {
        match(TokenType.KEYWORD_ELSE);
        return parseBlock();
    }

    // Stmt_Return -> "return" Expr ";"
    private StmtReturn parseStmtReturn() {
        match(TokenType.KEYWORD_RETURN);
        Expr expr = parseExpr();
        match(TokenType.SEMICOLON);
        return new StmtReturn(expr);
    }

    // ParamList -> Param ParamListTail | ε
    private ParamDecl parseParamList() {
        List<DeclVariable> params = new ArrayList<>();
        if (lookahead().type() == TokenType.IDENT) {
            params.add(parseParam());
            params.addAll(parseParamListTail());
        }
        return new ParamDecl(params);
    }

    // ParamListTail -> "," Param ParamListTail | ε
    private List<DeclVariable> parseParamListTail() {
        List<DeclVariable> params = new ArrayList<>();
        if (lookahead().type() == TokenType.COMMA) {
            match(TokenType.COMMA);
            params.add(parseParam());
            params.addAll(parseParamListTail());
        }
        return params;
    }

    // Param -> ident ":" Type
    private DeclVariable parseParam() {
        Token name = match(TokenType.IDENT);
        match(TokenType.TYPING);
        Type type = parseType();
        return new DeclVariable(type, name, Optional.empty(), null);
    }

    // Expr -> AssignExpr
    private Expr parseExpr() {
        return parseAssignExpr();
    }

    // AssignExpr -> OrExpr (":=" OrExpr ";")?
    private Expr parseAssignExpr() {
        Expr expr = parseOrExpr();
        if (lookahead().type().equals(TokenType.OP_ASSIGN)) {
            match(TokenType.OP_ASSIGN);
            Expr value = parseOrExpr();
            return new AssignExpr(expr, value, null, null);
        }
        return expr;
    }

    // OrExpr -> AndExpr ("or" AndExpr)*
    private Expr parseOrExpr() {
        Expr left = parseAndExpr();
        while (lookahead().type() == TokenType.KEYWORD_OR) {
            match(TokenType.KEYWORD_OR);
            left = new OrExpr(left, parseAndExpr(),null);
        }
        return left;
    }

    // AndExpr -> CompExpr ("and" CompExpr)*
    private Expr parseAndExpr() {
        Expr left = parseCompExpr();
        while (lookahead().type() == TokenType.KEYWORD_AND) {
            Token operator = match(TokenType.KEYWORD_AND);
            left = new AndExpr(left, parseCompExpr(), null);
        }
        return left;
    }

    // CompExpr -> RelExpr ("=" RelExpr)*
    private Expr parseCompExpr() {
        Expr left = parseRelationalExpr();
        while (lookahead().type() == TokenType.OP_EQU) {
            match(TokenType.OP_EQU);
            left = new CompExpr(left, parseRelationalExpr(), null);
        }
        return left;
    }

    // RelExpr -> AddExpr (("<" | ">" | "<=" | ">=") AddExpr)*
    private Expr parseRelationalExpr() {
        Expr left = parseAdditiveExpr();
        while (RELATIONAL.contains(lookahead().type())) {
            Token cond = matchAny(RELATIONAL);
            left = new RelExpr(left, parseAdditiveExpr(), cond, null);
        }
        return left;
    }

    // AddExpr -> MulExpr (("+" | "-") MulExpr)*
    private Expr parseAdditiveExpr() {
        Expr left = parseMultiplicativeExpr();
        while (ADDITIVE_OPS.contains(lookahead().type())) {
            Token operator = matchAny(ADDITIVE_OPS);
            left = new AddExpr(left, parseMultiplicativeExpr(), operator, null);
        }
        return left;
    }

    // MulExpr -> UnaryExpr (("*" | "/" | "%") UnaryExpr)*
    private Expr parseMultiplicativeExpr() {
        Expr left = parseUnaryExpr();
        while (MULTIPLICATIVE_OPS.contains(lookahead().type())) {
            Token operator = matchAny(MULTIPLICATIVE_OPS);
            left = new MulExpr(left, parseUnaryExpr(), operator, null);
        }
        return left;
    }

    // UnaryExpr -> ("-" | "~") UnaryExpr | Factor
    private Expr parseUnaryExpr() {
        if (UNARY_OPS.contains(lookahead().type())) {
            Token unaryOp = matchAny(UNARY_OPS);
            return unaryOp.type().equals(TokenType.OP_SUB) ?
                        new UnaryNegExpr(parseUnaryExpr(), null) :
                        new UnaryNotExpr(parseUnaryExpr(), null);
        } else {
            return parseFactor();
        }
    }

    // Factor -> literal | ident FactorTail | "(" Expr ")" | "[" BracketExpr "]"
    private Expr parseFactor() {
        Token token = lookahead();

        if (token.type() == TokenType.IDENT) {
            Token ident = match(TokenType.IDENT);
            return parseFactorTail(ident);
        } else if (LITERAL.contains(token.type())) {
            Token literal = matchAny(LITERAL);
            return switch (literal.type()){
                case LIT_INT -> new IntLitExpr(literal, Integer.parseInt(literal.lexeme()));
                case LIT_CHAR -> new CharLitExpr(literal, parseCharLiteral(literal.lexeme()));
                case LIT_FLOAT -> new FloatLitExpr(literal, Float.parseFloat(literal.lexeme()));
                default -> new StringLitExpr(literal, literal.lexeme());
            };
        } else if (token.type() == TokenType.PARENT_L) {
            match(TokenType.PARENT_L);
            Expr expr = parseExpr();
            match(TokenType.PARENT_R);
            return expr;
        }
        Token collection = match(TokenType.SQRBRACKET_L);
        Optional<Expr> expr = parseBracketExpr();
        match(TokenType.SQRBRACKET_R);
        return expr.orElseGet(() -> new CollectionLitExpr(collection, List.of(), null));

    }

    // FactorTail -> ArrayAccess FactorAction
    private Expr parseFactorTail(final Token ident) {
        Expr expr = parseArrayAccess(ident);
        return parseFactorAction(expr);
    }

    // FactorAction -> "(" ArgsList ")" | "." ("pop" "(" ")" | "push" "(" Expr ")" ) | ε
    private Expr parseFactorAction(final Expr ident) {
        Token token = lookahead();

        if (token.type() == TokenType.PARENT_L) {
            match(TokenType.PARENT_L);
            FuncCall funcCall = parseArgsList(ident);
            match(TokenType.PARENT_R);
            return funcCall;
        } else if (token.type() == TokenType.DOT) {
            match(TokenType.DOT);
            if (lookahead().type() == TokenType.KEYWORD_POP) {
                match(TokenType.KEYWORD_POP);
                match(TokenType.PARENT_L);
                match(TokenType.PARENT_R);
                return new PopExpr(ident, null, null);
            } else {
                match(TokenType.KEYWORD_PUSH);
                match(TokenType.PARENT_L);
                Expr value = parseExpr();
                match(TokenType.PARENT_R);
                return new PushExpr(ident, value, null, null);
            }
        }
        return ident;
    }

    // ArrayAccess -> ("[" Expr "]")*
    private Expr parseArrayAccess(Token ident) {
        Expr target = new LiteralIdent(ident, null, null);
        while (lookahead().type() == TokenType.SQRBRACKET_L) {
            match(TokenType.SQRBRACKET_L);
            target = new ArrayAccess(target, parseExpr(), null, null);
            match(TokenType.SQRBRACKET_R);
        }
        return target;
    }

    // ArgsList -> Expr ArgsListTail | ε
    private FuncCall parseArgsList(final Expr ident) {
        List<Expr> args = new ArrayList<>();
        if (EXPR_FIRST.contains(lookahead().type())) {
            args.add(parseExpr());
            parseArgsListTail(args);
        }
        return new FuncCall(ident, args, null, null);
    }

    // ArgsListTail -> "," Expr ArgsListTail | ε
    private void parseArgsListTail(final  List<Expr> args) {
        if (lookahead().type() == TokenType.COMMA) {
            match(TokenType.COMMA);
            args.add(parseExpr());
            parseArgsListTail(args);
        }
    }

    // BracketExpr -> Expr BracketTail | ε
    private Optional<Expr> parseBracketExpr() {
        Expr expr = null;
        if (EXPR_FIRST.contains(lookahead().type())) {
            expr = parseExpr();
            expr = parseBracketTail(expr);
        }
        return Optional.ofNullable(expr);
    }

    // BracketTail -> ".." Expr | ("," Expr)*
    private Expr parseBracketTail(final Expr value) {
        Token token = lookahead();
        if (token.type() == TokenType.RANGE) {
            match(TokenType.RANGE);
            return new RangeExpr(value, parseExpr(), null);
        }
        List<Expr> values = new ArrayList<>();
        values.add(value);
        while (lookahead().type() == TokenType.COMMA) {
            match(TokenType.COMMA);
            values.add(parseExpr());
        }
        return new CollectionLitExpr(value.token(), values, null);
    }

    private char parseCharLiteral(String text) {
        return switch (text) {
            case "\\n" -> '\n';
            case "\\'" -> '\'';
            case "\\\"" -> '"';
            case "\\\\" -> '\\';
            default -> text.charAt(1);
        };
    }
}
