package edu.ic5701.sem.table;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    private Scope parent;
    private Map<String, Symbol> symbols;

    public Scope(Scope parent) {
        this.parent = parent;
        this.symbols = new HashMap<String, Symbol>();
    }

    public Scope getParent() {
        return parent;
    }

    public void add(Symbol symbol) {
        symbols.put(symbol.name(), symbol);
    }

    public Symbol get(String name) {
        return symbols.get(name);
    }

    public Boolean contains(String name) {
        return symbols.containsKey(name);
    }
}
