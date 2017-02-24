import java.util.ArrayList;
import java.util.HashMap;

import java.util.Scanner;

public class Interpreter {

	ArrayList<Token> program;
	int pc;
	Token currentToken;
	HashMap<String, Integer> symbols;
	HashMap<String, Integer> subs;


	public Interpreter(String filename) throws Exception {

		Lexer lexer = new Lexer(filename);
		this.program = new ArrayList<Token>();
		Token t;
		this.pc = 0;

		this.symbols = new HashMap<String, Integer>();
		this.subs = new HashMap<String, Integer>();

		// Read all of the program tokens into an ArrayList
		do {
			t = lexer.nextToken();
			this.program.add(t);
		} while (t.type != TokenType.EOF);

		// Get the first Token
		this.pc = 0;
		this.currentToken = this.program.get(this.pc);
	}


	public void consume(TokenType expected) throws Exception {

		// If the currentToken matches the specified token, consume it and
		// advance to the next token. If not, throw an error.

		if (this.currentToken.type != expected) {
			throw new Exception("Expected " + expected + ", but found " + currentToken.type + "."); 
		}

		this.pc++;
		
		if (this.pc < this.program.size()) {
			this.currentToken = this.program.get(this.pc);
		}
	}


	public int evalFactor() throws Exception {
		// A factor is either a literal number, a variable reference, or
		// another expression in parentheses
		int value = 0;

		if (this.currentToken.type == TokenType.NUMBER) {
			value = Integer.parseInt(this.currentToken.value);
			this.consume(TokenType.NUMBER);
		} else if (this.currentToken.type == TokenType.NAME) { // Variable
			String name = this.currentToken.value;
			this.consume(TokenType.NAME);
			
			if (this.symbols.containsKey(name)) {
				return this.symbols.get(name);
			} else {
				throw new Exception("Unrecognized symbol: " + name); 
			}

		} else if (this.currentToken.type == TokenType.LEFT_PAREN) {
			this.consume(TokenType.LEFT_PAREN);
			value = this.evalExpression();
			this.consume(TokenType.RIGHT_PAREN);
		} else {
			throw new Exception("Expected number, variable name, or parenthesized expression, found " + this.currentToken.type); 
		}

		return value;
	}



	public int evalUnaryTerm() throws Exception {
		// A unary expression is either a factor or a factor
		// prefixed by a unary negation operator
		if (this.currentToken.type == TokenType.MINUS) {
			this.consume(TokenType.MINUS);
			int value = this.evalFactor();
			return -value;
		} else {
			return this.evalFactor(); 
		}
	}


	public int evalTerm() throws Exception {
		// A term consists of at least one factor ,followed by any 
		// number of terms separated by multiplication and division operations
		int value = this.evalUnaryTerm();

		while (this.currentToken.type == TokenType.TIMES ||
				this.currentToken.type == TokenType.DIVIDE ||
				this.currentToken.type == TokenType.MOD) {
			if (this.currentToken.type == TokenType.TIMES) {
				this.consume(TokenType.TIMES);
				value = value * this.evalUnaryTerm();
			} else if (this.currentToken.type == TokenType.DIVIDE) {
				this.consume(TokenType.DIVIDE); 
				value = value / this.evalUnaryTerm();
			} else {
				this.consume(TokenType.MOD); 
				value = value % this.evalUnaryTerm();
			}
		} 

		return value;
	}


	public int evalExpression() throws Exception {
		// An expression consists of at least one term ,followed
		// by any number of terms separated by plus and minus operations
		int value = this.evalTerm();

		while (this.currentToken.type == TokenType.PLUS || this.currentToken.type == TokenType.MINUS) {
			if (this.currentToken.type == TokenType.PLUS) {
				this.consume(TokenType.PLUS);
				value = value + this.evalTerm();
			} else {
				this.consume(TokenType.MINUS); 
				value = value - this.evalTerm();
			}
		}

		return value;
	}


