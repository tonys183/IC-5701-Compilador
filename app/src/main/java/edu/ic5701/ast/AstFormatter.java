package edu.ic5701.ast;

import edu.ic5701.scanner.Token;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.Optional;

public final class AstFormatter {

    private static final String INDENT_STEP = "  ";
    private static final String STR_EMPTY = "empty";
    private static final String STR_NULL = "null";
    private static final String STR_OPTIONAL_PREFIX = "Optional\n";
    private static final String STR_EMPTY_LIST = "[]";
    private static final String STR_LIST_START = "[\n";
    private static final String STR_LIST_ITEM_PREFIX = "- ";
    private static final String STR_LIST_END = "]";
    private static final String STR_NEWLINE = "\n";
    private static final String STR_CR = "\r";
    private static final String STR_LF = "\n";
    private static final String STR_ESCAPED_LF = "\\n";
    private static final String STR_EMPTY_STRING = "";
    private static final String STR_TOKEN_START = "('";
    private static final String STR_TOKEN_END = "')";
    private static final String STR_RECORD_START = " {";
    private static final String STR_FIELD_SEP = ": ";
    private static final String STR_ERROR_READING = "<error reading>";
    private static final String STR_RECORD_END = "}";

    private final StringBuilder builder = new StringBuilder();
    private int level = 0;

    private AstFormatter() {}

    public static String format(Object node) {
        AstFormatter formatter = new AstFormatter();
        formatter.builder.append(STR_NEWLINE);
        formatter.formatValue(node);
        return formatter.builder.toString();
    }

    private void formatValue(Object value) {
        if (value == null) {
            builder.append(STR_NULL);
        } else if (value instanceof Optional<?> optional) {
            if (optional.isPresent()) {
                builder.append(STR_OPTIONAL_PREFIX);
                level++;
                indent();
                formatValue(optional.get());
                level--;
            } else {
                builder.append(STR_EMPTY);
            }
        } else if (value instanceof Collection<?> collection) {
            formatCollection(collection);
        } else if (value instanceof Token token) {
            formatToken(token);
        } else if (value.getClass().isRecord()) {
            formatRecord(value);
        } else {
            builder.append(value);
        }
    }

    private void formatCollection(Collection<?> collection) {
        if (!(collection.isEmpty())) {
            builder.append(STR_LIST_START);
            level++;

            int index = 0;
            for (Object item : collection) {
                indent();
                builder.append(STR_LIST_ITEM_PREFIX);
                formatValue(item);

                if (index < collection.size() - 1) {
                    builder.append(STR_NEWLINE);
                }

                index++;
            }

            level--;
            builder.append(STR_NEWLINE);
            indent();
            builder.append(STR_LIST_END);
        } else {
            builder.append(STR_EMPTY_LIST);
        }
    }

    private void formatToken(Token token) {
        String formattedLexeme = token.lexeme()
                .replace(STR_LF, STR_ESCAPED_LF)
                .replace(STR_CR, STR_EMPTY_STRING);

        builder.append(token.type())
                .append(STR_TOKEN_START)
                .append(formattedLexeme)
                .append(STR_TOKEN_END);
    }

    private void formatRecord(Object record) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        int componentsLength = components.length;

        builder.append(record.getClass().getSimpleName()).append(STR_RECORD_START);

        if (componentsLength != 0) {
            builder.append(STR_NEWLINE);
            level++;

            for (int i = 0; i < componentsLength; i++) {
                RecordComponent component = components[i];

                indent();
                builder.append(component.getName()).append(STR_FIELD_SEP);

                try {
                    Object componentValue = component.getAccessor().invoke(record);
                    formatValue(componentValue);
                } catch (ReflectiveOperationException e) {
                    builder.append(STR_ERROR_READING);
                }

                if (i < componentsLength - 1) {
                    builder.append(STR_NEWLINE);
                }
            }

            level--;
            builder.append(STR_NEWLINE);
            indent();
            builder.append(STR_RECORD_END);
        } else {
            builder.append(STR_RECORD_END);
        }
    }

    private void indent() {
        builder.append(INDENT_STEP.repeat(level));
    }
}