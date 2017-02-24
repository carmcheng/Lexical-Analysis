// An example lexical analyzer that recognizes relational operators and identifiers
// DSM, 2017

import java.io.PushbackReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Lexer {
  
  // PushbackReader supports pushing characters back on the input stream
  PushbackReader reader = null;
  int lineNumber;

  
  public Lexer(String filename) throws FileNotFoundException {
    
    // Create a PushbackReader that can accept up to 1 pushbacks
    this.reader = new PushbackReader(new FileReader(filename), 1);
    lineNumber = 1;
  }
  
  
  public Token analyzeIdentifier() throws IOException {
    
    // Collect characters in a StringBuilder
    StringBuilder identifier = new StringBuilder();
    
    while (true) {
      int c = this.reader.read();
      
      if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_') {
        reader.unread(c);
        break;
      } else {
        identifier.append((char) c);
      }
    }
    
    String identString = identifier.toString();
    
    // Check if the name's value is a keyword
    if (identString.equalsIgnoreCase("if")) {
     return new Token(TokenType.IF); 
    } else if (identString.equalsIgnoreCase("while")) {
     return new Token(TokenType.WHILE); 
    } else if (identString.equalsIgnoreCase("then")) {
     return new Token(TokenType.THEN); 
    } else if (identString.equalsIgnoreCase("do")) {
     return new Token(TokenType.DO); 
    } else if (identString.equalsIgnoreCase("and")) {
    	return new Token(TokenType.AND);
    } else if (identString.equalsIgnoreCase("var")) {
    	return new Token(TokenType.VAR);
    } else if (identString.equalsIgnoreCase("print")) {
    	return new Token(TokenType.PRINT);
    } else if (identString.equalsIgnoreCase("endif")) {
    	return new Token(TokenType.ENDIF);
    } else if (identString.equalsIgnoreCase("endsub")) {
    	return new Token(TokenType.ENDSUB);
    } else if (identString.equalsIgnoreCase("endwhile")) {
    	return new Token(TokenType.ENDWHILE);
    } else if (identString.equalsIgnoreCase("input")) {
    	return new Token(TokenType.INPUT);
    } else if (identString.equalsIgnoreCase("sub")) {
    	return new Token(TokenType.SUB);
    } else if (identString.equalsIgnoreCase("return")) {
    	return new Token(TokenType.RETURN);
    } else if (identString.equalsIgnoreCase("not")) {
    	return new Token(TokenType.NOT);
    } else if (identString.equalsIgnoreCase("or")) {
    	return new Token(TokenType.OR);
    } else if (identString.equalsIgnoreCase("call")) {
    	return new Token(TokenType.CALL);
    } else if (identString.equalsIgnoreCase("end")) {
    	return new Token(TokenType.END);
    } else if (identString.equalsIgnoreCase("program")) {
    	return new Token(TokenType.PROGRAM);
    } else if (identString.equalsIgnoreCase("for")) {
    	return new Token(TokenType.FOR);
    } else if (identString.equalsIgnoreCase("endfor")) {
    	return new Token(TokenType.ENDFOR);
    } else if (identString.equalsIgnoreCase("else")) {
    	return new Token(TokenType.ELSE);
    } else {
       return new Token(TokenType.NAME, identString);
    }
  }
  
  
  public Token analyzeNumber() throws IOException {
    
    // Basic strategy: read characters in a loop and add them to
    // the value of the number we're building until we read something
    // that's not a digit
    
    int value = 0;
    
    while (true) {
      int c = this.reader.read();
      
      if (Character.isDigit(c)) {
        value = value * 10 + Character.getNumericValue(c);
      } else {
        this.reader.unread(c);
        break;
      }
    }
    
    return new Token(TokenType.NUMBER, String.valueOf(value));
  }
  
  
  public Token nextToken() throws Exception {
    
    while (true) {
    
      int c = reader.read();
      
      // End-of-file
      if (c == -1) {
        return new Token(TokenType.EOF);
      }
      
      // Newline
      if (c == '\n') {
    	  lineNumber++;
    	  return new Token(TokenType.NEWLINE);
      }
      
      // A single = is an EQUAL token
      else if (c == '=') {
        return new Token(TokenType.EQUAL); 
      }
      
      // Two tokens start with >
      else if (c == '>') {
        int next = this.reader.read();
        
        if (next == '=') {
          return new Token(TokenType.GREATER_THAN_OR_EQUAL); 
        } else {
          this.reader.unread(next);
          return new Token(TokenType.GREATER_THAN);
        }
      }
      
      // Three tokens start with <
      else if (c == '<') {
        int next = this.reader.read();
        
        if (next == '=') {
          return new Token(TokenType.LESS_THAN_OR_EQUAL); 
        } else if (next == '>') {
          return new Token(TokenType.NOT_EQUAL);
        } else {
          this.reader.unread(next);
          return new Token(TokenType.LESS_THAN);
        }
      }
      
      // Basic Arithmetic
      else if (c == '+') {
        return new Token(TokenType.PLUS); 
      }
      
      else if (c == '-') {
        return new Token(TokenType.MINUS);
      }
      
      else if (c == '*') {
        return new Token(TokenType.TIMES); 
      }
      
      else if (c == '/') {
        return new Token(TokenType.DIVIDE); 
      }
      
      // First character is a letter
      else if (Character.isLetter(c)) {
        
        // Push it back on the stack, then call analyzeIdentifier
        reader.unread(c);
        return this.analyzeIdentifier();
      }
      
      // First character is a digit
      else if (Character.isDigit(c)) {
        reader.unread(c);
        return this.analyzeNumber();
      }
      
      // This is a special case to catch a bug in PushbackReader.
      // If you unread() a -1, which can easily happen if the last
      // token in the program is a NAME or NUMBER, the PushbackReader
      // converts it internally to 65535, and will return that value
      // the next time you call read().
      else if (c == 65535) {
        continue;
      }
      
      // Add more cases for other tokens
      
      // Two tokens that start with :
      else if (c == ':') {
    	  int next = this.reader.read();
    	  if (next == '=') {
    		  return new Token(TokenType.ASSIGN);
    	  } else {
    		  this.reader.unread(next);
    		  return new Token(TokenType.COLON);
    	  }
      }
      
      // A single , token is a COMMA
      else if (c == ',') {
    	  return new Token(TokenType.COMMA);
      }
      
      // Comments, checks for pair of curly braces
      else if (c == '{') {
    	  while (true) {
    		  int next = this.reader.read();
    		  // Increments lineNumber within while loop
    		  if (next == '\n') lineNumber++;
    		  if (next == '}') {
    			  return new Token(TokenType.COMMENT);
    		  } else if (next == -1) {
    			  // Reaches EOF and doesn't find }
    			  System.out.println("ERROR: Missing }" + ", Line: " + lineNumber);
    	    	  return new Token(TokenType.UNKNOWN);
    		  }
    	  }
      }
      
      // String in double quotes for fancy printing
      else if (c == '"') {
    	// Collect characters in a StringBuilder
    	    StringBuilder identifier = new StringBuilder();
    	  
    	  while (true) {
    		  int next = this.reader.read();
    		  if (next != '"') identifier.append((char) next);
    		  
    		  // Increments lineNumber within while loop
    		  if (next == '\n') lineNumber++;
    		  if (next == '"') {
    			  return new Token(TokenType.STRING, identifier.toString());
    		  } else if (next == -1) {
    			  // Reaches EOF and doesn't find closing "
    			  System.out.println("ERROR: Missing \"" + ", Line: " + lineNumber);
    	    	  return new Token(TokenType.UNKNOWN);
    		  }
    	  }
      }
      
      // Modulus operator
      else if (c == '%') {
    	  return new Token(TokenType.MOD);
      }
      
      // Left and right Parentheses
      else if (c == '(') {
    	  return new Token(TokenType.LEFT_PAREN);
      }
      
      else if (c == ')') {
    	  return new Token(TokenType.RIGHT_PAREN);
      }
      
      // Add an error case that matches any other non-whitespace
      // character. Print an error message with the unmatched character
      // and the line number, then return an UNKNOWN token.
      else if (c != ' ') {
    	  System.out.println("\nERROR!: " + (char) c + ", Line: " + lineNumber);
    	  return new Token(TokenType.UNKNOWN);
      }
    }
  }
  
  
  public static void main(String[] args) {
    
    try {
      Lexer lex = new Lexer("/Test/Basic/Arithmetic.a");
      Token t;
      
      do {
        t = lex.nextToken();
        
        if (t.type == TokenType.NEWLINE) {
          System.out.println(); 
        } else {
          System.out.print(t  + " ");
        }
      } while (t.type != TokenType.EOF && t.type != TokenType.UNKNOWN);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}