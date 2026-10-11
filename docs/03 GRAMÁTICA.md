# Gramática del lenguaje de programacion 3Z

**Equipo:** Gael Ruiz, Anthony Segura, Saúl Pacheco

---

## 1) Gramática
Esta sección detalla la estructura gramatical del lenguaje.
```text
Program -> Func Func*
Func -> ident "(" ParamList ")" ":" Type Block
Type -> BaseType ArraySuffix*
ArraySuffix -> "[" ArrExpr "]"
BaseType -> "Int" | "Char" | "String" | "Float" | "list" "<" BaseType ">"
Block -> "{" Stmt* "}"
Stmt -> Stmt_Return | Stmt_For | Stmt_While | Stmt_If | Stmt_Print | Stmt_Decl | Expr
Stmt_Return -> "return" Expr ";"
Stmt_For -> "for" "(" ident ":" Type ";" "in" ("[" BracketExpr "]" | ident) ")" Block
Stmt_While -> "while" "(" Expr ")" Block
Stmt_If -> "if" "(" Expr ")" Block Stmt_Else
Stmt_Else -> "else" Block | ε
Stmt_Print -> "print" "(" Expr ")" ";"
Stmt_Decl ->  ident ":" Type (":=" Expr)? ";"
ArrExpr -> Expr | ε
Expr -> AssignExpr
AssignExpr -> OrExpr (":=" OrExpr ";")?
OrExpr -> AndExpr ("or" AndExpr)*
AndExpr -> CompExpr ("and" CompExpr)*
CompExpr -> RelExpr ("=" RelExpr)* 
RelExpr -> AddExpr (("<" | ">" | "<=" | ">=") AddExpr)*
AddExpr -> MulExpr (("+" | "-") MulExpr)*
MulExpr -> UnaryExpr (("*" | "/" | "%") UnaryExpr)*
UnaryExpr -> ("-" | "~") UnaryExpr | Factor
Factor -> literal | ident FactorTail | "(" Expr ")" | "[" BracketExpr "]"
BracketExpr -> Expr BracketTail | ε
BracketTail -> ".." Expr | ("," Expr)*
FactorTail -> ArrayAccess FactorAction
ArrayAccess -> ("[" Expr "]")*
FactorAction -> "(" ArgsList ")" | "." ("pop" "(" ")" | "push" "(" Expr ")" )";" | ε
ParamList -> Param ParamListTail | ε
ParamListTail -> "," Param ParamListTail | ε
Param -> ident ":" Type
ArgsList -> Expr ArgsListTail | ε
ArgsListTail -> "," Expr ArgsListTail | ε
```

## 2) FIRST
* `First(Program) = {"ident"}`
* `First(Func) = {"ident"}`
* `First(Type) = {"Int", "Char", "String", "Float", "list"}`
* `First(BaseType) = {"Int", "Char", "String", "Float", "list"}`
* `First(ArraySuffix) = {"["}`
* `First(Block) = {"{"}`
* `First(Stmt) = {"return", "for", "while", "if", "print", "ident", "~", "-", "literal", "(", "["}`
* `First(Stmt_Return) = {"return"}`
* `First(Stmt_For) = {"for"}`
* `First(Stmt_While) = {"while"}`
* `First(Stmt_If) = {"if"}`
* `First(Stmt_Else) = {"else", ε}`
* `First(Stmt_Print) = {"print"}`
* `First(Stmt_Decl) = {"ident"}`
* `First(ArrExpr) = {"~", "-", "literal", "ident", "(", "[", ε}`
* `First(Expr) = {"ident", "~", "-", "literal", "(", "["}`
* `First(AssignExpr) = {"ident", "~", "-", "literal", "(", "["}`
* `First(OrExpr) = {"~", "-", "literal", "ident", "(", "["}`
* `First(AndExpr) = {"~", "-", "literal", "ident", "(", "["}`
* `First(CompExpr) = {"~", "-", "literal", "ident", "(", "["}`
* `First(RelExpr) = {"~", "-", "literal", "ident", "(", "["}`
* `First(AddExpr) = {"~", "-", "literal", "ident", "(", "["}`
* `First(MulExpr) = {"~", "-", "literal", "ident", "(", "["}`
* `First(UnaryExpr) = {"-", "~", "literal", "ident", "(", "["}`
* `First(Factor) = {"literal", "ident", "(", "["}`
* `First(BracketExpr) = {"ident", "~", "-", "literal", "(", "[", ε}`
* `First(BracketTail) = {"..", ",", ε}`
* `First(FactorTail) = {"[", "(", ".", ε}`
* `First(ArrayAccess) = {"[", ε}`
* `First(FactorAction) = {"(", ".", ε}`
* `First(ParamList) = {"ident", ε}`
* `First(ParamListTail) = {",", ε}`
* `First(Param) = {"ident"}`
* `First(ArgsList) = {"ident", "~", "-", "literal", "(", "[", ε}`
* `First(ArgsListTail) = {",", ε}`

