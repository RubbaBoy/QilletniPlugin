/*
  Qilletni.flex — Full lexer mirroring reference/QilletniLexer.g4
*/
package dev.qilletni.intellij.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import dev.qilletni.intellij.psi.QilletniTypes;

import static dev.qilletni.intellij.psi.QilletniTypes.*;

%%
%public
%class QilletniLexer
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

<YYINITIAL,ORDER_MODE,LIMIT_MODE,WEIGHT_MODE>{
  {Ws}                            { return TokenType.WHITE_SPACE; }

  "//"[^\r\n]*                    { return QilletniTypes.LINE_COMMENT; }
  "/\*\*"([^*]|\*+[^/])*"\*/"     { return QilletniTypes.DOC_COMMENT; }
  "/\*"([^*]|\*+[^/])*"\*/"       { return QilletniTypes.BLOCK_COMMENT; }
}


<YYINITIAL>{
  {Ws}                            { return TokenType.WHITE_SPACE; }

  // Keywords and typed tokens
  "import"                        { return QilletniTypes.IMPORT; }
  "as"                            { return QilletniTypes.AS; }
  "weights"                       { return QilletniTypes.WEIGHTS_KEYWORD; }
  "any"                           { return QilletniTypes.ANY_TYPE; }
  "int"                           { return QilletniTypes.INT_TYPE; }
  "double"                        { return QilletniTypes.DOUBLE_TYPE; }
  "string"                        { return QilletniTypes.STRING_TYPE; }
  "boolean"                       { return QilletniTypes.BOOLEAN_TYPE; }
  "collection"                    { return QilletniTypes.COLLECTION_TYPE; }
  "song"                          { return QilletniTypes.SONG_TYPE; }
  "album"                         { return QilletniTypes.ALBUM_TYPE; }
  "java"                          { return QilletniTypes.JAVA_TYPE; }
  "entity"                        { return QilletniTypes.ENTITY; }
  "empty"                         { return QilletniTypes.EMPTY; }
  "new"                           { return QilletniTypes.NEW; }
  "order"                         { yybegin(ORDER_MODE); return ORDER_PARAM; }
  "limit"                         { yybegin(LIMIT_MODE); return LIMIT_PARAM; }
  "loop"                          { return QilletniTypes.LOOP_PARAM; }
  "is"                            { return QilletniTypes.IS_KEYWORD; }
  "if"                            { return QilletniTypes.IF_KEYWORD; }
  "else"                          { return QilletniTypes.ELSE_KEYWORD; }
  "for"                           { return QilletniTypes.FOR_KEYWORD; }
  "infinity"                      { return QilletniTypes.RANGE_INFINITY; }
  ":"                             { return QilletniTypes.COLON; }
  "play"                          { return QilletniTypes.PLAY; }
  "provider"                      { return QilletniTypes.PROVIDER; }
  "fun"                           { return QilletniTypes.FUNCTION_DEF; }
  "static"                        { return QilletniTypes.STATIC; }
  "native"                        { return QilletniTypes.NATIVE; }
  "on"                            { return QilletniTypes.ON; }
  "return"                        { return QilletniTypes.RETURN; }
  "by"                            { return QilletniTypes.BY; }
  "true"|"false"                  { return QilletniTypes.BOOL; }

  // Operators and punctuation
  "++"                            { return QilletniTypes.INCREMENT; }
  "--"                            { return QilletniTypes.DECREMENT; }
  "+="                            { return QilletniTypes.PLUS_EQUALS; }
  "-="                            { return QilletniTypes.MINUS_EQUALS; }
  "-"                             { return QilletniTypes.MINUS; }
  "+"                             { return QilletniTypes.PLUS; }
  "*"                             { return QilletniTypes.STAR; }
  "/~"                            { return QilletniTypes.FLOOR_DIV; }
  "/"                             { return QilletniTypes.DIV; }
  "%"                             { return QilletniTypes.MOD; }
  "&&"                            { return QilletniTypes.ANDAND; }
  "||"                            { return QilletniTypes.OROR; }
  ".."                            { return QilletniTypes.DOUBLE_DOT; }
  "."                             { return QilletniTypes.DOT; }
  ">="|"<="|"=="|"!="|">"|"<"     { return QilletniTypes.REL_OP; }
  "("                             { return QilletniTypes.LEFT_PAREN; }
  ")"                             { return QilletniTypes.RIGHT_PAREN; }
  "["                             { return QilletniTypes.LEFT_SBRACKET; }
  "]"                             { return QilletniTypes.RIGHT_SBRACKET; }
  "{"                             { return QilletniTypes.LEFT_CBRACKET; }
  "}"                             { return QilletniTypes.RIGHT_CBRACKET; }
  ","                             { return QilletniTypes.COMMA; }
  "="                             { return QilletniTypes.ASSIGN; }
  "!"                             { return QilletniTypes.NOT; }

  // Weight pipe starts WEIGHT_MODE
  "|!" | "|~" | "|"               { yybegin(WEIGHT_MODE); return QilletniTypes.WEIGHT_PIPE; }

  // Strings: enter STR mode
  "\""                            { yybegin(STR); /* consume content in STR, emit STRING on close */ }

  // Numbers
  {Minus}?{Digits}                { return QilletniTypes.INT; }
  {Minus}?({Digits}"D"|{Digits}"."{Digits}"D"?) { return QilletniTypes.DOUBLE; }

  // Identifier
  {IdStart}{IdPart}*              { return QilletniTypes.ID; }

  .                                { return TokenType.BAD_CHARACTER; }
}

// String mode: accumulate content; emit STRING on closing quote
<STR>{
  "\\\""                          { /* escaped quote, continue */ }
  "\\".                           { /* any escape sequence, continue */ }
  [^\n\r\"\\]+                    { /* text, continue */ }
  "\""                            { yybegin(YYINITIAL); return QilletniTypes.STRING; }
  \r?\n                           { yybegin(YYINITIAL); return QilletniTypes.STRING; }  // unterminated string line-break
  <<EOF>>                         { yybegin(YYINITIAL); return QilletniTypes.STRING; }  // unterminated string at EOF
}

// ORDER mode for collection order brackets
<ORDER_MODE>{
  {Ws}                            { return TokenType.WHITE_SPACE; }
  "["                             { return QilletniTypes.LEFT_SBRACKET; }
  "sequential"|"shuffle"          { return QilletniTypes.COLLECTION_ORDER; }
  "]"                             { yybegin(YYINITIAL); return QilletniTypes.RIGHT_SBRACKET; }
  <<EOF>>                         { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
  .                               { return TokenType.BAD_CHARACTER; }
}

// LIMIT mode for limit amounts
<LIMIT_MODE>{
  {Ws}                            { return TokenType.WHITE_SPACE; }
  "["                             { return QilletniTypes.LEFT_SBRACKET; }
  "]"                             { yybegin(YYINITIAL); return QilletniTypes.RIGHT_SBRACKET; }
  {Digits}                        { return QilletniTypes.INT; }
  "s"|"m"|"h"                     { return QilletniTypes.LIMIT_UNIT; }
  <<EOF>>                         { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
  .                               { return TokenType.BAD_CHARACTER; }
}

// WEIGHT mode for weight_amount and unit, then pop
<WEIGHT_MODE>{
  {Ws}                            { return TokenType.WHITE_SPACE; }
  {Minus}?{Digits}                { return QilletniTypes.INT; }
  "x"|"%"                         { yybegin(YYINITIAL); return QilletniTypes.WEIGHT_UNIT; }
  <<EOF>>                         { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
  .                               { return TokenType.BAD_CHARACTER; }
}