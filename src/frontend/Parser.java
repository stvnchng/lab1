package frontend;

import java.util.Iterator;
import java.util.LinkedList;

import frontend.Token.POS;
import frontend.Token.Lexeme;

public class Parser {

    // IR for -r flag
    private final IR irList = new IR();
    // Ops for -x flag
    private final IR ops = new IR();
    private int errors = 0;
    private Iterator<Token> tokens;
    private Token current;

    public void parse(Scanner scanner) {
        tokens = scanner.tokensToIterator();
        while (tokens.hasNext()) {
            current = nextToken();
            while (current.getPOS().equals(POS.NEWLINE) || current.getPOS().equals(POS.SPACE)) current = nextToken();
            switch (current.getPOS()) {
                case MEMOP:
                    finishMEMOP();
                    break;
                case LOADI:
                    finishLOADI();
                    break;
                case ARITHOP:
                    finishARITHOP();
                    break;
                case OUTPUT:
                    finishOUTPUT();
                    break;
                case NOP:
                    finishNOP();
                    break;
                default:
                    if (current.getPOS().equals(POS.ENDFILE)) return;
            }
        }
    }

    private void finishMEMOP() {
        int opcode = current.getOpcode(current.getLexeme());
        current = nextToken();
        if (!current.getPOS().equals(POS.SPACE)) {
            parseError(current.lineNum, "Missing space after memop.");
        } else {
            current = nextToken();
            if (!current.getPOS().equals(POS.REG)) {
                parseError(current.lineNum, "Missing source register in memop.");
            } else {
                int sr1 = Integer.parseInt(current.getLexeme().substring(1));
                current = nextToken();
                while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                if (!current.getPOS().equals(POS.INTO)) {
                    parseError(current.lineNum, "Missing '=>' in memop.");
                } else {
                    current = nextToken();
                    while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                    if (!current.getPOS().equals(POS.REG)) {
                        parseError(current.lineNum, "Missing target register in load or store.");
                    } else {
                        int sr3 = Integer.parseInt(current.getLexeme().substring(1));
                        int[] op1 = new int[]{ sr1 }, op2 = new int[]{     }, op3 = new int[]{ sr3 };
                        IR.Node operation = new IR.Node(opcode, op1, op2, op3);
                        irList.add(operation);
                        int[] ops1 = new int[]{ sr1, -1, -1, Integer.MAX_VALUE }, ops2 = new int[]{     }, ops3 = new int[]{ sr3, -1, -1, Integer.MAX_VALUE };
                        operation = new IR.Node(opcode, ops1, ops2, ops3);
                        ops.add(operation);
                    }
                }
            }
        }
    }

    private void finishLOADI() {
        current = nextToken();
        if (!current.getPOS().equals(POS.SPACE)) {
            parseError(current.lineNum, "Missing space after loadI.");
        } else {
            current = nextToken();
            while (current.getPOS().equals(POS.SPACE)) current = nextToken();
            if (!current.getPOS().equals(POS.CONST)) {
                parseError(current.lineNum, "Missing constant in loadI.");
            } else {
                int sr1 = Integer.parseInt(current.getLexeme());
                current = nextToken();
                while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                if (!current.getPOS().equals(POS.INTO)) {
                    parseError(current.lineNum, "Missing '=>' in loadI.");
                } else {
                    current = nextToken();
                    while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                    if (!current.getPOS().equals(POS.REG)) {
                        parseError(current.lineNum, "Missing destination register in loadI.");
                    } else {
                        int sr3 = Integer.parseInt(current.getLexeme().substring(1));
                        int[] op1 = new int[]{ sr1 }, op2 = new int[]{     }, op3 = new int[]{ sr3 };
                        IR.Node operation = new IR.Node(Lexeme.loadI.getInt(), op1, op2, op3, true);
                        irList.add(operation);
                        int[] ops1 = new int[]{ sr1, -1, -1, Integer.MAX_VALUE }, ops2 = new int[]{     }, ops3 = new int[]{ sr3, -1, -1, Integer.MAX_VALUE };
                        operation = new IR.Node(Lexeme.loadI.getInt(), ops1, ops2, ops3, true);
                        ops.add(operation);
                    }
                }
            }
        }
    }

