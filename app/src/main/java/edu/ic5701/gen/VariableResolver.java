package edu.ic5701.gen;

import edu.ic5701.ast.Program;
import edu.ic5701.ast.Visitor;
import java.util.Map;

public interface VariableResolver extends Visitor<Void> {
    Map<String, Map<Record, Integer>> locals();
    Map<String, Map<Record, Integer>> parameters();
    void solve(Program program);
}
