// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import dev.qilletni.intellij.psi.impl.*;

public interface QilletniTypes {

  IElementType ADDITIVE_EXPR = new IElementType("ADDITIVE_EXPR", null);
  IElementType ALBUM_EXPR = new IElementType("ALBUM_EXPR", null);
  IElementType ALBUM_URL_OR_NAME_PAIR = new IElementType("ALBUM_URL_OR_NAME_PAIR", null);
  IElementType ASMT = new IElementType("ASMT", null);
  IElementType BODY = new IElementType("BODY", null);
  IElementType BODY_STMT = new IElementType("BODY_STMT", null);
  IElementType COLLECTION_EXPR = new IElementType("COLLECTION_EXPR", null);
  IElementType COLLECTION_LIMIT = new IElementType("COLLECTION_LIMIT", null);
  IElementType COLLECTION_URL_OR_NAME_PAIR = new IElementType("COLLECTION_URL_OR_NAME_PAIR", null);
  IElementType DOUBLE_EXPR = new IElementType("DOUBLE_EXPR", null);
  IElementType ELSEIF_LIST = new IElementType("ELSEIF_LIST", null);
  IElementType ELSE_BODY = new IElementType("ELSE_BODY", null);
  IElementType ENTITY_BODY = new IElementType("ENTITY_BODY", null);
  IElementType ENTITY_CONSTRUCTOR = new IElementType("ENTITY_CONSTRUCTOR", null);
  IElementType ENTITY_DEF = new IElementType("ENTITY_DEF", null);
  IElementType ENTITY_INITIALIZE = new IElementType("ENTITY_INITIALIZE", null);
  IElementType ENTITY_PROPERTY_DECLARATION = new IElementType("ENTITY_PROPERTY_DECLARATION", null);
  IElementType EXPR = new IElementType("EXPR", null);
  IElementType EXPR_LIST = new IElementType("EXPR_LIST", null);
  IElementType FOREACH_RANGE = new IElementType("FOREACH_RANGE", null);
  IElementType FOR_EXPR = new IElementType("FOR_EXPR", null);
  IElementType FOR_STMT = new IElementType("FOR_STMT", null);
  IElementType FUNCTION_CALL = new IElementType("FUNCTION_CALL", null);
  IElementType FUNCTION_DEFINITION = new IElementType("FUNCTION_DEFINITION", null);
  IElementType FUNCTION_DEF_PARAMS = new IElementType("FUNCTION_DEF_PARAMS", null);
  IElementType FUNCTION_ON_TYPE = new IElementType("FUNCTION_ON_TYPE", null);
  IElementType IF_STMT = new IElementType("IF_STMT", null);
  IElementType IMMUTABLE_POSTFIX_EXPR_SUFFIX = new IElementType("IMMUTABLE_POSTFIX_EXPR_SUFFIX", null);
  IElementType IMPORT_FILE = new IElementType("IMPORT_FILE", null);
  IElementType INT_EXPR = new IElementType("INT_EXPR", null);
  IElementType IS_EXPR = new IElementType("IS_EXPR", null);
  IElementType JAVA_EXPR = new IElementType("JAVA_EXPR", null);
  IElementType LIMIT_AMOUNT = new IElementType("LIMIT_AMOUNT", null);
  IElementType LIST_EXPRESSION = new IElementType("LIST_EXPRESSION", null);
  IElementType LOGICAL_AND_EXPR = new IElementType("LOGICAL_AND_EXPR", null);
  IElementType LOGICAL_OR_EXPR = new IElementType("LOGICAL_OR_EXPR", null);
  IElementType MULTIPLICATIVE_EXPR = new IElementType("MULTIPLICATIVE_EXPR", null);
  IElementType ORDER_DEFINE = new IElementType("ORDER_DEFINE", null);
  IElementType PLAY_STMT = new IElementType("PLAY_STMT", null);
  IElementType POSTFIX_EXPR = new IElementType("POSTFIX_EXPR", null);
  IElementType POSTFIX_SUFFIX = new IElementType("POSTFIX_SUFFIX", null);
  IElementType PRIMARY_EXPR = new IElementType("PRIMARY_EXPR", null);
  IElementType PROG = new IElementType("PROG", null);
  IElementType PROVIDER_STMT = new IElementType("PROVIDER_STMT", null);
  IElementType RANGE_EXPR = new IElementType("RANGE_EXPR", null);
  IElementType RELATIONAL_EXPR = new IElementType("RELATIONAL_EXPR", null);
  IElementType RETURN_STMT = new IElementType("RETURN_STMT", null);
  IElementType RUNNING = new IElementType("RUNNING", null);
  IElementType SINGLE_WEIGHT = new IElementType("SINGLE_WEIGHT", null);
  IElementType SONG_EXPR = new IElementType("SONG_EXPR", null);
  IElementType SONG_URL_OR_NAME_PAIR = new IElementType("SONG_URL_OR_NAME_PAIR", null);
  IElementType STMT = new IElementType("STMT", null);
  IElementType STR_EXPR = new IElementType("STR_EXPR", null);
  IElementType UNARY_EXPR = new IElementType("UNARY_EXPR", null);
  IElementType WEIGHTS_DEFINE = new IElementType("WEIGHTS_DEFINE", null);
  IElementType WEIGHTS_EXPR = new IElementType("WEIGHTS_EXPR", null);
  IElementType WEIGHT_AMOUNT = new IElementType("WEIGHT_AMOUNT", null);

