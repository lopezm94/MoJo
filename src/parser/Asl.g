

grammar Asl;

options {
    output = AST;
    ASTLabelType = AslTree;
}

// Imaginary tokens to create some AST nodes

tokens {
    LIST_FUNCTIONS;   // List of functions (the root of the tree)
    ASSIGN;           // Assignment instruction
    PARAMS;           // List of parameters in the declaration of a function
    FUNCALL;          // Function call
    ARGLIST;          // List of arguments passed in a function call
    LIST_INSTR;       // Block of instructions
    BOOLEAN;          // Boolean atom (for Boolean constants "true" or "false")
    PVALUE;           // Parameter by value in the list of parameters
    PREF;             // Parameter by reference in the list of parameters
    ACCESS;           // Access element contents
    LIST;             // List
    COLUMN;           // Columna
    FROM_ACTIONS;     // Action to make inside block 'from'
    DICT;
}

@header {
package parser;
import interp.AslTree;
}

@lexer::header {
package parser;
}


// A program is a list of functions
prog	: func (('\r'? '\n')* func)* ('\r'? '\n')* EOF -> ^(LIST_FUNCTIONS func+)
        ;

// A function has a name, a list of parameters and a block of instructions
func  : FUNC^ ID params block_instructions END!
      ;

// The list of parameters grouped in a subtree (it can be empty)
params	: '(' paramlist? ')' -> ^(PARAMS paramlist?)
        ;

// Parameters are separated by commas
paramlist: param (','! param)*
        ;

// Parameters with & as prefix are passed by reference
// Only one node with the name of the parameter is created
param   :   '&' id=ID -> ^(PREF[$id,$id.text])
        |   id=ID -> ^(PVALUE[$id,$id.text])
        ;

// A list of instructions, all of them gouped in a subtree
block_instructions
        :	 instruction ((';' | '\r'? '\n') instruction)*
            -> ^(LIST_INSTR instruction+)
        ;

//******************************************** TODO table operations ************************************************************************************
// The different types of instructions
instruction
        : assign          // Assignment
        | ite_stmt        // if-then-else
        | while_stmt      // while statement
        | funcall         // Call to a procedure (no result produced)
        | return_stmt     // Return statement
        | read            // Read a variable
        | write           // Write a string or an expression
        | from            // Special instructions for tables
        |                 // Nothing
        ;

// Assignment
assign	:	var eq=EQUAL expr -> ^(ASSIGN[$eq,":="] var expr)
        ;

// if-then-else (else is optional)
ite_stmt	:	IF^ '('! expr ')'! block_instructions (ELSE! block_instructions)? END!
            ;

// while statement
while_stmt	:	WHILE^ '('! expr ')'! block_instructions END!
            ;

// Return statement with an expression
return_stmt	:	RETURN^ expr?
        ;

// Read a variable
read  :	READ^ ID
      ;

// Write an expression or a string
write :   WRITE^ expr
      |   WRITELN^ expr
      ;

// Grammar for expressions with boolean, relational and aritmetic operators
expr  :   boolterm (OR^ boolterm)*
      ;

boolterm:   boolfact (AND^ boolfact)*
        ;

boolfact:   num_expr ((EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) num_expr)?
        ;

num_expr:   term ( (PLUS^ | MINUS^) term)*
        ;

term    :   factor ( (MUL^ | DIV^ | MOD^) factor)*
        ;

factor  :   (NOT^ | PLUS^ | MINUS^)? atom
        ;

// Atom of the expressions (variables, integer and boolean literals).
// An atom can also be a function call or another expression
// in parenthesis
atom    :   var
        |   INT
        |   (b=TRUE | b=FALSE)  -> ^(BOOLEAN[$b,$b.text])
        |   funcall
        |   STRING
        |   '('! expr ')'!
        |   column
        |   list
        |   from
        |   dict
        ;

list    :   '[' expr_list? ']' -> ^(LIST expr_list?)
        ;

dict    :   '{' dict_list? '}' -> ^(DICT dict_list?)
        ;

dict_list : STRING ':'! expr (','! STRING ':'! expr)*
          ;

column  :   ':' name -> ^(COLUMN name)
        ;

name    :   STRING
        |   var
        |   INT
        ;

var     :   ID
        |   ID '[' expr_list ']' -> ^(ACCESS ID ^(ARGLIST expr_list))
        ;

from    :   FROM expr from_instructions END -> ^(FROM expr ^(FROM_ACTIONS from_instructions));

from_instructions   :   from_instruction ((';'! | '\r'!? '\n'!) from_instruction)*
                    ;

from_instruction    :   select
                    |   filter
                    |   update
                    |
                    ;

select  :   SELECT^ expr;

filter  :   FILTER^ expr;

update  :   UPDATE^ expr (WHEN! expr)? WITH! expr;

// A function call has a list of arguments in parenthesis (possibly empty)
funcall :   ID '(' expr_list? ')' -> ^(FUNCALL ID ^(ARGLIST expr_list?))
        ;

// A list of expressions separated by commas
expr_list :  expr (','! expr)*
          ;

// Basic tokens
EQUAL     : '=' ;
NOT_EQUAL : '!=' ;
LT        : '<' ;
LE        : '<=';
GT        : '>';
GE        : '>=';
PLUS      : '+' ;
MINUS	    : '-' ;
MUL	      : '*';
DIV	      : '/';
MOD	      : '%' ;
NOT	      : 'not';
AND	      : 'and';
OR        : 'or' ;
IF        : 'if' ;
THEN      : 'then' ;
ELSE      : 'else' ;
WHILE     : 'while' ;
FUNC      : 'function' ;
END       : 'end' ;
RETURN    : 'return' ;
READ      : 'read' ;
SELECT    : 'select';
UPDATE    : 'update';
WHEN      : 'when';
FROM      : 'from';
FILTER    : 'filter';
WRITE     : 'write' ;
WRITELN   : 'writeln' ;
TRUE      : 'true' ;
FALSE     : 'false';
WITH      : 'with';
ID        :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* '!'?;
INT       :	'0'..'9'+ ;

// C-style comments
COMMENT	: '#' ~('-') ~('\n'|'\r')* '\r'? {$channel=HIDDEN;}
    	| '#-' ( options {greedy=false;} : . )* '-#' {$channel=HIDDEN;}
    	;

// Strings (in quotes) with escape sequences
STRING  :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
        ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;

// White spaces
WS  	: ( ' '
        | '\t'
        | '\r'
        ) {$channel=HIDDEN;}
    	;
