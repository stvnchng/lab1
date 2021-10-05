package frontend;

import java.util.ArrayList;
import java.util.Iterator;

import frontend.Token.POS;
import frontend.Token.Lexeme;

public class Parser {

    private final IR irList = new IR();
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
                        parseError(current.lineNum, "Missing destination register in memop.");
                    } else {
                        int sr3 = Integer.parseInt(current.getLexeme().substring(1));
                        int[] op1 = new int[]{ sr1 }, op2 = new int[]{     }, op3 = new int[]{ sr3 };
                        IR.Node operation = new IR.Node(opcode, op1, op2, op3);
                        irList.add(operation);
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
                    parseError(current.lineNum, "Missing a comma after the register in " + current.getLexeme() + ".");
                } else {
                    current = nextToken();
                    while (current.getPOS().equals(POS.SPACE)) current = nextToken();
                    if (!current.getPOS().equals(POS.REG)) {
                        parseError(current.lineNum, "Missing second register in " + current.getLexeme() + ".");
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
                                parseError(current.lineNum, "Missing destination register in arithop.");
                            } else {
                                int sr3 = Integer.parseInt(current.getLexeme().substring(1));
                                int[] op1 = new int[]{ sr1 }, op2 = new int[]{ sr2 }, op3 = new int[]{ sr3 };
                                IR.Node operation = new IR.Node(opcode, op1, op2, op3);
                                irList.add(operation);
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

    // Variables associated with Lab 2 Renaming Algorithm
    ArrayList<Integer> LU = new ArrayList<>();
    ArrayList<Integer> SRToVR = new ArrayList<>();
    int maxSR = 0;
    int MAXLIVE = 0;
    int VRName = 0;

    public void renameRegisters(ArrayList<IR.Node> ops) {
        // First: Find distinct live ranges in code
        // value is "live" from definition up to last use

//        for (int i = 0; i < ops.size(); i++) {
//
//        }
        for (int i = ops.size() - 1; i > MAXLIVE; i--) {
//            if (ops.get(i).)
        }
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
