package frontend.lexer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private final ArrayList<Error> errors = new ArrayList<>();
    private final String input;

    private int lineno = 1;
    private int pos = -1;
    private char c;


    public Lexer(String input) {
        this.input = input + "\0\0";
        analyse();
    }

    private void analyse() {
        nextSym();
        while (notEnd()) {
            StringBuilder sb = new StringBuilder();
            // 空白符
            if (Character.isWhitespace(c)) {
                nextSym();
            }
            // 字母或下划线
            else if (Character.isLetter(c) || c == '_') {
                do {
                    sb.append(c);
                    nextSym();
                } while (Character.isLetter(c) || Character.isDigit(c) || c == '_');
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
                }, content));
            }
            // INTCON
            else if (c == '0') {
                tokens.add(new Token(Token.Type.INTCON, "0"));
                nextSym();
            } else if (Character.isDigit(c)) {
                do {
                    sb.append(c);
                    nextSym();
                } while (Character.isDigit(c));
                tokens.add(new Token(Token.Type.INTCON, sb.toString()));
            }
            // STRCON
            else if (c == '"') {
                do {
                    sb.append(c);
                    nextSym();
                } while (c != '"' && notEnd());
                sb.append(c);
                tokens.add(new Token(Token.Type.STRCON, sb.toString()));
                nextSym();
            }
            // NEQ 和 NOT
            else if (c == '!') {
                nextSym();
                if (c == '=') {
                    tokens.add(new Token(Token.Type.NEQ, "!="));
                    nextSym();
                } else {
                    tokens.add(new Token(Token.Type.NOT, "!"));
                }
            }
            // AND 和 a类错误
            else if (c == '&') {
                nextSym();
                if (c == '&') {
                    tokens.add(new Token(Token.Type.AND, "&&"));
                    nextSym();
                } else {
                    errors.add(new Error(lineno));
                }
            }
            // OR 和 a类错误
            else if (c == '|') {
                nextSym();
                if (c == '|') {
                    tokens.add(new Token(Token.Type.OR, "||"));
                    nextSym();
                } else {
                    errors.add(new Error(lineno));
                }
            }
            // LEQ 和 LSS
            else if (c == '<') {
                nextSym();
                if (c == '=') {
                    tokens.add(new Token(Token.Type.LEQ, "<="));
                    nextSym();
                } else {
                    tokens.add(new Token(Token.Type.LSS, "<"));
                }
            }
            // GEQ 和 GRE
            else if (c == '>') {
                nextSym();
                if (c == '=') {
                    tokens.add(new Token(Token.Type.GEQ, ">="));
                    nextSym();
                } else {
                    tokens.add(new Token(Token.Type.GRE, ">"));
                }
            }
            // EQL 和 ASSIGN
            else if (c == '=') {
                nextSym();
                if (c == '=') {
                    tokens.add(new Token(Token.Type.EQL, "=="));
                    nextSym();
                } else {
                    tokens.add(new Token(Token.Type.ASSIGN, "="));
                }
            }
            // 注释 和 除法
            else if (c == '/') {
                nextSym();
                if (c == '/') {
                    do {
                        nextSym();
                    } while (c != '\n' && notEnd());
                    nextSym();
                } else if (c == '*') {
                    char lc;
                    nextSym();
                    do {
                        lc = c;
                        nextSym();
                    } while (!(lc == '*' && c == '/') && notEnd());
                    nextSym();
                } else {
                    tokens.add(new Token(Token.Type.DIV, "/"));
                }
            } else {
                sb.append(c);
                tokens.add(new Token(
                        switch (c) {
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
                        }, sb.toString())
                );
                nextSym();
            }
        }
    }

    private void nextSym() {
        pos++;
        c = input.charAt(pos);
        if (c == '\n') {
            lineno++;
        }
    }

    private boolean notEnd() {
        return c != '\0';
    }

    public void writeToFile() {
        boolean printError = !errors.isEmpty();
        String filename = printError ? "error.txt" : "lexer.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            if (printError) {
                for (Error error : errors) {
                    writer.write(error.getLineno() + " " + error.getType());
                    writer.newLine();
                }
            } else {
                for (Token token : tokens) {
                    writer.write(token.type() + " " + token.content());
                    writer.newLine(); // 换行
                }
            }
            System.out.println("已成功写入文件:" + filename);
        } catch (IOException e) {
            System.err.println("写入文件时出错: " + e.getMessage());
        }

    }
}