## 3) FOLLOW
* `Follow(Program) = {EOF}`
* `Follow(Func) = {"ident", EOF}`
* `Follow(ParamList) = {")"}`
* `Follow(ParamListTail) = {")"}`
* `Follow(Param) = {",", ")"}`
* `Follow(Type) = {"{", ";", ":=", ","}`
* `Follow(BaseType) = {"[", ">", "{", ";", ":=", ","}`
* `Follow(ArraySuffix) = {"[", "{", ";", ":=", ","}`
* `Follow(Block) = {"ident", "return", "for", "while", "if", "print", "~", "-", "literal", "(", "[", "}", "else", EOF}`
* `Follow(Stmt) = {"ident", "return", "for", "while", "if", "print", "~", "-", "literal", "(", "[", "}", "else"}`
* `Follow(Stmt_Return) = Follow(Stmt)`
* `Follow(Stmt_For) = Follow(Stmt)`
* `Follow(Stmt_While) = Follow(Stmt)`
* `Follow(Stmt_If) = Follow(Stmt)`
* `Follow(Stmt_Else) = Follow(Stmt)`
* `Follow(Stmt_Print) = Follow(Stmt)`
* `Follow(Stmt_Decl) = Follow(Stmt)`
* `Follow(ArrExpr) = {"]"}`
* `Follow(BracketExpr) = {"]"}`
* `Follow(BracketTail) = {"]"}`
* `Follow(ArgsList) = {")"}`
* `Follow(ArgsListTail) = {")"}`
* `Follow(Expr) = {";", ")", "]", "..", ","} U Follow(Stmt)`
* `Follow(AssignExpr) = Follow(Expr)`
* `Follow(OrExpr) = {":=", ";"} U Follow(AssignExpr)`
* `Follow(AndExpr) = {"or"} U Follow(OrExpr)`
* `Follow(CompExpr) = {"and"} U Follow(AndExpr)`
* `Follow(RelExpr) = {"="} U Follow(CompExpr)`
* `Follow(AddExpr) = {"<", ">", "<=", ">="} U Follow(RelExpr)`
* `Follow(MulExpr) = {"+", "-"} U Follow(AddExpr)`
* `Follow(UnaryExpr) = {"*", "/", "%"} U Follow(MulExpr)`
* `Follow(Factor) = Follow(UnaryExpr)`
* `Follow(FactorTail) = Follow(Factor)`
* `Follow(ArrayAccess) = {"(", "."} U Follow(Factor)`
* `Follow(FactorAction) = Follow(Factor)`

