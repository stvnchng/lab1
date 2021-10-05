package frontend;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import frontend.Token.POS;

public class Scanner {

    private final BufferedReader br;

    private final ArrayList<Token> tokens = new ArrayList<>();
    private final ArrayList<Token> errors = new ArrayList<>();
    private char[] currWord;
    private int idx;

    public int lineNum = 0;

    public Scanner(BufferedReader br) {
        this.br = br;
    }

    public void scanInput() throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            lineNum++;
            nextToken(line);
            tokens.add(new Token(POS.NEWLINE, "\\n", lineNum));
        }
        Token eof = new Token(POS.ENDFILE, "", lineNum);
        tokens.add(eof);
    }

    /**
     * Scans and matches characters to TokenPairs to be analyzed by the parser.
     */
    public void nextToken(String line) {
        currWord = line.toCharArray();
        idx = -1;

        Token match;
        String lexeme;
        char c = nextChar();

        while (idx < currWord.length) {
            lexeme = "";

            // handle add
            if (c == 'a') {
                lexeme += c;
                if ((c = nextChar()) == 'd') {
                    lexeme += c;
                    if ((c = nextChar()) == 'd') {
                        lexeme += c;
                        match = new Token(POS.ARITHOP, lexeme, lineNum);
                        tokens.add(match);
                    } else {
                        lexeme += c;
                        createErrorToken(lexeme);
                        break;
                    }
                } else {
                    lexeme += c;
                    createErrorToken(lexeme);
                    break;
                }
            }
            // handle store and sub
            else if (c == 's') {
                lexeme += c;
                if ((c = nextChar()) == 'u') {
                    lexeme += c;
                    if ((c = nextChar()) == 'b') {
                        lexeme += c;
                        match = new Token(POS.ARITHOP, lexeme, lineNum);
                        tokens.add(match);
                    } else {
                        lexeme += c;
                        createErrorToken(lexeme);
                        break;
                    }
                } else if (c == 't') {
                    lexeme += c;
                    if ((c = nextChar()) == 'o') {
                        lexeme += c;
                        if ((c = nextChar()) == 'r') {
                            lexeme += c;
                            if ((c = nextChar()) == 'e') {
                                lexeme += c;
                                match = new Token(POS.MEMOP, lexeme, lineNum);
                                tokens.add(match);
                            } else {
                                lexeme += c;
                                createErrorToken(lexeme);
                                break;
                            }
                        } else {
                            lexeme += c;
                            createErrorToken(lexeme);
                            break;
                        }
                    } else {
                        lexeme += c;
                        createErrorToken(lexeme);
                        break;
                    }
                } else {
                    lexeme += c;
                    createErrorToken(lexeme);
                    break;
                }
            }
            // handle mult
            else if (c == 'm') {
                lexeme += c;
                if ((c = nextChar()) == 'u') {
                    lexeme += c;
                    if ((c = nextChar()) == 'l') {
                        lexeme += c;
                        if ((c = nextChar()) == 't') {
                            lexeme += c;
                            match = new Token(POS.ARITHOP, lexeme, lineNum);
                            tokens.add(match);
                        } else {
                            lexeme += c;
                            createErrorToken(lexeme);
                            break;
                        }
                    } else {
                        lexeme += c;
                        createErrorToken(lexeme);
                        break;
                    }
                } else {
                    lexeme += c;
                    createErrorToken(lexeme);
                    break;
                }
            }
            // handle load, loadI, lshift
            else if (c == 'l') {
                lexeme += c;
                if ((c = nextChar()) == 'o') {
                    lexeme += c;
                    if ((c = nextChar()) == 'a') {
                        lexeme += c;
                        if ((c = nextChar()) == 'd') {
                            lexeme += c;
                            if ((c = nextChar()) == 'I') {
                                lexeme += c;
                                match = new Token(POS.LOADI, lexeme, lineNum);
                                tokens.add(match);
                                c = nextChar();
                                continue;
                            }
                            match = new Token(POS.MEMOP, lexeme, lineNum);
                            tokens.add(match);
                            continue;
                        } else {
                            lexeme += c;
                            createErrorToken(lexeme);
                            break;
                        }
                    } else {
                        lexeme += c;
                        createErrorToken(lexeme);
                        break;
                    }
                }
                // handle lshift
                else if (c == 's') {
                    lexeme += c;
                    if ((c = nextChar()) == 'h') {
                        lexeme += c;
                        if ((c = nextChar()) == 'i') {
                            lexeme += c;
                            if ((c = nextChar()) == 'f') {
                                lexeme += c;
                                if ((c = nextChar()) == 't') {
                                    lexeme += c;
                                    match = new Token(POS.ARITHOP, lexeme, lineNum);
                                    tokens.add(match);
                                } else {
                                    lexeme += c;
                                    createErrorToken(lexeme);
                                    return;
                                }
                            } else {
                                lexeme += c;
                                createErrorToken(lexeme);
                                return;
                            }
                        } else {
                            lexeme += c;
                            createErrorToken(lexeme);
                            return;
                        }
                    } else {
                        lexeme += c;
                        createErrorToken(lexeme);
                        return;
                    }
                } else {
                    lexeme += c;
                    createErrorToken(lexeme);
                    return;
                }
            }
            // handle register and rshift
            else if (c == 'r') {
                lexeme += c;
                if (Character.isDigit(c = nextChar())) {
                    lexeme += c;
                    while (Character.isDigit(c = nextChar())) {
                        lexeme += c;
                    }
                    match = new Token(POS.REG, lexeme, lineNum);
                    tokens.add(match);
                    continue;
                } else if (c == 's') {
                    lexeme += c;
                    if ((c = nextChar()) == 'h') {
                        lexeme += c;
                        if ((c = nextChar()) == 'i') {
                            lexeme += c;
                            if ((c = nextChar()) == 'f') {
                                lexeme += c;
                                if ((c = nextChar()) == 't') {
                                    lexeme += c;
                                    match = new Token(POS.ARITHOP, lexeme, lineNum);
                                    tokens.add(match);
                                } else {
                                    lexeme += c;
                                    createErrorToken(lexeme);
                                    break;
                                }
                            } else {
                                lexeme += c;
                                createErrorToken(lexeme);
                                break;
                            }
                        } else {
                            lexeme += c;
                            createErrorToken(lexeme);
                            break;
                        }
                    } else {
                        lexeme += c;
                        createErrorToken(lexeme);
                        break;
                    }
                } else {
                    lexeme += c;
                    createErrorToken(lexeme);
                    break;
                }
            }
            // handle constant
            else if (Character.isDigit(c)) {
                lexeme += c;
                while (Character.isDigit(c = nextChar())) {
                    lexeme += c;
                }
                match = new Token(POS.CONST, lexeme, lineNum);
                tokens.add(match);
                continue;
            }
            // handle output
            else if (c == 'o') {
                lexeme += c;
                if ((c = nextChar()) == 'u') {
                    lexeme += c;
                    if ((c = nextChar()) == 't') {
                        lexeme += c;
                        if ((c = nextChar()) == 'p') {
                            lexeme += c;
                            if ((c = nextChar()) == 'u') {
                                lexeme += c;
                                if ((c = nextChar()) == 't') {
                                    lexeme += c;
                                    match = new Token(POS.OUTPUT, lexeme, lineNum);
                                    tokens.add(match);
                                } else {
                                    lexeme += c;
                                    createErrorToken(lexeme);
                                    break;
                                }
                            } else {
                                lexeme += c;
                                createErrorToken(lexeme);
                                break;
                            }
                        } else {
                            lexeme += c;
                            createErrorToken(lexeme);
                            break;
                        }
                    } else {
                        lexeme += c;
                        createErrorToken(lexeme);
                        break;
                    }
                } else {
                    lexeme += c;
                    createErrorToken(lexeme);
                    break;
                }
            }
            // handle nop
            else if (c == 'n') {
                lexeme += c;
                if ((c = nextChar()) == 'o') {
                    lexeme += c;
                    if ((c = nextChar()) == 'p') {
                        lexeme += c;
                        match = new Token(POS.NOP, lexeme, lineNum);
                        tokens.add(match);
                    } else {
                        lexeme += c;
                        createErrorToken(lexeme);
                        break;
                    }
                } else {
                    lexeme += c;
                    createErrorToken(lexeme);
                    break;
                }
            }
            // strip comments
            else if (c == '/') {
                lexeme += c;
                if ((c = nextChar()) != '/') {
                    lexeme += c;
                    createErrorToken(lexeme);
                }
                break;
            }
            // whitespace and tabs
            else if (c == ' ' || c == '\t') {
                lexeme += c;
                c = nextChar();
                while (c == ' ' || c == '\t') {
                    lexeme += c;
                    c = nextChar();
                }
                match = new Token(POS.SPACE, " ", lineNum);
                tokens.add(match);
                continue;
            }
            // comma
            else if (c == ',') {
                lexeme += c;
                match = new Token(POS.COMMA, lexeme, lineNum);
                tokens.add(match);
            }
            // arrow/into
            else if (c == '=') {
                lexeme += c;
                if ((c = nextChar()) == '>') {
                    lexeme += c;
                    match = new Token(POS.INTO, lexeme, lineNum);
                    tokens.add(match);
                } else {
                    lexeme += c;
                    createErrorToken(lexeme);
                    break;
                }
            } else {
                lexeme += c;
                createErrorToken(lexeme);
                return;
            }
            // increment to next char
            c = nextChar();
        }
    }

    public char nextChar() {
        return ++idx < currWord.length ? currWord[idx] : '!';
    }

    public Iterator<Token> tokensToIterator() {
        return tokens.iterator();
    }

    public void createErrorToken(String lexeme) {
        Token error = new Token(POS.ERROR, lexeme, lineNum);
        tokens.add(error);
        errors.add(error);
    }

    public boolean errorsPresent() {
        return errors.size() > 0;
    }

    /**
     * -s flag
     */
    public void printTokens() {
        tokens.forEach(tokenPair -> System.out.println(tokenPair.lineNum + ": " + tokenPair));
    }

    public void reportErrors() {
        System.out.println("Line\tMessage\n-----------------------------");
        errors.forEach(System.err::println);
    }

    public void renameRegisters() {
        
    }
}
