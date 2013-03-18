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
    if (level > 0){
        Report.warning("Unclosed comment (line: " + commentStartLine + ", column: " + commentStartColumn + ")");
    }
    return new PascalSym(PascalTok.EOF);
%eofval}
%eofclose

%{
    int level = 0;
    int commentStartLine = 0;
    int commentStartColumn = 0;

    private PascalSym sym(int type) {
        return new PascalSym(type, yyline + 1, yycolumn + 1, yytext());
    }
    private PascalSym sym(int type, String sym) {
        return new PascalSym(type, yyline + 1, yycolumn + 1, sym);
    }
%}

%eof{
%eof}

%state YYCOMMENT
%state YYCHARCONST

letter          = [A-Za-z]
digit           = [0-9]
alphanumeric	= {letter}|{digit}
integer_const	= {digit}+
char_const      = ''''|'[\x20-\x26\x28-\x7E]'
identifier      = ({letter}|_)({alphanumeric}|_)*
whitespace		= [ \n\t]
comment_body	= [^{}]*

%%

<YYINITIAL> {
    {whitespace}+       { /* ignore whitespace */ }
    and                 { return sym(PascalTok.AND); }
    array               { return sym(PascalTok.ARRAY); }
    begin               { return sym(PascalTok.BEGIN); }
    const               { return sym(PascalTok.CONST); }
    div                 { return sym(PascalTok.DIV); }
    do                  { return sym(PascalTok.DO); }
    else                { return sym(PascalTok.ELSE); }
    end                 { return sym(PascalTok.END); }
    for                 { return sym(PascalTok.FOR); }
    function            { return sym(PascalTok.FUNCTION); }
    if                  { return sym(PascalTok.IF); }
    nil                 { return sym(PascalTok.NIL); }
    not                 { return sym(PascalTok.NOT); }
    of                  { return sym(PascalTok.OF); }
    or                  { return sym(PascalTok.OR); }
    procedure           { return sym(PascalTok.PROCEDURE); }
    program             { return sym(PascalTok.PROGRAM); }
    record              { return sym(PascalTok.RECORD); }
    then                { return sym(PascalTok.THEN); }
    to                  { return sym(PascalTok.TO); }
    type                { return sym(PascalTok.TYPE); }
    var                 { return sym(PascalTok.VAR); }
    while               { return sym(PascalTok.WHILE); }

    /* symbols */
    ":="                { return sym(PascalTok.ASSIGN); }
    ":"                 { return sym(PascalTok.COLON); }
    ","                 { return sym(PascalTok.COMMA); }
    "."                 { return sym(PascalTok.DOT); }
    ".."                { return sym(PascalTok.DOTS); }
    "["                 { return sym(PascalTok.LBRACKET); }
    "("                 { return sym(PascalTok.LPARENTHESIS); }
    "]"                 { return sym(PascalTok.RBRACKET); }
    ")"                 { return sym(PascalTok.RPARENTHESIS); }
    ";"                 { return sym(PascalTok.SEMIC); }
    "+"                 { return sym(PascalTok.ADD); }
    "="                 { return sym(PascalTok.EQU); }
    ">="                { return sym(PascalTok.GEQ); }
    ">"                 { return sym(PascalTok.GTH); }
    "<"                 { return sym(PascalTok.LTH); }
    "<="                { return sym(PascalTok.LEQ); }
    "*"                 { return sym(PascalTok.MUL); }
    "<>"                { return sym(PascalTok.NEQ); }
    "^"                 { return sym(PascalTok.PTR); }
    "-"                 { return sym(PascalTok.SUB); }

    /* atomic types */
    boolean             { return sym(PascalTok.BOOL); }
    char                { return sym(PascalTok.CHAR); }
    integer             { return sym(PascalTok.INT); }

    /* constants for atomic types */
    "true"|"false"     { return sym(PascalTok.BOOL_CONST); }
    {integer_const}    { return sym(PascalTok.INT_CONST); }

    {char_const}       { return sym(PascalTok.CHAR_CONST); }

    /* identifiers */
    {identifier}        { return sym(PascalTok.IDENTIFIER); }

    /* comments */
    "{"                { level++;
                            commentStartLine = yyline;
                            commentStartColumn = yycolumn;
                            yybegin(YYCOMMENT);
                        }

    /* everything else */
    .                   { Report.warning("Can not understand '" + yytext() + "' (line: " +
                            yyline + ", column: " + yycolumn + ")");
                        }
}
<YYCOMMENT> {
    "{"                 { level++; }
    "}"                 { level--; if(level==0) yybegin(YYINITIAL); }
    {comment_body}      { /* ignore omments */ }
}