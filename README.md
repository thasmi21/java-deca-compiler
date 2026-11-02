# Deca Compiler — Java Compiler Engineering Project

A compiler for **Deca**, a Java-like programming language, developed as part of a six-week Software Engineering project at **Grenoble INP – Ensimag (2024–2025)**.

Built by a team of five engineering students, the project covers the main stages of compilation, from lexical analysis to assembly code generation, with a strong emphasis on software architecture, automated testing, and collaborative development.

## Features

### Compiler Architecture

- **Lexical analysis:** Tokenization and recognition of language keywords, operators, and symbols using ANTLR.
- **Syntax analysis:** Parsing Deca programs and building an Abstract Syntax Tree (AST).
- **Semantic analysis:** Type checking, contextual verification, and validation of object-oriented constructs.
- **Code generation:** Translation of Deca programs into assembly instructions for the IMA abstract machine.
- **Register management:** Handling register allocation constraints during code generation.
- **Error handling:** Reporting lexical, syntactic, semantic, and runtime errors.

### Multidimensional Array Extension

An additional compiler extension introduces support for:

- One-dimensional and multidimensional arrays.
- Array declarations, initialization, and indexed access.
- Type and dimension verification.
- Memory allocation and assembly code generation for array operations.
- A mathematical library supporting array and matrix operations.

## Technologies

| Technology | Purpose |
|---|---|
| Java | Compiler implementation |
| ANTLR4 | Lexer and parser generation |
| Maven | Build and dependency management |
| Git / GitLab | Version control and team collaboration |
| JaCoCo | Code coverage analysis |
| IMA Assembly | Target language and execution environment |
| Bash | Test automation |

## Software Engineering Practices

The project emphasized software quality and structured development:

- **Test-Driven Development (TDD):** Tests used to guide implementation, particularly during code generation.
- **Automated testing:** Tests covering lexical, syntactic, semantic, and code generation stages.
- **Regression testing:** Detecting unintended changes throughout development.
- **Code reviews:** Improving correctness, readability, and maintainability.
- **Team collaboration:** Coordinating development, validation, documentation, and integration across five contributors.
- **Performance analysis:** Evaluating the execution cost of generated assembly instructions.

A GitLab CI pipeline was also explored, although it was not fully operational at the end of the project.

## My Contributions

My personal contributions focused primarily on:

- Developing tests and validating compiler functionality, particularly during the syntax analysis stage.
- Contributing to the team's software quality and regression-testing efforts.
- Participating in code reviews, integration, technical documentation, and collaborative project management.

The compiler architecture, code generation, and array extension were developed collaboratively, with different team members leading different components.

## Building and Running

### Prerequisites

- Java Development Kit (JDK)
- Apache Maven
- IMA abstract machine interpreter

### Build

```bash
mvn compile
```

### Run Verification Tests

```bash
mvn verify
```

### Compile a Deca Program

```bash
decac example.deca
```

This generates an assembly file named `example.ass`.

### Execute the Generated Assembly

```bash
ima example.ass
```

### Useful Compiler Options

```bash
decac -p example.deca  # Parse and decompile
decac -v example.deca  # Semantic verification only
decac -n example.deca  # Disable selected runtime checks
decac -r 8 example.deca # Limit available registers
```

The `decac` executable and IMA interpreter must be available in the execution environment.

## Performance Analysis

The project also included an evaluation of the computational cost of generated IMA assembly.

For a simple variable-access example, the team compared two addressing strategies:

| Strategy | Instruction count | Execution cost |
|---|---:|---:|
| Direct addressing | 6 | 47 cycles |
| Indirect addressing | 9 | 60 cycles |

This analysis informed code generation decisions, favoring direct addressing where appropriate.

## Key Takeaways

This project provided hands-on experience in:

- Compiler construction and programming language fundamentals.
- Object-oriented software architecture.
- Automated testing and software verification.
- Low-level code generation and performance considerations.
- Collaborative engineering under tight deadlines.

## Project Information

**Institution:** Grenoble INP – Ensimag  
**Course:** Software Engineering  
**Academic year:** 2024–2025  
**Duration:** 6 weeks  
**Team:** 5 engineering students

**Contributors:** Lucas Zivkovic, Benjamin Sullet, Samy Zouggari, Fiè Victor Traoré, and Mathis Hurstel.

---

*Academic team project developed for educational purposes.*
