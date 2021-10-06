package frontend;

import java.util.Iterator;
import java.util.LinkedList;

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
    int[] SRToVR;
    int[] LU;

    int maxSR;
    int MAXLIVE = 0;
    int VRName = 0;

    public LinkedList<IR.Node> getIR() {
        return this.irList.getOps();
    }

    public void renameRegisters(LinkedList<IR.Node> ops) {
        // First: Find distinct live ranges in code
        // value is "live" from definition up to last use
        for (IR.Node op : ops) {
            if (op.getMaxSR() > maxSR) maxSR = op.getMaxSR();
        }
        SRToVR = new int[maxSR];
        LU = new int[maxSR];
        for (int idx = 0; idx < maxSR; idx++) {
            SRToVR[idx] = -1;
            LU[idx] = Integer.MAX_VALUE;
        }
        for (int i = ops.size() - 1; i > -1; i--) {
            // load
            if (ops.get(i).opcode == 0) {
                // update and kill
                if (SRToVR[ops.get(i).op3[0]] == -1) {
                    SRToVR[ops.get(i).op3[0]] = VRName;
                    VRName++;
                }
                ops.get(i).op3[1] = SRToVR[ops.get(i).op3[0]];
                ops.get(i).op3[3] = SRToVR[ops.get(i).op3[0]];

            }
            // store
            else if (ops.get(i).opcode == 1) {
                // don't need to initialize for store

            }
            // loadI
            else if (ops.get(i).opcode == 2) {

            }
            // arithop
            else if (ops.get(i).op1.length > 0 && ops.get(i).op2.length > 0 && ops.get(i).op3.length > 0) {

            }
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
