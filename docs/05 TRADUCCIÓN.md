# Reglas de traducción

**Equipo:** Gael Ruiz, Anthony Segura, Saúl Pacheco

---

## 1) Reglas

* **Nodo: Program(functions)**
    * `∀ function ∈ functions, function.accept(this)`
    * _StackVM Code_:
      ```stackvm
      .def main: args = 0, locals = 0
        call main_func()
        halt
      ```


* **Nodo: Block(staments)**
    * `∀ stmt ∈ staments, stmt.accept(this)`


* **Nodo: StmtExpr(expr)**
    * `expr.accept(this)`


* **Nodo: ParamDecl(params)**
    * `∀ param ∈ params, param.accept(this)`
    * 

* **Nodo: FuncDecl(funcType, params, name, block)**
    * ``` 
      ∀ param ∈ params, param.accept(this)
      .def label_name: args = params.size, locals = body.locals`
      block.accept(this)
      ```

* **Nodo: DeclVariable(type, name, assignValue, symbol)**
    * ```
      Si assignValue.isPresent() -> assignValue.get().accept(this)
      assignValue.isPresent():
      ```
      _StackVM Code:_
      ```stackvm
      ; eval assignValue
      store #_var
      ```

* **Nodo: LiteralIdent(name)**
  <br> _StackVM Code:_
   ```stackvm
    load #_var

* **Nodo: StmtReturn(expr)**
    * `expr.accept(this)` <br>
      _StackVM Code:_
       ```stackvm
       ret

* **Nodo: StmtPrInt(expr)**
    * `expr.accept(this)` <br>
      _StackVM Code:_
      ```stackvm
      print

* **Nodo: StmtIf(cond, thenBody, elseBody)**
    * `cond.accept(this)`
    * `Si elseBody.isPresent() -> elseBody.get().accept(this) ^:` <br>
      _StackVM Code:_
      ```stackvm
      call truthy()
      iconst 1     
      ieq
      jmpt label_then_#
      jmp label_else_#
      label_then_#:
         ; thenBody.accept(this)
         jmp label_end_if_#
      label_else_#:
        ; elseBody.get().accept(this)
        jmp label_end_if_#
      label_end_if_#:
      ```

    * `Si elseBody.isEmpty() ->` <br>
      _StackVM Code:_
      ```stackvm
      call truthy()
      icons 1
      ieq
      jmpt label_then_#
      jmp label_end_if_#
      label_then_#:
         ; thenBody.accept(this)
      label_end_if_#:
      ```

* **Nodo: StmtWhile(cond, body)**
    * `cond.accept(this)` <br>
      _StackVM Code:_
       ```stackvm 
      label_while_cond_#:
        ; cond.accept(this)
      call truthy()              
      iconst 1
      ieq   
      jmpt label_while_body_#
      jmp label_end_while_#
      label_while_body_#:
        ; body.accept(this)
        jmp label_while_cond_#
      label_end_while_#
      ```