	public int evalConditional() throws Exception {
		// An expression consists of at least one arithmetic expression,
		// possibly joined to a second by a relational operator
		int value = this.evalExpression();

		// Add additional cases for the other relational operators
		if (this.currentToken.type == TokenType.LESS_THAN ||
			this.currentToken.type == TokenType.LESS_THAN_OR_EQUAL ||
			this.currentToken.type == TokenType.GREATER_THAN ||
			this.currentToken.type == TokenType.GREATER_THAN_OR_EQUAL ||
			this.currentToken.type == TokenType.NOT_EQUAL ||
			this.currentToken.type == TokenType.EQUAL) {

			if (this.currentToken.type == TokenType.LESS_THAN) {
				this.consume(TokenType.LESS_THAN);
				int lhs = value;
				int rhs = this.evalExpression();
				if (lhs < rhs) {
					return 1;
				} else {
					return 0; 
				}
			}
			
			else if (this.currentToken.type == TokenType.LESS_THAN_OR_EQUAL) {
				this.consume(TokenType.LESS_THAN_OR_EQUAL);
				int lhs = value;
				int rhs = this.evalExpression();
				if (lhs <= rhs) {
					return 1;
				} else {
					return 0;
				}
			}

			else if (this.currentToken.type == TokenType.GREATER_THAN) {
				this.consume(TokenType.GREATER_THAN);
				int lhs = value;
				int rhs = this.evalExpression();
				if (lhs > rhs) {
					return 1;
				} else {
					return 0; 
				}
			}
			
			else if (this.currentToken.type == TokenType.GREATER_THAN_OR_EQUAL) {
				this.consume(TokenType.GREATER_THAN_OR_EQUAL);
				int lhs = value;
				int rhs = this.evalExpression();
				if (lhs >= rhs) {
					return 1;
				} else {
					return 0;
				}
			}
			
			else if (this.currentToken.type == TokenType.NOT_EQUAL) {
				this.consume(TokenType.NOT_EQUAL);
				int lhs = value;
				int rhs = this.evalExpression();
				if (lhs != rhs) {
					return 1;
				} else {
					return 0;
				}
			}
			
			else if (this.currentToken.type == TokenType.EQUAL) {
				this.consume(TokenType.EQUAL);
				int lhs = value;
				int rhs = this.evalExpression();
				if (lhs == rhs) {
					return 1;
				} else {
					return 0;
				}
			}
		}

		return value;
	}


	public void evalAssignmentStatement() throws Exception {

		// An assignment has the form NAME := EXPRESSION
		String lhs = this.currentToken.value;
		this.consume(TokenType.NAME);

		if (this.currentToken.type != TokenType.ASSIGN) {
			throw new Exception("Unrecognized assignment symbol.");
		}
		
		this.consume(TokenType.ASSIGN);  // match the :=

		int value = this.evalExpression(); 
		this.symbols.put(lhs, value); 
	}


	public void evalPrintStatement() throws Exception {

		// Match the print token that got us here
		this.consume(TokenType.PRINT); 
		
		while (true) {
			if (this.currentToken.type == TokenType.STRING) {
				System.out.print(this.currentToken.value);
				this.consume(TokenType.STRING);
			} else {
				System.out.print(this.evalExpression());
			}
			if (this.currentToken.type == TokenType.NEWLINE) {
				break;
			}
			this.consume(TokenType.COMMA);
		}
		
		System.out.println();
	}


