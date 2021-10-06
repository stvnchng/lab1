package frontend;

import java.util.Arrays;
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
                        parseError(current.lineNum, "Missing destination register in memop.");
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
                                int[] ops1 = new int[]{ sr1, -1, -1, Integer.MAX_VALUE }, ops2 = new int[]{ sr2, -1, -1, Integer.MAX_VALUE }, ops3 = new int[]{ sr3, -1, -1, Integer.MAX_VALUE };
                                operation = new IR.Node(opcode, ops1, ops2, ops3);
                                ops.add(operation);
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

    // Variables associated with Lab 2 Renaming Algorithm
    int[] SRToVR;
    int[] LU;

    int maxSR;
    int MAXLIVE = 0;
    int VRName = 0;

    static final int SR = 0;
    static final int VR = 1;
    static final int NU = 3;

    public String[] renamedOps;

    public void renameRegisters(LinkedList<IR.Node> ops) {
        for (IR.Node op : ops) {
            if (op.getMaxSR() > maxSR) maxSR = op.getMaxSR();
        }
        maxSR++;
        SRToVR = new int[maxSR];
        LU = new int[maxSR];
        renamedOps = new String[ops.size()];
        // initialize
        for (int idx = 0; idx < maxSR; idx++) {
            SRToVR[idx] = -1;
            LU[idx] = Integer.MAX_VALUE;
        }
        for (int i = ops.size() - 1; i > -1; i--) {
            IR.Node currOp = ops.get(i);
            // LOAD, kill op3 then update op1, if ARITHOP also update op2
            if (currOp.opcode == 0 || (currOp.opcode >= 3 && currOp.opcode <= 7)) {
                updateTable(currOp.op3, i);
                SRToVR[currOp.op3[SR]] = -1;
                LU[currOp.op3[SR]] = Integer.MAX_VALUE;
                updateTable(currOp.op1, i);
                // ARITHOP, update op2
                if (currOp.opcode >= 3) {
                    updateTable(currOp.op2, i);
                }
                updateMAXLIVE();
            }
            // STORE, exception - don't need to kill op3, just update op1 and op2
            else if (currOp.opcode == 1) {
                updateTable(currOp.op1, i);
                updateTable(currOp.op3, i);
                updateMAXLIVE();
            }
            // loadI, constant load into sr so just kill op3
            else if (currOp.opcode == 2) {
                updateTable(currOp.op3, i);
                SRToVR[currOp.op3[SR]] = -1;
                LU[currOp.op3[SR]] = Integer.MAX_VALUE;
                updateMAXLIVE();
            }
//            System.out.println(i + ": " + Arrays.toString(SRToVR));
//            System.out.println(i + ": " + Arrays.toString(LU));
//            System.out.println("VRName: " + VRName);
            updateRenamedOps(currOp, i);
        }
        Arrays.stream(renamedOps).forEach(System.out::println);
//        System.out.println("MAXLIVE: " + MAXLIVE);
    }

    public void updateTable(int[] operand, int instructionNum) {
        if (operand.length > 0) {
            if (SRToVR[operand[SR]] == -1) {
                SRToVR[operand[SR]] = VRName++;
            }
            operand[VR] = SRToVR[operand[SR]];
            operand[NU] = SRToVR[operand[SR]];
            LU[operand[SR]] = instructionNum;
        } else System.out.println("Empty operand!");
    }

    public void updateMAXLIVE() {
        long SRToVRCount = Arrays.stream(SRToVR).filter(reg -> reg != -1).count();
        if (SRToVRCount > MAXLIVE) MAXLIVE = (int) SRToVRCount;
    }

    public void updateRenamedOps(IR.Node op, int i) {
        String reg1 = "", reg2 = "", reg3 = "";
        if (op.op1.length > 0 && op.op1[VR] != -1) reg1 = "  \t\tr" + op.op1[VR];
        else if (op.op1[SR] != -1) reg1 = "  \t\t" + op.op1[SR];
        if (op.op2.length > 0 && op.op2[VR] != -1) reg2 = ", \tr" + op.op2[VR];
        if (op.op3.length > 0 && op.op3[VR] != -1) reg3 = "\t=>\t r" + op.op3[VR];
        String renamedOp = Token.Lexeme.valueOf(op.opcode) + reg1 + reg2 + reg3;
        renamedOps[i]  = renamedOp;
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
