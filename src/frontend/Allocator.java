package frontend;

import java.util.*;

public class Allocator {

    int[] VRToPR;
    int[] PRToVR;
    int[] PRNU;

    public void allocate() {

    }


    // Stuff for Code Check 1 (Renaming algo)
    public int MAXLIVE = 0;

    int[] SRToVR;
    int[] LU;

    int maxSR;
    int VRName = 0;

    static final int SR = 0;
    static final int VR = 1;
    static final int NU = 3;

    // pretty-printed ILOC code for flag output
    private String[] formattedOps;

    public IR.Node[] renameRegisters(LinkedList<IR.Node> ops, boolean flagX) {
        // list of ops after renaming in IR.Node form
        IR.Node[] renamedOps = new IR.Node[ops.size()];
        formattedOps = new String[ops.size()];

        for (IR.Node op : ops) {
            if (op.getMaxSR() > maxSR) maxSR = op.getMaxSR();
        }
        maxSR++;

        // initialize
        SRToVR = new int[maxSR];
        LU = new int[maxSR];
        Arrays.fill(SRToVR, -1);
        Arrays.fill(LU, Integer.MAX_VALUE);
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
            updateMAXLIVE();
            renamedOps[i] = currOp;
            formatOperation(currOp, i, VR);
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

    private void updateMAXLIVE() {
//        System.out.println(i + ": " + Arrays.toString(SRToVR));
//        System.out.println(i + ": " + Arrays.toString(LU));
//        System.out.println("VRName: " + VRName);
        long SRToVRCount = Arrays.stream(SRToVR).filter(reg -> reg != -1).count();
        if (SRToVRCount > MAXLIVE) MAXLIVE = (int) SRToVRCount;
    }

    private void formatOperation(IR.Node op, int i, int regType) {
        String reg1 = "", reg2 = "", reg3 = "";
        if (op.op1.length > 0 && op.op1[regType] != -1) reg1 = "  \t\tr" + op.op1[regType];
        else if (op.op1[SR] != -1) reg1 = "  \t\t" + op.op1[SR];
        if (op.op2.length > 0 && op.op2[regType] != -1) reg2 = ", \tr" + op.op2[regType];
        if (op.op3.length > 0 && op.op3[regType] != -1) reg3 = "\t=>\t r" + op.op3[regType];
        formattedOps[i] = Token.Lexeme.valueOf(op.opcode) + reg1 + reg2 + reg3;
    }

    // Stuff for Code Check 2 - allocator algo
    int[] VRToPR;
    int[] PRToVR;
    int[] PRNU;

    int maxVR;
    // trying out some sort of stack to keep track of available registers
    Deque<Integer> PRStack = new ArrayDeque<>();
    IR.Node[] allocatedOps;

    public void allocate(IR.Node[] ops, int k) {
//        System.out.println("Invoking allocator with k = " + k + " and MAXLIVE = " + MAXLIVE);
        allocatedOps = new IR.Node[ops.length];
        for (IR.Node op : ops) {
            if (op.getMaxRegister(VR) > maxVR) maxVR = op.getMaxRegister(VR);
        }

        for (int idx = k - 1; idx > -1; idx--) {
            PRStack.push(idx);
        }

        VRToPR = new int[maxVR + 1];
        PRToVR = new int[k];
        PRNU = new int[k];
        Arrays.fill(VRToPR, -1);
        Arrays.fill(PRToVR, -1);
        Arrays.fill(PRNU, Integer.MAX_VALUE);

        // traverse block linearly
        for (int i = 0; i < ops.length; i++) {
            IR.Node currOp = ops[i];
            // allocate uses, first for op1 then op2
            if (currOp.usesOP1()) {
                if (VRToPR[currOp.op1[VR]] == -1) {
                    System.out.println("op1 gets a pr with inputs - vr: " + currOp.op1[VR] + " nu: " + currOp.op1[NU]);
                    currOp.op1[PR] = getAPR(currOp.op1[VR],  currOp.op1[NU], false);
                    restore(currOp.op1[VR], currOp.op1[PR]);
                } else {
                    currOp.op1[PR] = VRToPR[currOp.op1[VR]];
                }
            }
            if (currOp.usesOP2()) {
                if (VRToPR[currOp.op2[VR]] == -1) {
                    System.out.println("op2 gets a pr with inputs - vr: " + currOp.op2[VR] + " nu: " + currOp.op2[NU]);
                    currOp.op2[PR] = getAPR(currOp.op2[VR],  currOp.op2[NU], true);
                    restore(currOp.op2[VR], currOp.op2[PR]);
                } else {
                    currOp.op2[PR] = VRToPR[currOp.op2[VR]];
                }
            }
            // set the mark in ops
            // last use?
            boolean op1Freed = false;
            boolean op2Freed = false;
            if (currOp.usesOP1()) {
                if (currOp.op1[NU] == Integer.MAX_VALUE && currOp.op1[PR] != -1) {
                    System.out.println("op1 freed pr" + currOp.op1[PR]);
                    freeAPR(currOp.op1[PR]);
                    op1Freed = true;
                }
            }
            if (currOp.usesOP2()) {
                if (currOp.op2[NU] == Integer.MAX_VALUE && currOp.op2[PR] != -1) {
                    System.out.println("op2 freed pr" + currOp.op2[PR]);
                    freeAPR(currOp.op2[PR]);
                    op2Freed = true;
                }
            }
            if (op1Freed && op2Freed) {
                PRStack.push(currOp.op2[PR]);
                PRStack.push(currOp.op1[PR]);
            } else if (op1Freed) {
                PRStack.push(currOp.op1[PR]);
            } else if (op2Freed) {
                PRStack.push(currOp.op2[PR]);
            }
            System.out.println(PRStack);

            // allocate defs - op3
            if (currOp.usesOP3()) {
                if (VRToPR[currOp.op3[VR]] == -1) {
                    System.out.println("op3 gets a pr with inputs - vr: " + currOp.op3[VR] + " nu: " + currOp.op3[NU]);
                    currOp.op3[PR] = getAPR(currOp.op3[VR], currOp.op3[NU], false);
                    if (currOp.op3[NU] == Integer.MAX_VALUE) {
                        freeAPR(currOp.op3[PR]);
                        PRStack.push(currOp.op3[PR]);
                    }
                } else {
                    currOp.op3[PR] = VRToPR[currOp.op3[VR]];
                }
            }
            // clear the mark in each PR
            System.out.println("currOp: " + currOp + "\nVRToPR: " + Arrays.toString(VRToPR) +  "\t\tPRToVR: " + Arrays.toString(PRToVR) + "\t\tPRNU: " + Arrays.toString(PRNU) + "\nallocating next op...");
            allocatedOps[i] = currOp;
            formatOperation(currOp, i, PR);
        }
        Arrays.stream(formattedOps).forEach(System.out::println);
    }

    public int getAPR(int vr, int nu, boolean OP2) {
        int x;
        if (!PRStack.isEmpty()) {
            x = PRStack.pop();
        } else {
            if (OP2) {
                int[] PRNUModified = PRNU;
//                PRNUModified[]
                x = getIdxMaxVal(PRNUModified);
            } else {
                x = getIdxMaxVal(PRNU);
            }
           // pick unmarked x and spill it
            spill(x);
            VRToPR[PRToVR[x]] = -1;
        }
        System.out.println("getAPR picked pr" + x);
        VRToPR[vr] = x;
        PRToVR[x] = vr;
        PRNU[x] = nu;

        return x;
    }

    // TODO: this shit
    public void restore(int vr, int pr) {

    }

    public void freeAPR(int pr) {
        VRToPR[PRToVR[pr]] = -1;
        PRToVR[pr] = -1;
        PRNU[pr] = Integer.MAX_VALUE;
    }

    int spillAddr = 32768;
    // TODO: properly spill
    public void spill(int pr) {

    }

    public int getIdxMaxVal(int[] arr) {
        int maxVal = -Integer.MAX_VALUE;
        int maxIdx = -1;
        for (int i = 0; i < arr.length; i++) {
            int num = arr[i];
            if (num > maxVal) {
                maxVal = num;
                maxIdx = i;
            }
        }
        return maxIdx;
    }
}