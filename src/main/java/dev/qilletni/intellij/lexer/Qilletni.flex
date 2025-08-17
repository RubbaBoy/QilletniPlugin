/*
  Qilletni.flex — Full lexer mirroring reference/QilletniLexer.g4
*/
package dev.qilletni.intellij.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import static dev.qilletni.intellij.psi.QilletniTypes.*;

%%
%public
%class _QilletniLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%final
%state STR ORDER_MODE LIMIT_MODE WEIGHT_MODE

// Macros
Digits = [0-9]+
Minus = -
IdStart = [a-zA-Z_]
IdPart = [a-zA-Z_0-9]
Ws = [ \t\r\n]+

%%

<YYINITIAL>{
  {Ws}                          { return TokenType.WHITE_SPACE; }

  "//"[^\r\n]*                { return LINE_COMMENT; }
  "/**"([^*]|\*+[^/])*"*/"    { return DOC_COMMENT; }
  "/\*"([^*]|\*+[^/])*"\*/"   { return BLOCK_COMMENT; }

  // Keywords and typed tokens
  "import"                     { return IMPORT; }
  "as"                         { return AS; }
  "weights"                    { return WEIGHTS_KEYWORD; }
  "any"                        { return ANY_TYPE; }
  "int"                        { return INT_TYPE; }
  "double"                     { return DOUBLE_TYPE; }
  "string"                     { return STRING_TYPE; }
  "boolean"                    { return BOOLEAN_TYPE; }
  "collection"                 { return COLLECTION_TYPE; }
  "song"                       { return SONG_TYPE; }
  "album"                      { return ALBUM_TYPE; }
  "java"                       { return JAVA_TYPE; }
  "entity"                     { return ENTITY; }
  "empty"                      { return EMPTY; }
  "new"                        { return NEW; }
  "order"                      { yybegin(ORDER_MODE); return ORDER_PARAM; }
  "limit"                      { yybegin(LIMIT_MODE); return LIMIT_PARAM; }
  "loop"                       { return LOOP_PARAM; }
  "is"                         { return IS_KEYWORD; }
  "if"                         { return IF_KEYWORD; }
  "else"                       { return ELSE_KEYWORD; }
  "for"                        { return FOR_KEYWORD; }
  "infinity"                   { return RANGE_INFINITY; }
  ":"                          { return COLON; }
  "play"                       { return PLAY; }
  "provider"                   { return PROVIDER; }
  "fun"                        { return FUNCTION_DEF; }
  "static"                     { return STATIC; }
  "native"                     { return NATIVE; }
  "on"                         { return ON; }
  "return"                     { return RETURN; }
  "by"                         { return BY; }
  "created"                    { return CREATED; }
  "true"|"false"              { return BOOL; }

  // Operators and punctuation
  "++"                         { return INCREMENT; }
  "--"                         { return DECREMENT; }
  "+="                         { return PLUS_EQUALS; }
  "-="                         { return MINUS_EQUALS; }
  "-"                          { return MINUS; }
  "+"                          { return PLUS; }
  "*"                          { return STAR; }
  "/~"                         { return FLOOR_DIV; }
  "/"                          { return DIV; }
  "%"                          { return MOD; }
  "&&"                         { return ANDAND; }
  "||"                         { return OROR; }
  "."                          { return DOT; }
  ".."                         { return DOUBLE_DOT; }
  ">="                         { return REL_OP; }
  "<="                         { return REL_OP; }
  "=="                         { return REL_OP; }
  "!="                         { return REL_OP; }
  ">"                          { return REL_OP; }
  "<"                          { return REL_OP; }
  "("                          { return LEFT_PAREN; }
  ")"                          { return RIGHT_PAREN; }
  "["                          { return LEFT_SBRACKET; }
  "]"                          { return RIGHT_SBRACKET; }
  "{"                          { return LEFT_CBRACKET; }
  "}"                          { return RIGHT_CBRACKET; }
  ","                          { return COMMA; }
  "="                          { return ASSIGN; }
  "!"                          { return NOT; }

  // Weight pipe starts WEIGHT_MODE
  "|!" | "|~" | "|"           { yybegin(WEIGHT_MODE); return WEIGHT_PIPE; }

  // Strings: enter STR mode
  "\""                        { yybegin(STR); /* consume content in STR, emit STRING on close */ }

  // Numbers
  {Minus}?{Digits}              { return INT; }
  {Minus}?({Digits}"D"|{Digits}"."{Digits}"D"?) { return DOUBLE; }

  // Identifier
  {IdStart}{IdPart}*            { return ID; }

  .                              { return TokenType.BAD_CHARACTER; }
}

// String mode: accumulate content; emit STRING on closing quote
<STR>{
  "\\\""                      { /* escaped quote, continue */ }
  "\\".                        { /* any escape sequence, continue */ }
  [^"\\]+                      { /* text, continue */ }
  "\""                        { yybegin(YYINITIAL); return STRING; }
}

// ORDER mode for collection order brackets
<ORDER_MODE>{
  {Ws}                         { return TokenType.WHITE_SPACE; }
  "["                         { return LEFT_SBRACKET; }
  "sequential"|"shuffle"      { return COLLECTION_ORDER; }
  "]"                         { yybegin(YYINITIAL); return RIGHT_SBRACKET; }
  .                            { return TokenType.BAD_CHARACTER; }
}

// LIMIT mode for limit amounts
<LIMIT_MODE>{
  {Ws}                         { return TokenType.WHITE_SPACE; }
  "["                         { return LEFT_SBRACKET; }
  "]"                         { yybegin(YYINITIAL); return RIGHT_SBRACKET; }
  {Digits}                     { return INT; }
  "s"|"m"|"h"                 { return LIMIT_UNIT; }
  .                            { return TokenType.BAD_CHARACTER; }
}

// WEIGHT mode for weight_amount and unit, then pop
<WEIGHT_MODE>{
  {Ws}                         { return TokenType.WHITE_SPACE; }
  {Minus}?{Digits}             { return INT; }
  "x"|"%"                     { yybegin(YYINITIAL); return WEIGHT_UNIT; }
  .                            { return TokenType.BAD_CHARACTER; }
}