* **Nodo: StmtFor(iterator, list, body)**
    * `list.accept(this)` <br>
      _StackVM Code:_
      ```stackvm
      store #_list
      iconst 0
      store #_index
      label_for_cond_#:
        load #_index
        load #_list
        (s | h)len
        call igeq()
        jmpt label_for_end_#
      
      load #_index
      h(s | i | f | c)load #_list
      store #_iterator
      
      ; body.accept(this)
      
      load #_index
      iconst 1
      iadd
      store #_index
      jmp label_for_cond_#
      
      label_for_end_#:

* **Nodo: AssignExpr(target, value)**

    * `Si target = LiteralIdent`:
      ```stackvm
      ; value.accept(this)
      store #_target
      ```

    * `Si target = ArrayAccess(base, index)` y `base = LiteralIdent`:
      ```stackvm
      ; value.accept(this)
      ; index.accept(this)
      hstore #_base
      ```

    * `Si target = ArrayAccess(base, index)` y `base = ArrayAccess`:
      ```stackvm
      ; base.accept(this)       ; resuelve hasta la sublista contenedora
      store #_tmp_ptr

      ; value.accept(this)
      ; index.accept(this)
      hstore #_tmp_ptr
      ```

* **Nodo: AddExpr(left, right, operator)**
    * `left.accept(this) ^ right.accept(this)`
    * `Si left.type = Int ^ right.type = Float:` <br>
      _StackVM Code:_
      ```stackvm
      swap
      itof
      swap
      fadd | fsub
      
    * `Si left.type = Int ^ right.type = Char:` <br>
      _StackVM Code:_
      ```stackvm
      ctoi
      iadd | isub
      call itoc()
      
    * `Si left.type ^ right.type = Int:` <br>
      _StackVM Code:_
      ```stackvm
      iadd | isub

    * `Si left.type = Float ^ right.type = Int:` <br>
      _StackVM Code:_
      ```stackvm
      itof
      fadd | fsub
      
    * `Si left.type ^ right.type = Float:` <br>
      _StackVM Code:_
      ```stackvm
      fadd | fsub

    * `Si left.type = Char ^ right.type = Int:` <br>
      _StackVM Code:_
      ```stackvm
      swap
      ctoi
      sawp
      iadd | isub
      call itoc()

    * `Si left.type ^ right.type = Char:` <br>
      _StackVM Code:_
      ```stackvm
      ctoi
      swap
      ctoi
      swap
      iadd | isub
      call itoc()

* **Nodo: MulExpr(left, right, operator)**
    * `left.accept(this) ^ right.accept(this)`
    * `Si left.type = Int ^ right.type = Float:` <br>
      _StackVM Code:_
      ```stackvm
      swap
      itof
      swap
      fmul | call fdiv()

    * `Si left.type ^ right.type = Int:` <br>
      _StackVM Code:_
      ```stackvm
      imul | idiv | imod

    * `Si left.type = Float ^ right.type = Int:` <br>
      _StackVM Code:_
      ```stackvm
      itof
      fmul | call fdiv()
      
    * `Si left.type ^ right.type = Float:` <br>
      _StackVM Code:_
      ```stackvm
      fmul | fdiv

* **Nodo: CompExpr(left, right, operator)**
    * `left.accept(this) ^ right.accept(this)`
    * `Si left.type ^ right.type = Int:` <br>
      _StackVM Code:_
      ```stackvm
      ieq

    * `Si left.type ^ right.type = Float:` <br>
      _StackVM Code:_
      ```stackvm
      feq

    * `Si left.type ^ right.type = Char:` <br>
      _StackVM Code:_
      ```stackvm
      ctoi
      swap
      ctoi
      swap
      ieq

* **Nodo: RelExpr(left, right, operator)**
    * `left.accept(this) ^ right.accept(this)`
    * `Si left.type ^ right.type = Char:` <br>
      _StackVM Code:_
      ```stackvm
      ctoi
      swap
      ctoi
      swap

    * `Si (left.type ^ right.type = Int) ∨ (left.type ^ right.type = Char):`
      * `Si operator = '<':` <br>
        _StackVM Code:_
        ```stackvm
        ilt

      * `Si operator = '>':` <br>
        _StackVM Code:_
        ```stackvm
        call igt()

      * `Si operator = '<=':` <br>
        _StackVM Code:_
        ```stackvm
        call ileq()
          
      * `Si operator = '>=':` <br>
        _StackVM Code:_
        ```stackvm
        call igeq()

    * `Si left.type ^ right.type = Float:`
      * `Si operator = '<':` <br>
        _StackVM Code:_
        ```stackvm
        flt

      * `Si operator = '>':` <br>
        _StackVM Code:_
        ```stackvm
        call fgt()

      * `Si operator = '<=':` <br>
        _StackVM Code:_
        ```stackvm
        call fleq()

      * `Si operator = '>=':` <br>
        _StackVM Code:_
        ```stackvm
        call fgeq()

* **Nodo: AndExpr(left, right)**
    * `left.accept(this) ^ right.accept(this)` <br>
      _StackVM Code:_
      ```stackvm
      call truthy() 
      swap
      call truthy()
      imul

* **Nodo: OrExpr(left, right)**
    * `left.accept(this) ^ right.accept(this)` <br>
      _StackVM Code:_
      ```stackvm
      call truthy() 
      swap
      call truthy()
      iadd
      iconst 0
      call igt()