    private void finishARITHOP() {
        int opcode = current.getOpcode(current.getLexeme());
        current = nextToken();
        if (!current.getPOS().equals(POS.SPACE)) {
            parseError(current.lineNum, "Missing space after arithop.");
        } else {
            current = nextToken();
            if (!current.getPOS().equals(POS.REG)) {
                parseError(current.lineNum, "Missing destination register in " + current.getLexeme() + ".");
            } else {
                int sr1 = Integer.parseInt(current.getLexeme().substring(1));
                current = nextToken();
                while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                if (!current.getPOS().equals(POS.COMMA)) {
                    parseError(current.lineNum, "Missing a comma after the register in arithop.");
                } else {
                    current = nextToken();
                    while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                    if (!current.getPOS().equals(POS.REG)) {
                        parseError(current.lineNum, "Missing second source register in arithop.");
                    } else {
                        int sr2 = Integer.parseInt(current.getLexeme().substring(1));
                        current = nextToken();
                        while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                        if (!current.getPOS().equals(POS.INTO)) {
                            parseError(current.lineNum, "Missing => in arithop.");
                        } else {
                            current = nextToken();
                            while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                            if (!current.getPOS().equals(POS.REG)) {
                                parseError(current.lineNum, "Missing target register in arithop.");
                            } else {
                                int sr3 = Integer.parseInt(current.getLexeme().substring(1));
                                int[] op1 = new int[]{ sr1 }, op2 = new int[]{ sr2 }, op3 = new int[]{ sr3 };
                                IR.Node operation = new IR.Node(opcode, op1, op2, op3);
                                irList.add(operation);
                                int[] ops1 = new int[]{ sr1, -1, -1, Integer.MAX_VALUE }, ops2 = new int[]{ sr2, -1, -1, Integer.MAX_VALUE }, ops3 = new int[]{ sr3, -1, -1, Integer.MAX_VALUE };
                                operation = new IR.Node(opcode, ops1, ops2, ops3);
                                ops.add(operation);

                                // handle weird cases like extra registers
                                current = nextToken();
                                while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                                if (current.getPOS().equals(POS.REG)) {
                                    parseError(current.lineNum, " Extra token at end of line \"" + current.getLexeme() + "\" (REG)");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void finishOUTPUT() {
        current = nextToken();
        if (!current.getPOS().equals(POS.SPACE)) {
            parseError(current.lineNum, "Missing space after the output.");
        } else {
            current = nextToken();
            if (!current.getPOS().equals(POS.CONST)) {
                parseError(current.lineNum, "Missing constant in output.");
            } else {
                int sr1 = Integer.parseInt(current.getLexeme());
                IR.Node operation = new IR.Node(Lexeme.output.getInt(), new int[]{ sr1 });
                irList.add(operation);
                operation = new IR.Node(Lexeme.output.getInt(), new int[]{ sr1,  -1, -1, Integer.MAX_VALUE  });
                ops.add(operation);
            }
        }
    }

    private void finishNOP() {
        IR.Node operation = new IR.Node(Lexeme.nop.getInt());
        irList.add(operation);
    }

    private Token nextToken() {
        return tokens.next();
    }

    public LinkedList<IR.Node> getIR() {
        return this.ops.getOps();
    }

    /**
     * -r flag
     */
    public void printIR() {
        irList.printIR();
    }

    /**
     *  -p flag
     */
    public void printSummary() {
        if (errors > 0) System.err.println("Frontend found " + errors + " errors.");
        else System.out.println("Parse succeeded, finding " + irList.size() + " ILOC operations.");
    }

    public boolean errorsPresent() {
        return errors > 0;
    }

    public void parseError(int lineNum, String msg) {
        errors++;
        System.err.println(lineNum + ": " + msg);
    }
}
