package frontend.lexer;

import frontend.error.Error;
import frontend.error.ErrorRecorder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;

public class Lexer {
    private final PushbackInputStream reader;
    private final ArrayList<Token> tokens;
    private int lineNumber;
    private char currentChar;


    public Lexer(PushbackInputStream reader) throws IOException {
        this.reader = reader;
        tokens = new ArrayList<>();
        lineNumber = 1;
        currentChar = (char) reader.read();
    }

    public void analyse() throws IOException {
        while (notEnd()) {
            StringBuilder sb = new StringBuilder();
            // 空白符
            if (Character.isWhitespace(currentChar)) {
                next();
            }
            // 字母或下划线
            else if (Character.isLetter(currentChar) || currentChar == '_') {
                do {
                    sb.append(currentChar);
                    next();
                } while (Character.isLetter(currentChar) || Character.isDigit(currentChar) || currentChar == '_');
                String content = sb.toString();
                // IDENFR 与 关键字
                tokens.add(new Token(switch (content) {
                    case "const" -> Token.Type.CONSTTK;
                    case "int" -> Token.Type.INTTK;
                    case "static" -> Token.Type.STATICTK;
                    case "break" -> Token.Type.BREAKTK;
                    case "continue" -> Token.Type.CONTINUETK;
                    case "if" -> Token.Type.IFTK;
                    case "main" -> Token.Type.MAINTK;
                    case "else" -> Token.Type.ELSETK;
                    case "for" -> Token.Type.FORTK;
                    case "return" -> Token.Type.RETURNTK;
                    case "void" -> Token.Type.VOIDTK;
                    case "printf" -> Token.Type.PRINTFTK;
                    default -> Token.Type.IDENFR;
                }, content, lineNumber));
            }
            // INTCON
            else if (currentChar == '0') {
                tokens.add(new Token(Token.Type.INTCON, "0", lineNumber));
                next();
            } else if (Character.isDigit(currentChar)) {
                do {
                    sb.append(currentChar);
                    next();
                } while (Character.isDigit(currentChar));
                tokens.add(new Token(Token.Type.INTCON, sb.toString(), lineNumber));
            }
            // STRCON
            else if (currentChar == '"') {
                do {
                    sb.append(currentChar);
                    next();
                } while (currentChar != '"' && notEnd());
                sb.append(currentChar);
                tokens.add(new Token(Token.Type.STRCON, sb.toString(), lineNumber));
                next();
            }
            // NEQ 和 NOT
            else if (currentChar == '!') {
                next();
                if (currentChar == '=') {
                    tokens.add(new Token(Token.Type.NEQ, "!=", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(Token.Type.NOT, "!", lineNumber));
                }
            }
            // AND 和 a类错误
            else if (currentChar == '&') {
                next();
                if (currentChar == '&') {
                    tokens.add(new Token(Token.Type.AND, "&&", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(Token.Type.AND, "&&", lineNumber));
                    error();
                }
            }
            // OR 和 a类错误
            else if (currentChar == '|') {
                next();
                if (currentChar == '|') {
                    tokens.add(new Token(Token.Type.OR, "||", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(Token.Type.OR, "||", lineNumber));
                    error();
                }
            }
            // LEQ 和 LSS
            else if (currentChar == '<') {
                next();
                if (currentChar == '=') {
                    tokens.add(new Token(Token.Type.LEQ, "<=", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(Token.Type.LSS, "<", lineNumber));
                }
            }
            // GEQ 和 GRE
            else if (currentChar == '>') {
                next();
                if (currentChar == '=') {
                    tokens.add(new Token(Token.Type.GEQ, ">=", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(Token.Type.GRE, ">", lineNumber));
                }
            }
            // EQL 和 ASSIGN
            else if (currentChar == '=') {
                next();
                if (currentChar == '=') {
                    tokens.add(new Token(Token.Type.EQL, "==", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(Token.Type.ASSIGN, "=", lineNumber));
                }
            }
            // 注释 和 除法
            else if (currentChar == '/') {
                next();
                if (currentChar == '/') {
                    do {
                        next();
                    } while (currentChar != '\n' && notEnd());
                    next();
                } else if (currentChar == '*') {
                    char lc;
                    next();
                    do {
                        lc = currentChar;
                        next();
                    } while (!(lc == '*' && currentChar == '/') && notEnd());
                    next();
                } else {
                    tokens.add(new Token(Token.Type.DIV, "/", lineNumber));
                }
            } else {
                sb.append(currentChar);
                tokens.add(new Token(
                        switch (currentChar) {
                            case '+' -> Token.Type.PLUS;
                            case '-' -> Token.Type.MINU;
                            case '*' -> Token.Type.MULT;
                            case '%' -> Token.Type.MOD;
                            case ';' -> Token.Type.SEMICN;
                            case ',' -> Token.Type.COMMA;
                            case '(' -> Token.Type.LPARENT;
                            case ')' -> Token.Type.RPARENT;
                            case '[' -> Token.Type.LBRACK;
                            case ']' -> Token.Type.RBRACK;
                            case '{' -> Token.Type.LBRACE;
                            default -> Token.Type.RBRACE;
                        }, sb.toString(), lineNumber)
                );
                next();
            }
        }
    }

    private void next() throws IOException {
        currentChar = (char) reader.read();
        if (currentChar == '\n') {
            lineNumber++;
        }
    }

    private boolean notEnd() {
        return currentChar != '\uFFFF'; // EOF = -1, 转为 char 即是 255 (Unicode最大字符)
    }

    private void error() {
        ErrorRecorder.addError(new Error(Error.Type.a, lineNumber));
    }

    public ArrayList<Token> getTokenList() {
        return tokens;
    }
}
