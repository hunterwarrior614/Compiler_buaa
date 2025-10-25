package frontend.lexer;

import error.Error;
import error.ErrorRecorder;

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
            // 换行符
            if (currentChar == '\n') {
                lineNumber++;
                next();
            }
            // 空白符
            else if (Character.isWhitespace(currentChar)) {
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
                    case "const" -> TokenType.CONSTTK;
                    case "int" -> TokenType.INTTK;
                    case "static" -> TokenType.STATICTK;
                    case "break" -> TokenType.BREAKTK;
                    case "continue" -> TokenType.CONTINUETK;
                    case "if" -> TokenType.IFTK;
                    case "main" -> TokenType.MAINTK;
                    case "else" -> TokenType.ELSETK;
                    case "for" -> TokenType.FORTK;
                    case "return" -> TokenType.RETURNTK;
                    case "void" -> TokenType.VOIDTK;
                    case "printf" -> TokenType.PRINTFTK;
                    default -> TokenType.IDENFR;
                }, content, lineNumber));
            }
            // INTCON
            else if (currentChar == '0') {
                tokens.add(new Token(TokenType.INTCON, "0", lineNumber));
                next();
            } else if (Character.isDigit(currentChar)) {
                do {
                    sb.append(currentChar);
                    next();
                } while (Character.isDigit(currentChar));
                tokens.add(new Token(TokenType.INTCON, sb.toString(), lineNumber));
            }
            // STRCON
            else if (currentChar == '"') {
                do {
                    sb.append(currentChar);
                    next();
                } while (currentChar != '"' && notEnd());
                sb.append(currentChar);
                tokens.add(new Token(TokenType.STRCON, sb.toString(), lineNumber));
                next();
            }
            // NEQ 和 NOT
            else if (currentChar == '!') {
                next();
                if (currentChar == '=') {
                    tokens.add(new Token(TokenType.NEQ, "!=", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(TokenType.NOT, "!", lineNumber));
                }
            }
            // AND 和 a类错误
            else if (currentChar == '&') {
                next();
                if (currentChar == '&') {
                    tokens.add(new Token(TokenType.AND, "&&", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(TokenType.AND, "&&", lineNumber));
                    error();
                }
            }
            // OR 和 a类错误
            else if (currentChar == '|') {
                next();
                if (currentChar == '|') {
                    tokens.add(new Token(TokenType.OR, "||", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(TokenType.OR, "||", lineNumber));
                    error();
                }
            }
            // LEQ 和 LSS
            else if (currentChar == '<') {
                next();
                if (currentChar == '=') {
                    tokens.add(new Token(TokenType.LEQ, "<=", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(TokenType.LSS, "<", lineNumber));
                }
            }
            // GEQ 和 GRE
            else if (currentChar == '>') {
                next();
                if (currentChar == '=') {
                    tokens.add(new Token(TokenType.GEQ, ">=", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(TokenType.GRE, ">", lineNumber));
                }
            }
            // EQL 和 ASSIGN
            else if (currentChar == '=') {
                next();
                if (currentChar == '=') {
                    tokens.add(new Token(TokenType.EQL, "==", lineNumber));
                    next();
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, "=", lineNumber));
                }
            }
            // 注释 和 除法
            else if (currentChar == '/') {
                next();
                // 单行注释
                if (currentChar == '/') {
                    do {
                        next();
                    } while (currentChar != '\n' && notEnd());
                }
                // 多行注释
                else if (currentChar == '*') {
                    char lc;
                    next();
                    do {
                        if (currentChar == '\n') {
                            lineNumber++;
                        }
                        lc = currentChar;
                        next();
                    } while (!(lc == '*' && currentChar == '/') && notEnd());
                    next();
                }
                // 除法
                else {
                    tokens.add(new Token(TokenType.DIV, "/", lineNumber));
                }
            } else {
                sb.append(currentChar);
                tokens.add(new Token(
                        switch (currentChar) {
                            case '+' -> TokenType.PLUS;
                            case '-' -> TokenType.MINU;
                            case '*' -> TokenType.MULT;
                            case '%' -> TokenType.MOD;
                            case ';' -> TokenType.SEMICN;
                            case ',' -> TokenType.COMMA;
                            case '(' -> TokenType.LPARENT;
                            case ')' -> TokenType.RPARENT;
                            case '[' -> TokenType.LBRACK;
                            case ']' -> TokenType.RBRACK;
                            case '{' -> TokenType.LBRACE;
                            default -> TokenType.RBRACE;
                        }, sb.toString(), lineNumber)
                );
                next();
            }
        }
    }

    private void next() throws IOException {
        currentChar = (char) reader.read();
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