	public void evalInputStatement() throws Exception {

		this.consume(TokenType.INPUT);

		String name = this.currentToken.value;
		this.consume(TokenType.NAME);

		Scanner scan = new Scanner(System.in);
		System.out.print("Enter a value for " + name + ": ");
		int value = scan.nextInt();

		this.symbols.put(name, value);
	}
	
	
	public void evalIfStatement() throws Exception {
		// Evaluate if statements, 1 for true and 0 for false
		// if --> variables/condition --> colon
		this.consume(TokenType.IF);
		int cond = this.evalConditional();
		if (this.currentToken.type != TokenType.COLON)
			throw new Exception("Missing COLON after condition.");
		else
			this.consume(TokenType.COLON);
		
		if (cond == 1) {
			while (true) {
				if (this.currentToken.type == TokenType.ELSE) {
					while (true) {
						if (this.currentToken.type == TokenType.ENDIF) {
							this.consume(TokenType.ENDIF);
							return;
						}
						this.consume(this.currentToken.type);
					}
				}
				this.evalStatement();
				if (this.currentToken.type == TokenType.ENDIF) {
					this.consume(TokenType.ENDIF);
					return;
				}
				if (this.currentToken.type == TokenType.COMMENT) this.consume(TokenType.COMMENT);
				if (this.currentToken.type == TokenType.NEWLINE) this.consume(TokenType.NEWLINE);
			}
		} 
		
		else {
			while (true) {
				if (this.currentToken.type == TokenType.ELSE) {
					this.consume(TokenType.ELSE);
					this.consume(TokenType.COLON);
					while (this.currentToken.type != TokenType.ENDIF) {
						this.evalStatement();
						this.consume(TokenType.NEWLINE);
					}
				}
				if (this.currentToken.type == TokenType.ENDIF) {
					this.consume(TokenType.ENDIF);
					break;
				}
				this.consume(this.currentToken.type);
			}
		}
		
	}
	

	public void evalWhileStatement() throws Exception {
		// Evaluate while statements
		int reset = this.pc;
		this.consume(TokenType.WHILE);
		int bool = this.evalConditional();
		if (this.currentToken.type != TokenType.COLON)
			throw new Exception("Missing COLON after condition.");
		this.consume(TokenType.COLON);
		this.consume(TokenType.NEWLINE);
		while (bool == 1) {
			while (this.currentToken.type != TokenType.ENDWHILE) {
				if (this.currentToken.type == TokenType.END)
					throw new Exception("Missing ENDWHILE.");
				this.evalStatement();
				this.consume(TokenType.NEWLINE);
			}
			this.pc = reset;
			this.currentToken = this.program.get(this.pc);
			this.consume(TokenType.WHILE);
			bool = this.evalConditional();
			this.consume(TokenType.COLON);
		}
		
		if (bool == 0) {
			while (true) {
				if (this.currentToken.type == TokenType.ENDWHILE) {
					this.consume(TokenType.ENDWHILE);
					break;
				}
				this.consume(this.currentToken.type);
			}
		}
	}
	
	
	public void evalSubRoutine() throws Exception {
		// Evaluates sub routines; only takes in the subroutine name and saves it with its
		// corresponding pc, then consumes everything until endsub. ALSO checks to see if
		// there is a missing endsub at the end
		this.consume(TokenType.SUB);
		String subName = this.currentToken.value;
		this.consume(TokenType.NAME);
		this.consume(TokenType.COLON);
		this.consume(TokenType.NEWLINE);
		int reset = this.pc;
		this.subs.put(subName, reset);			// Put sub name and pc number to HashMap
		
		while (true) {
			if (this.currentToken.type == TokenType.ENDSUB) {
				this.consume(TokenType.ENDSUB);
				break;
			} else if (this.currentToken.type == TokenType.END) {
				throw new Exception("Missing ENDSUB.");
			}
			this.consume(this.currentToken.type);
		}
	}
	
	
	public void evalCallStatement() throws Exception {
		this.consume(TokenType.CALL);
		String subName = this.currentToken.value;
		this.consume(TokenType.NAME);
		this.pc = this.subs.get(subName);
		this.currentToken = this.program.get(this.pc);				// currentToken == SUB
		while (this.currentToken.type != TokenType.RETURN) {
			this.evalStatement();
			if (this.currentToken.type == TokenType.COMMENT) this.consume(TokenType.COMMENT);
			if (this.currentToken.type == TokenType.NEWLINE) this.consume(TokenType.NEWLINE);
		}
		while (true) {
			if (this.currentToken.value == subName) {
				this.consume(TokenType.NAME);		// consume name of subroutine
				break;
			}
			this.consume(this.currentToken.type);
		}
	}
	
	
	public void evalForStatement() throws Exception {
		this.consume(TokenType.FOR);
		String var = this.currentToken.value;
		this.consume(TokenType.NAME);
		this.consume(TokenType.ASSIGN);
		int val = this.evalExpression();
		this.symbols.put(var, val);			// put var i and start val into symbols
		this.consume(this.currentToken.type);		// consume 'to' in for statement
		this.symbols.put(var + "*", this.evalExpression());		// condition for end is variableName*
		this.consume(TokenType.COLON);
		this.consume(TokenType.NEWLINE);
		int reset = this.pc;
		
		while (true) {
			if (this.currentToken.type == TokenType.ENDFOR
					&& this.symbols.get(var) >= this.symbols.get(var + "*")) {
				this.consume(TokenType.ENDFOR);
				break;
			} else if (this.symbols.get(var) > this.symbols.get(var + "*")) {
				// when loop condition is checked at top and is false
				// consumes all tokens until the end
				while (true) {
					if (this.currentToken.type == TokenType.ENDFOR)
						break;
					this.consume(this.currentToken.type);
				}
			} else if (this.currentToken.type == TokenType.ENDFOR) {
				this.symbols.put(var, this.symbols.get(var) + 1);
				this.pc = reset;
				this.currentToken = this.program.get(this.pc);
			} else if (this.currentToken.type == TokenType.END) {
				throw new Exception("Missing ENDFOR.");
			} else {				
				this.evalStatement();
				if (this.currentToken.type == TokenType.COMMENT) this.consume(TokenType.COMMENT);
				if (this.currentToken.type == TokenType.NEWLINE) this.consume(TokenType.NEWLINE);
			}
		}
	}
	
	
	public void evalStatement() throws Exception {
		// There are different types of statements: select the 
		// appropriate case based on the currentToken
		switch(this.currentToken.type) {

		// Add new cases here for each new statement type:
		// INPUT, IF, WHILE, CALL, SUB, RETURN, FOR
		case CALL:
			this.evalCallStatement();
			break;
			
		case FOR:
			this.evalForStatement();
			break;
		
		case IF:
			this.evalIfStatement();
			break;

		case INPUT:
			this.evalInputStatement();
			break;

		case NAME:
			this.evalAssignmentStatement();
			break;

		case NEWLINE:  // Empty statement
			break;

		case PRINT:
			this.evalPrintStatement();
			break;
			
		case RETURN:
			break;
			
		case SUB:
			this.evalSubRoutine();
			break;
		
		case WHILE:
			this.evalWhileStatement();
			break;

			// Cases corresponding to the end of blocks
			// Treated as empty

		case COMMENT:
			break;
			
		case ELSE:
			break;
		
		case END:
			break;

		case ENDIF:			
			break;

		case ENDWHILE:
			break;

		case ENDSUB:
			break;

			// Unrecognized token error
		default:
			throw new Exception("Unexpected token: " + this.currentToken.type);
		}
	}


