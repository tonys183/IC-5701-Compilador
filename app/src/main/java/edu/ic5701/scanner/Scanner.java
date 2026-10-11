package edu.ic5701.scanner;

public interface Scanner {
    Token next();
    Token peek();
    Token doublePeek();
    boolean hasNext();
}
