# Especificación del lenguaje de programacion 3Z

**Equipo:** Gael Ruiz, Anthony Segura, Saúl Pacheco

---

## 1) Introducción
Este lenguaje multiparadigma incluye elementos del paradigma funcional y procedimental. Utiliza tanto memoria dinámica como estática. Se basó en la sintaxis tipo C, con elementos de Python y Pascal.


## 2) Palabras reservadas
* `Int`, `Char`, `Float` y `String` son las palabras reservadas para definir el tipo de una variable.
  * *Ejemplo*: `a: Int`.
* `for`, `while`, `if`, `else` son las palabras reservadas típicas para ciclos y condicionales.
* `in` es una palabra reservada para un iterador (como el `in` de Python).
* `or` y `and` son operadores booleanos.
* `main` es la palabra reservada para la función principal del programa, similar a C.
* `list` es la palabra reservada para definir un arreglo dinámico.
  * *Ejemplo:* `a: list<Char> := ['a', 'b'];`
  * Este arreglo dinámico tiene métodos `push` y `pop` para añadir y eliminar elementos respectivamente.


## 3) Tipos
Como se mencionó anteriormente, están los siguientes tipos (datos primitivos):

* **`Int`**:Tipo entero de 32 bits.
* **`Float`**:Número flotante de 32 bits.
* **`Char`**:Número entero de 8 bits que contiene el código de un carácter ASCII.
* **`String`**:Arreglo dinámico de datos tipo `Char`.
* **`list<Tipo>`**:Arreglo dinámico de cualquier dato.


## 4) Expresiones (operadores)
Nuestro lenguaje es fuertemente tipado. Operadores aritméticos pueden ser aplicados a enteros y flotantes. Comparaciones pueden ser realizadas en cualquier tipo de dato. Los operadores lógicos son condicionados aplicando a booleanos en nuestra representación. Se detallan los operadores a continuación:

* `f()`: llamada a función
* `~`: negación
* `[]`: definición de arreglos
* `list<Tipo>`: definición de arreglos dinámicos
* `*`: multiplicación
* `/`: división
* `%`: módulo
* `+`: suma
* `-`: resta
* `<`, `<=`, `>`, `>=`, `=`: comparación
* `and`, `or`: *y* lógico, *o* lógico
* `:=`: asignación

**Algunos ejemplos:**

```text
X: Int;
X: Int := 5;
arr1: Int[3]:= [1, 2, 3];
arr2: Int[100];
dynamicArr: list<Char> := ['a', 'b'];
```

Asimismo, se presentan delimitadores: `{}`, `()`,`[]`, `""`, `,`, `;` para separar los diferentes bloques. 

* `{}`: delimitadores para bloques de código
* `()`: delimitadores para funciones y expresiones
* `[]`: delimitadores para arreglos
* `""`: delimitadores para hileras
* `,`: delimitador para separar elementos de un arreglo o parámetros de una función
* `;`: delimitador para separar instrucciones


## 5) Declaraciones (Statements)
Las variables se pueden declarar sin inicializarse y se pueden inicializar posteriormente. Entre los statements se encuentran las expresiones aritméticas, el `return`, `if` y `else`, ciclos `for` y `while`.

El `print` se maneja de una forma similar a Python, donde se imprime el valor del literal en pantalla, sin dar formato.

El `range` ([x .. y]) se maneja de una forma similar a Python, generando un arreglo estático de enteros o flotantes desde `x` hasta `y`. En el caso de hacer un range de flotantes se hacen incrementos discretos de una unidad sobre el flotante inicial. 

Cada línea es terminada con un `;` que separa cada instrucción, permitiendo saltos de línea.

Explicación semántica y estructural de cada *statement*:
* **`if`** y **`else`**: El bloque de código dentro del `if` se ejecutará si la condición es verdadera, de lo contrario se ejecutará el bloque de código dentro del `else` (*si existe*). El bloque de código se delimita con `{}`.
    **Ejemplo**:
    ```text
    if (x%2 = 0) {
        print(x); print(" es par\n");
    } else {
        print(x); print(" es impar\n");
    }
    ```
  
* **`for`**: El bloque de código dentro del ciclo se ejecutará un número determinado de veces, dependiendo de la cantidad de elementos que el iterador `in` recorra en la lista especificada en el `for`. El bloque de código se delimita con `{}`.
    **Ejemplo**:
    ```text
    for (i:Int in [0 .. 4]) {
        print(i); print("\n");
    }
    ```
  
* **`while`**: El bloque de código dentro del ciclo se ejecutará mientras la condición sea verdadera. El bloque de código se delimita con `{}`.
    **Ejemplo**:
    ```text
    while (x > 0) {
        print(x); print(" es positivo\n");
        x := x - 1;
    }
    ```
  
* **`print`**: Se imprime el valor del literal en pantalla, solamente un único literal.
    **Ejemplo**:
    ```text
    a : Int := 3;
    b : Int := 7;
    c : Int := 50;
    pi: Float := 3.14;
    print("El resultado de la fracción mixta "); 
    print(a); print(" "); print(b); print('/'); print(c);
    print(" es "); print(pi);
    ```

* **`[x .. y]`**: Genera un arreglo estático de enteros o flotantes desde `x` hasta `y`. Se incrementa `x` de uno a uno hasta que sea mayor o igual que `y`. Se puede usar como parte de un `for` o para asignar a una variable.
  **Ejemplo**:
  ```text
  for (i:Char in ['a' .. 'z']) {
      print(i); 
  }
  print("\n");
   arr: Int[5] := [0 .. 4];
  ```

## 6) Otros Aspectos
* La indexación de arreglos y listas empieza en `0`.
* Al definir un arreglo de enteros `a:Int[n];` de forma predeterminada los `n` espacios serán `0`. Cualquier otro tipo de dato se inicializa de forma *"sucia"*.
* El programa debe contener una función `main` que es el punto de entrada del programa. Esta función no recibe parámetros y retorna un valor entero. El valor `0` representa que el programa no tuvo errores, cualquier otro valor significará que hubo algún error.
