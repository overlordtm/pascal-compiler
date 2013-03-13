package compiler.lexanal;

import java.io.*;

import compiler.report.*;
import compiler.synanal.*;

%%

%class      PascalLex
%public

%line
%column

/* Vzpostavimo zdruzljivost z orodjem Java Cup.
 * To bi lahko naredili tudi z ukazom %cup,
 * a v tem primeru ne bi mogli uporabiti razreda compiler.lexanal.PascalSym
 * namesto razreda java_cup.runtime.Symbol za opis osnovnih simbolov. */
%cupsym     compiler.synanal.PascalTok
%implements java_cup.runtime.Scanner
%function   next_token
%type       PascalSym
%eofval{
    return new PascalSym(PascalTok.EOF);
%eofval}
%eofclose

%{
    private PascalSym sym(int type) {
        return new PascalSym(type, yyline + 1, yycolumn + 1, yytext());
    }
%}

%eof{
%eof}

letter          = [A-Za-z]
digit           = [0-9]
alphanumeric    = {letter}|{digit}

%%

[ \n\t]+						{ }

and					{ return sym(PascalTok.AND); }
array				{ return sym(PascalTok.ARRAY); }
begin				{ return sym(PascalTok.BEGIN); }
const				{ return sym(PascalTok.CONST); }
div					{ return sym(PascalTok.DIV); }
do					{ return sym(PascalTok.DO); }
else				{ return sym(PascalTok.ELSE); }
end					{ return sym(PascalTok.END); }
for					{ return sym(PascalTok.FOR); }
function			{ return sym(PascalTok.FUNCTION); }
if					{ return sym(PascalTok.IF); }
nil					{ return sym(PascalTok.NIL); }
not					{ return sym(PascalTok.NOT); }
of					{ return sym(PascalTok.OF); }
or					{ return sym(PascalTok.OR); }
procedure			{ return sym(PascalTok.PROCEDURE); }
program				{ return sym(PascalTok.PROGRAM); }
record				{ return sym(PascalTok.RECORD); }
then				{ return sym(PascalTok.THEN); }
to					{ return sym(PascalTok.TO); }
type				{ return sym(PascalTok.TYPE); }
var					{ return sym(PascalTok.VAR); }
while				{ return sym(PascalTok.WHILE); }

"true"|"false"		{ return sym(PascalTok.BOOL_CONST); }