## 4) Tabla (Relaciones)
```text
(Program, ident): Program -> Func Func*

(Func, ident): Func -> ident "(" ParamList ")" ":" Type Block

(Type, "Int"): Type -> BaseType ArraySuffix*
(Type, "Char"): Type -> BaseType ArraySuffix*
(Type, "String"): Type -> BaseType ArraySuffix*
(Type, "Float"): Type -> BaseType ArraySuffix*
(Type, "list"): Type -> BaseType ArraySuffix*

(BaseType, "Int"): BaseType -> "Int"
(BaseType, "Char"): BaseType -> "Char"
(BaseType, "String"): BaseType -> "String"
(BaseType, "Float"): BaseType -> "Float"
(BaseType, "list"): BaseType -> "list" "<" BaseType ">"

(ArraySuffix, "["): ArraySuffix -> "[" ArrExpr "]"

(ArrExpr, "~"): ArrExpr -> Expr
(ArrExpr, "-"): ArrExpr -> Expr
(ArrExpr, literal): ArrExpr -> Expr
(ArrExpr, ident): ArrExpr -> Expr
(ArrExpr, "("): ArrExpr -> Expr
(ArrExpr, "["): ArrExpr -> Expr
(ArrExpr, "]"): ArrExpr -> ε

(Block, "{"): Block -> "{" Stmt* "}"

(Stmt, "return"): Stmt -> Stmt_Return
(Stmt, "for"): Stmt -> Stmt_For
(Stmt, "while"): Stmt -> Stmt_While
(Stmt, "if"): Stmt -> Stmt_If
(Stmt, "print"): Stmt -> Stmt_Print
(Stmt, "ident"): Stmt -> Stmt_Decl
(Stmt, "ident"): Stmt -> Expr
(Stmt, "~"): Stmt -> Expr
(Stmt, "-"): Stmt -> Expr
(Stmt, literal): Stmt -> Expr
(Stmt, "("): Stmt -> Expr
(Stmt, "["): Stmt -> Expr

(Stmt_Return, "return"): Stmt_Return -> "return" Expr ";"

(Stmt_For, "for"): Stmt_For -> "for" "(" ident ":" Type ";" "in" ("[" BracketExpr "]" | ident) ")" Block

(Stmt_While, "while"): Stmt_While -> "while" "(" Expr ")" Block

(Stmt_If, "if"): Stmt_If -> "if" "(" Expr ")" Block Stmt_Else

(Stmt_Else, "else"): Stmt_Else -> "else" Block
(Stmt_Else, ident): Stmt_Else -> ε
(Stmt_Else, "return"): Stmt_Else -> ε
(Stmt_Else, "for"): Stmt_Else -> ε
(Stmt_Else, "while"): Stmt_Else -> ε
(Stmt_Else, "if"): Stmt_Else -> ε
(Stmt_Else, "print"): Stmt_Else -> ε
(Stmt_Else, "~"): Stmt_Else -> ε
(Stmt_Else, "-"): Stmt_Else -> ε
(Stmt_Else, literal): Stmt_Else -> ε
(Stmt_Else, "("): Stmt_Else -> ε
(Stmt_Else, "["): Stmt_Else -> ε
(Stmt_Else, "}"): Stmt_Else -> ε

(Stmt_Print, "print"): Stmt_Print -> "print" "(" Expr ")" ";"

(Stmt_Decl, ident): Stmt_Decl -> ident ":" Type (":=" Expr)? ";"

(Expr, ident): Expr -> AssignExpr
(Expr, "~"): Expr -> AssignExpr
(Expr, "-"): Expr -> AssignExpr
(Expr, literal): Expr -> AssignExpr
(Expr, "("): Expr -> AssignExpr
(Expr, "["): Expr -> AssignExpr

(AssignExpr, ident): AssignExpr -> OrExpr (":=" OrExpr ";")?
(AssignExpr, "~"): AssignExpr -> OrExpr (":=" OrExpr ";")?
(AssignExpr, "-"): AssignExpr -> OrExpr (":=" OrExpr ";")?
(AssignExpr, literal): AssignExpr -> OrExpr (":=" OrExpr ";")?
(AssignExpr, "("): AssignExpr -> OrExpr (":=" OrExpr ";")?
(AssignExpr, "["): AssignExpr -> OrExpr (":=" OrExpr ";")?

(OrExpr, "~"): OrExpr -> AndExpr ("or" AndExpr)*
(OrExpr, "-"): OrExpr -> AndExpr ("or" AndExpr)*
(OrExpr, literal): OrExpr -> AndExpr ("or" AndExpr)*
(OrExpr, ident): OrExpr -> AndExpr ("or" AndExpr)*
(OrExpr, "("): OrExpr -> AndExpr ("or" AndExpr)*
(OrExpr, "["): OrExpr -> AndExpr ("or" AndExpr)*

(AndExpr, "~"): AndExpr -> CompExpr ("and" CompExpr)*
(AndExpr, "-"): AndExpr -> CompExpr ("and" CompExpr)*
(AndExpr, literal): AndExpr -> CompExpr ("and" CompExpr)*
(AndExpr, ident): AndExpr -> CompExpr ("and" CompExpr)*
(AndExpr, "("): AndExpr -> CompExpr ("and" CompExpr)*
(AndExpr, "["): AndExpr -> CompExpr ("and" CompExpr)*

(CompExpr, "~"): CompExpr -> RelExpr ("=" RelExpr)*
(CompExpr, "-"): CompExpr -> RelExpr ("=" RelExpr)*
(CompExpr, literal): CompExpr -> RelExpr ("=" RelExpr)*
(CompExpr, ident): CompExpr -> RelExpr ("=" RelExpr)*
(CompExpr, "("): CompExpr -> RelExpr ("=" RelExpr)*
(CompExpr, "["): CompExpr -> RelExpr ("=" RelExpr)*

(RelExpr, "~"): RelExpr -> AddExpr (("<" | ">" | "<=" | ">=") AddExpr)*
(RelExpr, "-"): RelExpr -> AddExpr (("<" | ">" | "<=" | ">=") AddExpr)*
(RelExpr, literal): RelExpr -> AddExpr (("<" | ">" | "<=" | ">=") AddExpr)*
(RelExpr, ident): RelExpr -> AddExpr (("<" | ">" | "<=" | ">=") AddExpr)*
(RelExpr, "("): RelExpr -> AddExpr (("<" | ">" | "<=" | ">=") AddExpr)*
(RelExpr, "["): RelExpr -> AddExpr (("<" | ">" | "<=" | ">=") AddExpr)*

(AddExpr, "~"): AddExpr -> MulExpr (("+" | "-") MulExpr)*
(AddExpr, "-"): AddExpr -> MulExpr (("+" | "-") MulExpr)*
(AddExpr, literal): AddExpr -> MulExpr (("+" | "-") MulExpr)*
(AddExpr, ident): AddExpr -> MulExpr (("+" | "-") MulExpr)*
(AddExpr, "("): AddExpr -> MulExpr (("+" | "-") MulExpr)*
(AddExpr, "["): AddExpr -> MulExpr (("+" | "-") MulExpr)*

(MulExpr, "~"): MulExpr -> UnaryExpr (("*" | "/" | "%") UnaryExpr)*
(MulExpr, "-"): MulExpr -> UnaryExpr (("*" | "/" | "%") UnaryExpr)*
(MulExpr, literal): MulExpr -> UnaryExpr (("*" | "/" | "%") UnaryExpr)*
(MulExpr, ident): MulExpr -> UnaryExpr (("*" | "/" | "%") UnaryExpr)*
(MulExpr, "("): MulExpr -> UnaryExpr (("*" | "/" | "%") UnaryExpr)*
(MulExpr, "["): MulExpr -> UnaryExpr (("*" | "/" | "%") UnaryExpr)*

(UnaryExpr, "-"): UnaryExpr -> "-" UnaryExpr
(UnaryExpr, "~"): UnaryExpr -> "~" UnaryExpr
(UnaryExpr, literal): UnaryExpr -> Factor
(UnaryExpr, ident): UnaryExpr -> Factor
(UnaryExpr, "("): UnaryExpr -> Factor
(UnaryExpr, "["): UnaryExpr -> Factor

(Factor, literal): Factor -> literal
(Factor, ident): Factor -> ident FactorTail
(Factor, "("): Factor -> "(" Expr ")"
(Factor, "["): Factor -> "[" BracketExpr "]"

(BracketExpr, "~"): BracketExpr -> Expr BracketTail
(BracketExpr, "-"): BracketExpr -> Expr BracketTail
(BracketExpr, literal): BracketExpr -> Expr BracketTail
(BracketExpr, ident): BracketExpr -> Expr BracketTail
(BracketExpr, "("): BracketExpr -> Expr BracketTail
(BracketExpr, "["): BracketExpr -> Expr BracketTail
(BracketExpr, "]"): BracketExpr -> ε

(BracketTail, ".."): BracketTail -> ".." Expr
(BracketTail, ","): BracketTail -> ("," Expr)*
(BracketTail, "]"): BracketTail -> ε

(FactorTail, "["): FactorTail -> ArrayAccess FactorAction
(FactorTail, "("): FactorTail -> ArrayAccess FactorAction
(FactorTail, "."): FactorTail -> ArrayAccess FactorAction
(FactorTail, ":="): FactorTail -> ArrayAccess FactorAction
(FactorTail, ";"): FactorTail -> ArrayAccess FactorAction
(FactorTail, ")"): FactorTail -> ArrayAccess FactorAction
(FactorTail, "]"): FactorTail -> ArrayAccess FactorAction
(FactorTail, ","): FactorTail -> ArrayAccess FactorAction
(FactorTail, ".."): FactorTail -> ArrayAccess FactorAction
(FactorTail, "or"): FactorTail -> ArrayAccess FactorAction
(FactorTail, "and"): FactorTail -> ArrayAccess FactorAction
(FactorTail, "="): FactorTail -> ArrayAccess FactorAction
(FactorTail, "<"): FactorTail -> ArrayAccess FactorAction
(FactorTail, ">"): FactorTail -> ArrayAccess FactorAction
(FactorTail, "<="): FactorTail -> ArrayAccess FactorAction
(FactorTail, ">="): FactorTail -> ArrayAccess FactorAction
(FactorTail, "+"): FactorTail -> ArrayAccess FactorAction
(FactorTail, "-"): FactorTail -> ArrayAccess FactorAction
(FactorTail, "*"): FactorTail -> ArrayAccess FactorAction
(FactorTail, "/"): FactorTail -> ArrayAccess FactorAction
(FactorTail, "%"): FactorTail -> ArrayAccess FactorAction

(ArrayAccess, "["): ArrayAccess -> ("[" Expr "]")*
(ArrayAccess, ":="): ArrayAccess -> ε
(ArrayAccess, "("): ArrayAccess -> ε
(ArrayAccess, "."): ArrayAccess -> ε
(ArrayAccess, ";"): ArrayAccess -> ε
(ArrayAccess, ")"): ArrayAccess -> ε
(ArrayAccess, "]"): ArrayAccess -> ε
(ArrayAccess, ","): ArrayAccess -> ε
(ArrayAccess, ".."): ArrayAccess -> ε
(ArrayAccess, "or"): ArrayAccess -> ε
(ArrayAccess, "and"): ArrayAccess -> ε
(ArrayAccess, "="): ArrayAccess -> ε
(ArrayAccess, "<"): ArrayAccess -> ε
(ArrayAccess, ">"): ArrayAccess -> ε
(ArrayAccess, "<="): ArrayAccess -> ε
(ArrayAccess, ">="): ArrayAccess -> ε
(ArrayAccess, "+"): ArrayAccess -> ε
(ArrayAccess, "-"): ArrayAccess -> ε
(ArrayAccess, "*"): ArrayAccess -> ε
(ArrayAccess, "/"): ArrayAccess -> ε
(ArrayAccess, "%"): ArrayAccess -> ε

(FactorAction, "("): FactorAction -> "(" ArgsList ")"
(FactorAction, "."): FactorAction -> "." ("pop" "(" ")" | "push" "(" Expr ")")";"
(FactorAction, ":="): FactorAction -> ε
(FactorAction, ";"): FactorAction -> ε
(FactorAction, ")"): FactorAction -> ε
(FactorAction, "]"): FactorAction -> ε
(FactorAction, ","): FactorAction -> ε
(FactorAction, ".."): FactorAction -> ε
(FactorAction, "or"): FactorAction -> ε
(FactorAction, "and"): FactorAction -> ε
(FactorAction, "="): FactorAction -> ε
(FactorAction, "<"): FactorAction -> ε
(FactorAction, ">"): FactorAction -> ε
(FactorAction, "<="): FactorAction -> ε
(FactorAction, ">="): FactorAction -> ε
(FactorAction, "+"): FactorAction -> ε
(FactorAction, "-"): FactorAction -> ε
(FactorAction, "*"): FactorAction -> ε
(FactorAction, "/"): FactorAction -> ε
(FactorAction, "%"): FactorAction -> ε

(ParamList, ident): ParamList -> Param ParamListTail
(ParamList, ")"): ParamList -> ε

(ParamListTail, ","): ParamListTail -> "," Param ParamListTail
(ParamListTail, ")"): ParamListTail -> ε

(Param, ident): Param -> ident ":" Type

(ArgsList, "~"): ArgsList -> Expr ArgsListTail
(ArgsList, "-"): ArgsList -> Expr ArgsListTail
(ArgsList, literal): ArgsList -> Expr ArgsListTail
(ArgsList, ident): ArgsList -> Expr ArgsListTail
(ArgsList, "("): ArgsList -> Expr ArgsListTail
(ArgsList, "["): ArgsList -> Expr ArgsListTail
(ArgsList, ")"): ArgsList -> ε

(ArgsListTail, ","): ArgsListTail -> "," Expr ArgsListTail
(ArgsListTail, ")"): ArgsListTail -> ε
```