* **Nodo: UnaryNegExpr(left)**
    * `left.accept(this)`
    * `Si left.type = Int:` <br>
      _StackVM Code:_
      ```stackvm
      iconst -1
      imul


  * `Si left.type = Float:` <br>
    _StackVM Code:_
    ```stackvm
    fconst -1.0
    fmul

* **Nodo: UnaryNotExpr(left)**
    * `left.accept(this)`
    * `Si left.type = Int:` <br>
      _StackVM Code:_
      ```stackvm
      iconst 0
      ieq

* **Nodo: FuncCall(name, args)**
    * `∀ arg ∈ args, arg.accept(this)` <br>
      _StackVM Code:_
      ```stackvm
      call label_name_func()

* **Nodo: CollectionLitExpr(values)**
    * `∀ value ∈ values, value.accept(this)` <br>
      _StackVM Code:_
      ```stackvm
      iconst values.size()
      alloc
      store #_collection
      
      iconst values.size()
      iconst 1
      isub 
      hstore #_collection
       
      iconst values.size()
      iconst 2
      isub 
      hstore #_collection
      ⋮
      load #_collection
* **Nodo: PushExpr(name, value)**
    * `value.accept(this) ^ name.accept(this) ` <br>
      _StackVM Code:_
      ```stackvm
      dup ;  [value, list]
      (h | s)len
      iconst 1
      iadd
      call (i | c | f | s)realloc() ; [value, list+1]
      store #_name
      load #_name
      (h | s)len
      iconst 1
      isub
      hstore #_name

* **Nodo: PopExpr(name)** <br>
    * `name.accept(this) ` <br>
    _StackVM Code:_ 
    ```stackvm
    load #_name
    dup
    (h | s)len
    iconst 1
    isub
    call (i | c | f | s)realloc()
    swap
    (h | s)len
    iconst 1
    isub
    h(i | c | f | s)load #_name
    swap
    store #_name
    
* * **Nodo: ArrayAccess(target, index)**

    * `Si target = LiteralIdent`:
      ```stackvm
      ; index.accept(this)
      h(i | f | c | s)load #_target
      ```

    * `Si target = ArrayAccess`:
      ```stackvm
      ; target.accept(this)
      store #_target
  
      ; index.accept(this)
      h(i | f | c | s)load #_target
      ```
      
    * `Si el resultado del acceso es otra lista/arreglo`:
      ```stackvm
      ; index.accept(this)
      hiload #_target           
      ```

    * `Si target.type = String`:
      ```stackvm
      ; target.accept(this)
      ; index.accept(this)
      sget
      ```

* **Nodo: RangeExpr(inf, sup)**

    * `inf.accept(this)`
    * `Si inf.type = Char`:
      ```stackvm
      ctoi
      ```
    * `Si inf.type = Float`:
      ```stackvm
      call ftoi()
      ```

  _StackVM Code:_
    ```stackvm
    store #_inf
    ```

    * `sup.accept(this)`
    * `Si sup.type = Char`:
      ```stackvm
      ctoi
      ```
    * `Si sup.type = Float`:
      ```stackvm
      call ftoi()
      ```

  _StackVM Code:_
    ```stackvm
    load #_inf
    swap
    isub
    iconst 1
    iadd
    alloc
    store #_range
    iconst 0                  ; i = 0
    label_range_loop_#:
      dup                     ; [i, i]
      dup                     ; [i, i, i]
      load #_inf              ; [i, i, i, inf]
      swap                    ; [i, i, inf, i]
      iadd                    ; [i, i, inf + i]
      (call itoc() | ε)
      swap                    ; [i, inf + i, i]
      load #_range            ; [i, inf + i, i, range]
      hstore         
      iconst 1
      iadd                    ; [i + 1]
      dup                     ; [i + 1, i + 1]
      hlen #_range            ; [i + 1, i + 1, len]
      ilt                     ; [i + 1, bool]
      jmpt label_range_loop_#

    load #_range
    ```
* **Nodo: (Int | Float | Char | String)LitExpr** <br>
   _StackVM Code:_
   ```stackvm
   (i | f | c | s)const value