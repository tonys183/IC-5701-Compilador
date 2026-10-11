# Especificación semántica operativa

**Equipo:** Gael Ruiz, Anthony Segura, Saúl Pacheco

---

## 1) Resolución de nombres
* **Nodo: Program(functions)**
    * `inicio st.newScope()`
    * `fin st.pop()`

* **Nodo: FuncDecl(name, funcType, param, block)**
    * `name ∉ st_actual => st[name] = (name, type = funcType, category = function)`
    * `FuncDecl.type = st[name]`
    * `inicio st.newScope()`
    * `fin st.pop()`

* **Nodo: ParamDecl(params)**
    * `∀ param ∈ params : param.assignValue = ∅`

* **Nodo: DeclVariable(type, name, assignValue)**
    * `name ∉ st_actual ^ (name ∉ st v st[name].category = name.category) => st[name] = (name, type, category = var)`
    * `DeclVariable.type = st[name]`

* **Nodo: Block([stmt])**
    * `inicio st.newScope()`
    * `fin st.pop()`

* **Nodo: StmtFor(iterator, list, body)**
    * `list ≠ ∅`
    * `inicio st.newScope()`
    * `fin st.pop()`

* **Nodo: AssignExpr(target, expr)**
    * `target ∈ {LiteralIdent, ArrayAccess}`
    * `target ∈ st`
    * `AssignExpr.type = st[target]`

* **Nodo: LiteralIdent(name)**
    * `name ∈ st`
    * `LiteralIdent.type = st[name]`

* **Nodo: FuncCall(name, args)**
    * `name ∈ LiteralIdent`
    * `name ∈ st`
    * `st[name].category = function`
    * `FuncCall.type = st[name]`

* **Nodo: PopExpr(name)**
    * `name ∈ st`

* **Nodo: PushExpr(name, value)**
    * `name ∈ st`
    * `PushExpr.type = st[name]`

* **Nodo: ArrayType(baseType, dimensions)**
    * `∀ dimension ∈ dimensions `
    * `Si dimension ≠ ∅ => ∀ ident ∈ dimension, ident ∈ st  `

## 2) Chequeo de tipos
* **Nodo: FuncDecl(name, funcType, param, block)**
    * `name = main  => funcType = Int`
* **Nodo: DeclVariable(type, name, assignValue)**
    * `Si assignValue ≠ ∅ => type = assignValue.type`

* **Nodo: OrExpr(left, right)**
    * `left.type = Int ^ right.type = Int => OrExpr.type = Int`

* **Nodo: AndExpr(left, right)**
    * `left.type = Int ^ right.type = Int => AndExpr.type = Int`

* **Nodo: CompExpr(left, right)**
    * `left.type = (Int | Float | Char) ^ right.type = left.type => CompExpr.type = Int`

* **Nodo: RelExpr(left, right)**
    * `left.type = (Int | Float | Char) ^ right.type = left.type => RelExpr.type = Int`

* **Nodo: AddExpr(left, right)**
    * `left.type = (Int | Float | Char) ^ right.type = (Int | Float | Char)`
    * `Si right.type = left.type => AddExpr.type = left.type`
    * `Si left.type = Int ^ right.type = Char (o viceversa) => AddExpr.type = Char`
    * `Si left.type = Int ^ right.type = Float (o viceversa) => AddExpr.type = Float`

* **Nodo: MulExpr(left, right)**
    * `Si operator = % => left.type = Int ^ right.type = Int => MulExpr.type = Int`
    * `Si operator ∈ {* , /} => left.type = (Int | Float) ^ right.type = (Int | Float)`
    * `Si right.type = left.type => MulExpr.type = left.type`
    * `Si left.type = Int ^ right.type = Float (o viceversa) => MulExpr.type = Float`

* **Nodo: UnaryNegExpr(left)**
    * `left.type = (Int | Float) => UnaryNegExpr.type = left.type`

* **Nodo: UnaryNotExpr(left)**
    * `left.type = Int => UnaryNotExpr.type = Int`

* **Nodo: RangeExpr(inf, sup)**
    * `inf.type = (Int | Char | Float) ^ sup.type = inf.type => RangeExpr.type = inf.type`

* **Nodo: ArrayAccess(target, index)**
    * `index.type = Int`
    * `Si target.type = String => ArrayAccess.type = Char`
    * `Si target.type = ArrayType(baseType, dimensions) ^ el acceso consume todas las dimensiones => ArrayAccess.type = baseType`
    * `Si target.type = ArrayType(baseType, dimensions) ^ el acceso no consume todas las dimensiones => ArrayAccess.type = ArrayType(baseType, dimensions restantes)`

* **Nodo: CollectionLitExpr(values)**
    * `∀ i ∈ [1 .. |values|-1] : values{i}.type = values{i+1}.type`
    * `CollectionLitExpr.type = values{1}.type`

* **Nodo: ReturnStmt(expr)**
    * `Sea F la función contenedora más cercana => expr.type = F.funcType`

* **Nodo: WhileStmt(cond, body)**
    * `cond.type = Int`

* **Nodo: IfStmt(cond, thenBody, elseBody)**
    * `cond.type = Int`

* **Nodo: ForStmt(iterator, list, body)**
    * `iterator.type = (Int | Float | Char)`
    * `Si list = RangeExpr(inf, sup) => iterator.type = list.type`
    * `Si list = CollectionLitExpr(values) => iterator.type = values{1}.type`
    * `Si list es identificador o acceso => list.type debe tener dimensión mayor que 0`
    * `Si list es identificador o acceso => iterator.type = tipo resultante de acceder a los elementos de list`

* **Nodo: AssignExpr(target, value)**
    * `target ∈ {LiteralIdent, ArrayAccess}`
    * `value.type = target.type`

* **Nodo: FuncCall(name, args)**
    * `Sea F la declaración asociada a name`
    * `|args| = |F.params|`
    * `∀ i ∈ [0 .. |args|-1] : args{i}.type = F.params{i}.type`
    * `FuncCall.type = F.funcType`

* **Nodo: PushExpr(name, value)**
    * `Si name.type = ListType(innerType) => value.type = innerType`
    * `Si name.type ≠ ListType => value.type = name.type`

* **Nodo: PopExpr(name)**
    * `PopExpr.type = name.innerType`

* **Nodo: IntLitExpr(value)**
    * `IntLitExpr.type = Int`

* **Nodo: FloatLitExpr(value)**
    * `FloatLitExpr.type = Float`

* **Nodo: CharLitExpr(value)**
    * `CharLitExpr.type = Char`

* **Nodo: StringLitExpr(value)**
    * `StringLitExpr.type = String`