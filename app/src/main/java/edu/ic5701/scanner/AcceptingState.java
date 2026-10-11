package edu.ic5701.scanner;

public record AcceptingState(
        Integer stateId,
        TokenType type,
        Boolean rollback,
        Boolean skip
) {
}
