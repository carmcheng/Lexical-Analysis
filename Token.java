// A Token recognized by the lexical analyzer
// DSM, 2017

enum TokenType {
   NAME, NUMBER, LESS_THAN, GREATER_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL,
   EQUAL, NOT_EQUAL, ASSIGN, PLUS, MINUS, TIMES, DIVIDE, EOF, IF, WHILE, THEN, DO,
   NEWLINE, UNKNOWN, AND, COLON, COMMA, COMMENT, MOD, VAR, SUB, PRINT, ENDIF, ENDSUB, 
   ENDWHILE, LEFT_PAREN, RIGHT_PAREN, INPUT, RETURN, NOT, OR, CALL, END, PROGRAM, FOR,
   STRING, ENDFOR, ELSE
}

public class Token {
   TokenType type;
   String value;  // Some tokens, like IDENT, have an associated value
   
   public Token(TokenType type) {
     this.type = type;
     this.value = null;
   }
   
   public Token(TokenType type, String value) {
     this.type = type;
     this.value = value;
   }
   
   public String toString() {
     if (this.type == TokenType.NAME || this.type == TokenType.NUMBER) {
       return "<" + this.type + ", " + this.value + ">";
     } else {
        return "<" + this.type + ">"; 
     }
   }
}