	public void evalStatementBlock() throws Exception {
		// A program is at least one statement followed
		// by any number of optional statements separated by newlines
		this.evalStatement();
		
		while (this.currentToken.type == TokenType.NEWLINE ||
				this.currentToken.type == TokenType.COMMENT) {
			if (this.currentToken.type == TokenType.NEWLINE) this.consume(TokenType.NEWLINE);
			else if (this.currentToken.type == TokenType.COMMENT) this.consume(TokenType.COMMENT);
			this.evalStatement();
		}
	}

	public void evalProgram() throws Exception {
		// Assumes that user will include program, program name, and colon
		if (this.currentToken.type == TokenType.COMMENT) {
			this.consume(TokenType.COMMENT);
		}
		
		while (this.currentToken.type == TokenType.NEWLINE) {
			this.consume(TokenType.NEWLINE);
		}
		
		if (this.currentToken.type == TokenType.PROGRAM) {
			this.consume(TokenType.PROGRAM);
			this.consume(TokenType.NAME);
			this.consume(TokenType.COLON);
		}
		this.evalStatementBlock();
	}


	public static void main(String[] args) {

		try {
			Interpreter interpreter = new Interpreter("Test/Extra/ListAllPrimes.a");
			interpreter.evalProgram();  // Start with evalProgram, which you'll need to write
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}