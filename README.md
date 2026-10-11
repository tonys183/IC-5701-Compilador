# IC-5701 — Compiladores e Intérpretes

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-ED8B00?style=plastic&logo=openjdk&logoColor=white">
  <img alt="Gradle" src="https://img.shields.io/badge/Gradle-02303A?style=plastic&logo=gradle&logoColor=white">
  <img alt="PlantUML" src="https://img.shields.io/badge/-PlantUML-455A64?style=plastic">
  <a href="LICENSE"><img alt="Licencia Apache-2.0" src="https://img.shields.io/github/license/tonys183/IC-5701-Compilador?style=plastic&label=Licencia&logo=opensourceinitiative&logoColor=white"></a>
</p>

<p align="center">
  <a href="#compilador">Compilador</a> · <a href="#documentación">Documentación</a>
</p>

## Compilador

Implementación de un compilador en Java para un lenguaje de programación propio. El proyecto abarca el reconocimiento de tokens, el análisis sintáctico y semántico y la generación de código.

El código se organiza en módulos para el analizador léxico, el parser, el árbol de sintaxis abstracta, la comprobación semántica y la traducción. El repositorio incluye documentación del lenguaje y programas de ejemplo para su procesamiento.

## Documentación

- [Especificación del lenguaje](docs/01%20ESPECIFICACI%C3%93N.md): Describe el lenguaje 3Z, sus tipos de datos, palabras reservadas y construcciones.
- [Analizador léxico](docs/02%20L%C3%89XICO.plantuml): Diagrama en PlantUML del reconocimiento de los componentes léxicos.
- [Gramática](docs/03%20GRAM%C3%81TICA.md): Define las reglas sintácticas de los programas y sus expresiones.
- [Semántica](docs/04%20SEM%C3%81NTICA.md): Explica la resolución de nombres, los ámbitos y las reglas de comprobación de tipos.
- [Traducción](docs/05%20TRADUCCI%C3%93N.md): Detalla las reglas para generar código de la máquina virtual de pila a partir del programa.
