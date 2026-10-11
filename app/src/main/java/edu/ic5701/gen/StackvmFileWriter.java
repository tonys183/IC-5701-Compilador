package edu.ic5701.gen;

import java.io.FileWriter;
import java.io.IOException;

public class StackvmFileWriter implements Writer {

    private final FileWriter stackvmWriter;
    private static final String FUNCTION_DEF = ".def";
    private static final String NEWLINE = "\n";
    private static final String LABEL_END = ":";
    private static final String TAB = "  ";

    public StackvmFileWriter(FileWriter writer) {
        this.stackvmWriter = writer;
    }

    @Override
    public void write(String out) {
        try {
            String[] lines = out.split(NEWLINE);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    stackvmWriter.write(NEWLINE);
                    continue;
                }
                if (trimmed.startsWith(FUNCTION_DEF)) {
                    stackvmWriter.write(NEWLINE + trimmed + NEWLINE);
                } else if (trimmed.endsWith(LABEL_END)) {
                    stackvmWriter.write(NEWLINE + TAB + trimmed + NEWLINE);
                } else {
                    stackvmWriter.write(TAB + TAB + trimmed + NEWLINE);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            stackvmWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