  IElementType ALBUM_TYPE = new IElementType("ALBUM_TYPE", null);
  IElementType ANDAND = new IElementType("ANDAND", null);
  IElementType ANY_TYPE = new IElementType("ANY_TYPE", null);
  IElementType AS = new IElementType("AS", null);
  IElementType ASSIGN = new IElementType("ASSIGN", null);
  IElementType BOOL = new IElementType("BOOL", null);
  IElementType BOOLEAN_TYPE = new IElementType("BOOLEAN_TYPE", null);
  IElementType BY = new IElementType("BY", null);
  IElementType COLLECTION_ORDER = new IElementType("COLLECTION_ORDER", null);
  IElementType COLLECTION_TYPE = new IElementType("COLLECTION_TYPE", null);
  IElementType COLON = new IElementType("COLON", null);
  IElementType COMMA = new IElementType("COMMA", null);
  IElementType DECREMENT = new IElementType("DECREMENT", null);
  IElementType DIV = new IElementType("DIV", null);
  IElementType DOC_COMMENT = new IElementType("DOC_COMMENT", null);
  IElementType DOT = new IElementType("DOT", null);
  IElementType DOUBLE = new IElementType("DOUBLE", null);
  IElementType DOUBLE_DOT = new IElementType("DOUBLE_DOT", null);
  IElementType DOUBLE_TYPE = new IElementType("DOUBLE_TYPE", null);
  IElementType ELSE_BODY_1_0 = new IElementType("else_body_1_0", null);
  IElementType ELSE_KEYWORD = new IElementType("ELSE_KEYWORD", null);
  IElementType EMPTY = new IElementType("EMPTY", null);
  IElementType ENTITY = new IElementType("ENTITY", null);
  IElementType FLOOR_DIV = new IElementType("FLOOR_DIV", null);
  IElementType FOR_KEYWORD = new IElementType("FOR_KEYWORD", null);
  IElementType FUNCTION_DEF = new IElementType("FUNCTION_DEF", null);
  IElementType ID = new IElementType("ID", null);
  IElementType IF_KEYWORD = new IElementType("IF_KEYWORD", null);
  IElementType IMPORT = new IElementType("IMPORT", null);
  IElementType INCREMENT = new IElementType("INCREMENT", null);
  IElementType INT = new IElementType("INT", null);
  IElementType INT_TYPE = new IElementType("INT_TYPE", null);
  IElementType IS_KEYWORD = new IElementType("IS_KEYWORD", null);
  IElementType JAVA_TYPE = new IElementType("JAVA_TYPE", null);
  IElementType LEFT_CBRACKET = new IElementType("LEFT_CBRACKET", null);
  IElementType LEFT_PAREN = new IElementType("LEFT_PAREN", null);
  IElementType LEFT_SBRACKET = new IElementType("LEFT_SBRACKET", null);
  IElementType LIMIT_PARAM = new IElementType("LIMIT_PARAM", null);
  IElementType LIMIT_UNIT = new IElementType("LIMIT_UNIT", null);
  IElementType LOOP_PARAM = new IElementType("LOOP_PARAM", null);
  IElementType MINUS = new IElementType("MINUS", null);
  IElementType MINUS_EQUALS = new IElementType("MINUS_EQUALS", null);
  IElementType MOD = new IElementType("MOD", null);
  IElementType NATIVE = new IElementType("NATIVE", null);
  IElementType NEW = new IElementType("NEW", null);
  IElementType NEWLINE = new IElementType("NEWLINE", null);
  IElementType NOT = new IElementType("NOT", null);
  IElementType ON = new IElementType("ON", null);
  IElementType ORDER_PARAM = new IElementType("ORDER_PARAM", null);
  IElementType OROR = new IElementType("OROR", null);
  IElementType PLAY = new IElementType("PLAY", null);
  IElementType PLUS = new IElementType("PLUS", null);
  IElementType PLUS_EQUALS = new IElementType("PLUS_EQUALS", null);
  IElementType PROVIDER = new IElementType("PROVIDER", null);
  IElementType RANGE_INFINITY = new IElementType("RANGE_INFINITY", null);
  IElementType REL_OP = new IElementType("REL_OP", null);
  IElementType RETURN = new IElementType("RETURN", null);
  IElementType RIGHT_CBRACKET = new IElementType("RIGHT_CBRACKET", null);
  IElementType RIGHT_PAREN = new IElementType("RIGHT_PAREN", null);
  IElementType RIGHT_SBRACKET = new IElementType("RIGHT_SBRACKET", null);
  IElementType SONG_TYPE = new IElementType("SONG_TYPE", null);
  IElementType STAR = new IElementType("STAR", null);
  IElementType STATIC = new IElementType("STATIC", null);
  IElementType STRING = new IElementType("STRING", null);
  IElementType STRING_TYPE = new IElementType("STRING_TYPE", null);
  IElementType WEIGHTS_KEYWORD = new IElementType("WEIGHTS_KEYWORD", null);
  IElementType WEIGHT_PIPE = new IElementType("WEIGHT_PIPE", null);
  IElementType WEIGHT_UNIT = new IElementType("WEIGHT_UNIT", null);

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ADDITIVE_EXPR) {
        return new AdditiveExprImpl(node);
      }
      else if (type == ALBUM_EXPR) {
        return new AlbumExprImpl(node);
      }
      else if (type == ALBUM_URL_OR_NAME_PAIR) {
        return new AlbumUrlOrNamePairImpl(node);
      }
      else if (type == ASMT) {
        return new AsmtImpl(node);
      }
      else if (type == BODY) {
        return new BodyImpl(node);
      }
      else if (type == BODY_STMT) {
        return new BodyStmtImpl(node);
      }
      else if (type == COLLECTION_EXPR) {
        return new CollectionExprImpl(node);
      }
      else if (type == COLLECTION_LIMIT) {
        return new CollectionLimitImpl(node);
      }
      else if (type == COLLECTION_URL_OR_NAME_PAIR) {
        return new CollectionUrlOrNamePairImpl(node);
      }
      else if (type == DOUBLE_EXPR) {
        return new DoubleExprImpl(node);
      }
      else if (type == ELSEIF_LIST) {
        return new ElseifListImpl(node);
      }
      else if (type == ELSE_BODY) {
        return new ElseBodyImpl(node);
      }
      else if (type == ENTITY_BODY) {
        return new EntityBodyImpl(node);
      }
      else if (type == ENTITY_CONSTRUCTOR) {
        return new EntityConstructorImpl(node);
      }
      else if (type == ENTITY_DEF) {
        return new EntityDefImpl(node);
      }
      else if (type == ENTITY_INITIALIZE) {
        return new EntityInitializeImpl(node);
      }
      else if (type == ENTITY_PROPERTY_DECLARATION) {
        return new EntityPropertyDeclarationImpl(node);
      }
      else if (type == EXPR) {
        return new ExprImpl(node);
      }
      else if (type == EXPR_LIST) {
        return new ExprListImpl(node);
      }
      else if (type == FOREACH_RANGE) {
        return new ForeachRangeImpl(node);
      }
      else if (type == FOR_EXPR) {
        return new ForExprImpl(node);
      }
      else if (type == FOR_STMT) {
        return new ForStmtImpl(node);
      }
      else if (type == FUNCTION_CALL) {
        return new FunctionCallImpl(node);
      }
      else if (type == FUNCTION_DEFINITION) {
        return new FunctionDefImpl(node);
      }
      else if (type == FUNCTION_DEF_PARAMS) {
        return new FunctionDefParamsImpl(node);
      }
      else if (type == FUNCTION_ON_TYPE) {
        return new FunctionOnTypeImpl(node);
      }
      else if (type == IF_STMT) {
        return new IfStmtImpl(node);
      }
      else if (type == IMMUTABLE_POSTFIX_EXPR_SUFFIX) {
        return new ImmutablePostfixExprSuffixImpl(node);
      }
      else if (type == IMPORT_FILE) {
        return new ImportFileImpl(node);
      }
      else if (type == INT_EXPR) {
        return new IntExprImpl(node);
      }
      else if (type == IS_EXPR) {
        return new IsExprImpl(node);
      }
      else if (type == JAVA_EXPR) {
        return new JavaExprImpl(node);
      }
      else if (type == LIMIT_AMOUNT) {
        return new LimitAmountImpl(node);
      }
      else if (type == LIST_EXPRESSION) {
        return new ListExpressionImpl(node);
      }
      else if (type == LOGICAL_AND_EXPR) {
        return new LogicalAndExprImpl(node);
      }
      else if (type == LOGICAL_OR_EXPR) {
        return new LogicalOrExprImpl(node);
      }
      else if (type == MULTIPLICATIVE_EXPR) {
        return new MultiplicativeExprImpl(node);
      }
      else if (type == ORDER_DEFINE) {
        return new OrderDefineImpl(node);
      }
      else if (type == PLAY_STMT) {
        return new PlayStmtImpl(node);
      }
      else if (type == POSTFIX_EXPR) {
        return new PostfixExprImpl(node);
      }
      else if (type == POSTFIX_SUFFIX) {
        return new PostfixSuffixImpl(node);
      }
      else if (type == PRIMARY_EXPR) {
        return new PrimaryExprImpl(node);
      }
      else if (type == PROG) {
        return new ProgImpl(node);
      }
      else if (type == PROVIDER_STMT) {
        return new ProviderStmtImpl(node);
      }
      else if (type == RANGE_EXPR) {
        return new RangeExprImpl(node);
      }
      else if (type == RELATIONAL_EXPR) {
        return new RelationalExprImpl(node);
      }
      else if (type == RETURN_STMT) {
        return new ReturnStmtImpl(node);
      }
      else if (type == RUNNING) {
        return new RunningImpl(node);
      }
      else if (type == SINGLE_WEIGHT) {
        return new SingleWeightImpl(node);
      }
      else if (type == SONG_EXPR) {
        return new SongExprImpl(node);
      }
      else if (type == SONG_URL_OR_NAME_PAIR) {
        return new SongUrlOrNamePairImpl(node);
      }
      else if (type == STMT) {
        return new StmtImpl(node);
      }
      else if (type == STR_EXPR) {
        return new StrExprImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new UnaryExprImpl(node);
      }
      else if (type == WEIGHTS_DEFINE) {
        return new WeightsDefineImpl(node);
      }
      else if (type == WEIGHTS_EXPR) {
        return new WeightsExprImpl(node);
      }
      else if (type == WEIGHT_AMOUNT) {
        return new WeightAmountImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
