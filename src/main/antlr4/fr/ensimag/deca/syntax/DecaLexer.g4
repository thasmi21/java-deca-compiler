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

WS :    ( ' '
        | '\t'
        |'\r'
        | '\n'
        ) {
            skip(); // avoid producing a token
        }
    ;

OBRACE : '{' {skip();};

PRINTLN : 'println';

COMMENT : '/' .? '*/'
    { skip(); };

OPARENT : '(' { skip();};

CPARENT : ')' {skip();};

CBRACE : '}' {skip();};

SEMI : ';' {skip();};

