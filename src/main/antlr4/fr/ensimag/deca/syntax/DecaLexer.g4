lexer grammar DecaLexer;

options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@members {
}

// Deca lexer rules.
// A FAIRE : Règle bidon qui reconnait tous les caractères.
// A FAIRE : Il faut la supprimer et la remplacer par les vraies règles.

fragment STRING_CAR: ~ ('"' | '\\' );

ASM : 'asm';

CLASS : 'class';

EXTENDS : 'extends';

ELSE : 'else';

FALSE : 'false';

IF : 'if';

INSTANCEOF: 'instanceof';

NEW : 'new';

NULL : 'null';

READFLOAT : 'readFloat';

PROTECTED : 'protected';

THIS : 'this';

TRUE : 'true';

WS :    ( ' '
        | '\t'
        |'\r'
        | '\n'
        ) {
            skip(); // avoid producing a token
        }
    ;

OBRACE : '{' ;

PRINTLN : 'println';

PRINT : 'print';

PRINTX : 'printx';

PRINTLNX : 'printlnx';

WHILE : 'while';

RETURN : 'return';

COMMENT : ('/*' .*? '*/'
          | '//' ~[\r\n]*
          ){
          skip();
           };

OPARENT : '(' ;

CPARENT : ')' ;

OCROCHET : '[';

CCROCHET : ']';

CBRACE : '}' ;

SEMI : ';' ;
READINT : 'readInt' ;
STRING : '"' (STRING_CAR | '\\"' | '\\\\')* '"';

// suivi de la convention de fragment expliqué dans le poly
// pour ajouter les priorités
fragment LETTER : [a-zA-Z] ;
fragment DIGIT : [0-9] ;
fragment POSITIVE_DIGIT : '1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9';
IDENT : (LETTER | '$' | '_')(LETTER | DIGIT | '$' | '_' )* ;
INT : '0' | POSITIVE_DIGIT DIGIT* ;

fragment NUM: DIGIT+;
fragment SIGN : '+' | '-' | ' ' ;
fragment EXP : ('E' | 'e') SIGN NUM ;
fragment DEC : NUM '.' NUM ;
fragment FLOATDEC : (DEC | DEC EXP) ('F' | 'f')? ;
fragment DIGITTHEX : [0-9A-Fa-f] ;
fragment NUMHEX : DIGITTHEX+ ;
fragment FLOATHEX : ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') SIGN NUM ('F' | 'f' | ' ');


// FLOAT est prioritaire sur DEC
FLOAT : FLOATDEC | FLOATHEX ;


// Les règles NUM et DEC sont définies après FLOAT

EQUALS : '=' ;

TIMES : '*';

AND : '&&';

PLUS : '+';

MINUS : '-';

SLASH : '/';

PERCENT : '%';

DOT : '.';

EXCLAM : '!';

LEQ : '<=';

GEQ : '>=';

GT : '>';

LT : '<';

EQEQ : '==';

NEQ : '!=';

OR : '||';

EOL : ( '\t' |'\r'| '\n');

COMMA : ',';

MULTI_LINE_STRING: '"' (STRING_CAR | EOL | '\\"' | '\\\\')* '"';