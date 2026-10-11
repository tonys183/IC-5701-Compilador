package edu.ic5701.scanner;

import edu.ic5701.error.ErrorHandler;
import edu.ic5701.error.LexicalTokenErrorHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public final class MappingScanner implements Scanner {

    private static final int NIL = -1;
    private static final String EOF = "<EOF>";
    private static final char NEWLINE = '\n';

    private static final int
            Q00 = 0, Q01 = 1, Q02 = 2, Q03 = 3, Q04 = 4, Q05 = 5, Q06 = 6, Q07 = 7,
            Q08 = 8, Q09 = 9, Q10 = 10, Q11 = 11, Q12 = 12, Q13 = 13, Q14 = 14,
            Q15 = 15, Q16 = 16, Q17 = 17, Q18 = 18, Q19 = 19, Q20 = 20, Q21 = 21,
            Q22 = 22, Q23 = 23, Q24 = 24, Q25 = 25, Q26 = 26, Q27 = 27, Q28 = 28,
            Q29 = 29, Q30 = 30, Q31 = 31, Q32 = 32, Q33 = 33, Q34 = 34, Q35 = 35,
            Q36 = 36, Q37 = 37, Q38 = 38, Q39 = 39, Q40 = 40, Q41 = 41, Q42 = 42,
            Q43 = 43, Q44 = 44, Q45 = 45;

    private static final int
            INPUT_ALPHA = -1000, INPUT_DIGIT = -2000, INPUT_WS = -3000, INPUT_EOF = -4000, INPUT_OTHER = -5000;

    private String input;
    private int pointer;
    private int offset;
    private int line;

    private final Map<Integer, AcceptingState> acceptingStates = Map.ofEntries(
            Map.entry(Q02, new AcceptingState(Q02, TokenType.LIT_INT, true, false)),
            Map.entry(Q05, new AcceptingState(Q05, TokenType.LIT_INT, true, false)),
            Map.entry(Q06, new AcceptingState(Q06, TokenType.LIT_INT, true, false)),
            Map.entry(Q07, new AcceptingState(Q07, TokenType.LIT_FLOAT, true, false)),
            Map.entry(Q09, new AcceptingState(Q09, TokenType.IDENT, true, false)),
            Map.entry(Q11, new AcceptingState(Q11, TokenType.WHITE_SPACE, true, true)),
            Map.entry(Q13, new AcceptingState(Q13, TokenType.LIT_STRING, false, false)),
            Map.entry(Q17, new AcceptingState(Q17, TokenType.LIT_CHAR, false, false)),
            Map.entry(Q19, new AcceptingState(Q19, TokenType.OP_LE, true, false)),
            Map.entry(Q20, new AcceptingState(Q20, TokenType.OP_LEQ, false, false)),
            Map.entry(Q22, new AcceptingState(Q22, TokenType.OP_GE, true, false)),
            Map.entry(Q23, new AcceptingState(Q23, TokenType.OP_GEQ, false, false)),
            Map.entry(Q25, new AcceptingState(Q25, TokenType.TYPING, true, false)),
            Map.entry(Q26, new AcceptingState(Q26, TokenType.OP_ASSIGN, false, false)),
            Map.entry(Q28, new AcceptingState(Q28, TokenType.RANGE, false, false)),
            Map.entry(Q29, new AcceptingState(Q29, TokenType.DOT, true, false)),
            Map.entry(Q30, new AcceptingState(Q30, TokenType.OP_SUM, false, false)),
            Map.entry(Q31, new AcceptingState(Q31, TokenType.OP_SUB, false, false)),
            Map.entry(Q32, new AcceptingState(Q32, TokenType.OP_MUL, false, false)),
            Map.entry(Q33, new AcceptingState(Q33, TokenType.OP_MOD, false, false)),
            Map.entry(Q34, new AcceptingState(Q34, TokenType.OP_EQU, false, false)),
            Map.entry(Q35, new AcceptingState(Q35, TokenType.PARENT_L, false, false)),
            Map.entry(Q36, new AcceptingState(Q36, TokenType.PARENT_R, false, false)),
            Map.entry(Q37, new AcceptingState(Q37, TokenType.BRACKET_L, false, false)),
            Map.entry(Q38, new AcceptingState(Q38, TokenType.BRACKET_R, false, false)),
            Map.entry(Q39, new AcceptingState(Q39, TokenType.SQRBRACKET_L, false, false)),
            Map.entry(Q40, new AcceptingState(Q40, TokenType.SQRBRACKET_R, false, false)),
            Map.entry(Q41, new AcceptingState(Q41, TokenType.SEMICOLON, false, false)),
            Map.entry(Q42, new AcceptingState(Q42, TokenType.COMMA, false, false)),
            Map.entry(Q43, new AcceptingState(Q43, TokenType.OP_DIV, false, false)),
            Map.entry(Q44, new AcceptingState(Q44, TokenType.OP_NEGATION, false, false)),
            Map.entry(Q45, new AcceptingState(Q45, TokenType.EOF, false, false))
    );

    private final Map<Integer, Map<Integer, Integer>> dfa = Map.ofEntries(
            Map.entry(Q00, Map.ofEntries(
                    Map.entry(INPUT_DIGIT, Q01), Map.entry(INPUT_ALPHA, Q08), Map.entry(INPUT_WS, Q10),
                    Map.entry((int) '"', Q12), Map.entry((int) '\'', Q14), Map.entry((int) '<', Q18),
                    Map.entry((int) '>', Q21), Map.entry((int) ':', Q24), Map.entry((int) '.', Q27),
                    Map.entry((int) '+', Q30), Map.entry((int) '-', Q31), Map.entry((int) '*', Q32),
                    Map.entry((int) '%', Q33), Map.entry((int) '=', Q34), Map.entry((int) '(', Q35),
                    Map.entry((int) ')', Q36), Map.entry((int) '{', Q37), Map.entry((int) '}', Q38),
                    Map.entry((int) '[', Q39), Map.entry((int) ']', Q40), Map.entry((int) ';', Q41),
                    Map.entry((int) ',', Q42), Map.entry((int) '/', Q43), Map.entry((int) '~', Q44),
                    Map.entry(INPUT_EOF, Q45)
            )),
            Map.entry(Q01, Map.of(INPUT_DIGIT, Q01, (int) '.', Q03, INPUT_OTHER, Q02)),
            Map.entry(Q03, Map.of(INPUT_DIGIT, Q04, (int) '.', Q05, INPUT_OTHER, Q06)),
            Map.entry(Q04, Map.of(INPUT_DIGIT, Q04, INPUT_OTHER, Q07)),
            Map.entry(Q08, Map.of(INPUT_ALPHA, Q08, INPUT_DIGIT, Q08, (int) '_', Q08, INPUT_OTHER, Q09)),
            Map.entry(Q10, Map.of(INPUT_EOF, Q11, INPUT_WS, Q10, INPUT_OTHER, Q11)),
            Map.entry(Q12, Map.of((int) '"', Q13, INPUT_OTHER, Q12)),
            Map.entry(Q14, Map.of((int) '\\', Q15, INPUT_OTHER, Q16)),
            Map.entry(Q15, Map.of(INPUT_OTHER, Q16)),
            Map.entry(Q16, Map.of((int) '\'', Q17)),
            Map.entry(Q18, Map.of((int) '=', Q20, INPUT_OTHER, Q19)),
            Map.entry(Q21, Map.of((int) '=', Q23, INPUT_OTHER, Q22)),
            Map.entry(Q24, Map.of((int) '=', Q26, INPUT_OTHER, Q25)),
            Map.entry(Q27, Map.of((int) '.', Q28, INPUT_OTHER, Q29))
    );

    private final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("print", TokenType.KEYWORD_PRINT),
            Map.entry("and", TokenType.KEYWORD_AND),
            Map.entry("or", TokenType.KEYWORD_OR),
            Map.entry("Int", TokenType.KEYWORD_INT),
            Map.entry("String", TokenType.KEYWORD_STRING),
            Map.entry("Char", TokenType.KEYWORD_CHAR),
            Map.entry("Float", TokenType.KEYWORD_FLOAT),
            Map.entry("list", TokenType.KEYWORD_LIST),
            Map.entry("if", TokenType.KEYWORD_IF),
            Map.entry("else", TokenType.KEYWORD_ELSE),
            Map.entry("return", TokenType.KEYWORD_RETURN),
            Map.entry("for", TokenType.KEYWORD_FOR),
            Map.entry("while", TokenType.KEYWORD_WHILE),
            Map.entry("in", TokenType.KEYWORD_IN),
            Map.entry("main", TokenType.KEYWORD_MAIN),
            Map.entry("pop", TokenType.KEYWORD_POP),
            Map.entry("push", TokenType.KEYWORD_PUSH)
    );


    private final LexicalTokenErrorHandler errorHandler = new LexicalTokenErrorHandler();

    private MappingScanner(final String inputPath) {
        this.input = inputPath;
        this.pointer = 0;
        this.offset = 1;
        this.line = 1;
    }

    public static MappingScanner createScanner(final Path path) {
        try {
            return new MappingScanner(Files.readString(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Token next() {
        int currentState = Q00;
        final StringBuilder buffer = new StringBuilder();
        while (true) {
            if (this.acceptingStates.containsKey(currentState)) {
                Optional<Token> currentToken = getCurrentToken(currentState, buffer);
                if (currentToken.isPresent()) {
                    return currentToken.get();
                }
                currentState = Q00;
                buffer.setLength(0);
            }

            int inputValue = this.pointer < input.length() ? input.charAt(pointer) : INPUT_EOF;
            final int nextState = getNextState(classify(inputValue), currentState);

            if (nextState == NIL) {
                final String lexeme = buffer.toString() + (INPUT_EOF == inputValue ? EOF : (char) inputValue);
                throw errorHandler.error(new Token(TokenType.NIL, lexeme, this.line, this.offset), "");
            } else {
                currentState = nextState;
                buffer.append(INPUT_EOF == inputValue ? EOF : (char) inputValue);
                this.pointer++;
                changeCodePosition(inputValue);
            }
        }
    }

    @Override
    public Token peek() {
        final int savedPointer = pointer;
        final int savedLine = line;
        final int savedOffset = offset;

        final Token nextToken = next();

        this.pointer = savedPointer;
        this.line = savedLine;
        this.offset = savedOffset;

        return nextToken;
    }

    @Override
    public Token doublePeek() {
        final int savedPointer = pointer;
        final int savedLine = line;
        final int savedOffset = offset;

        next();
        final Token nextToken = next();

        this.pointer = savedPointer;
        this.line = savedLine;
        this.offset = savedOffset;

        return nextToken;
    }

    @Override
    public boolean hasNext() {
        return this.pointer <= this.input.length();
    }

    private int getNextState(final int processedInputValue, final int currentState) {
        final Map<Integer, Integer> transitions = this.dfa.get(currentState);

        if (processedInputValue == INPUT_EOF) {
            return transitions.getOrDefault(INPUT_EOF, NIL);
        }
        return transitions.getOrDefault(processedInputValue,
               transitions.getOrDefault(INPUT_OTHER, NIL));
    }

    private Optional<Token> getCurrentToken(final int currentState, final StringBuilder buffer) {
        AcceptingState state = this.acceptingStates.get(currentState);
        if (state.rollback()) {
            doRollback(buffer, state);
        }

        if (!state.skip()) {
            final String lexeme = buffer.toString();
            final TokenType tokenType;
            if (state.type() == TokenType.IDENT) {
                tokenType = this.keywords.getOrDefault(lexeme, TokenType.IDENT);
            } else {
                tokenType = state.type();
            }
            return Optional.of(new Token(tokenType, lexeme, this.line, this.offset));
        }

        return Optional.empty();
    }

    private void doRollback(final StringBuilder buffer, final AcceptingState state) {
        final int rollbackCount = (state.stateId() == Q05) ? 2 : 1;
        this.pointer -= rollbackCount;
        if (buffer.length() >= rollbackCount) {
            buffer.setLength(buffer.length() - rollbackCount);
        }
    }

    private int classify(final int inputChar) {
        if (Character.isLetter(inputChar)) {
            return INPUT_ALPHA;
        }
        if (Character.isDigit(inputChar)) {
            return INPUT_DIGIT;
        }
        if (Character.isWhitespace(inputChar)) {
            return INPUT_WS;
        }
        return inputChar;
    }

    private void changeCodePosition(final int inputChar) {
        if (inputChar == NEWLINE) {
            this.line++;
            this.offset = 1;
        } else {
            this.offset++;
        }
    }

}
