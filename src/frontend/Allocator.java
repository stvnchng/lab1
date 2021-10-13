package frontend;

import java.util.Arrays;
import java.util.LinkedList;

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

    public String[] renamedOps;

    public IR.Node[] renameRegisters(LinkedList<IR.Node> ops, boolean flagX) {
        renamedOps = new IR.Node[ops.size()];
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
            formatOperation(currOp, i);
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

    public void updateRenamedOps(IR.Node op, int i) {
        String reg1 = "", reg2 = "", reg3 = "";
        if (op.op1.length > 0 && op.op1[VR] != -1) reg1 = "  \t\tr" + op.op1[VR];
        else if (op.op1[SR] != -1) reg1 = "  \t\t" + op.op1[SR];
        if (op.op2.length > 0 && op.op2[VR] != -1) reg2 = ", \tr" + op.op2[VR];
        if (op.op3.length > 0 && op.op3[VR] != -1) reg3 = "\t=>\t r" + op.op3[VR];
        String renamedOp = Token.Lexeme.valueOf(op.opcode) + reg1 + reg2 + reg3;
        renamedOps[i]  = renamedOp;
    }

    // Stuff for Code Check 2 - allocator algo
    int[] VRToPR;
    int[] PRToVR;
    int[] PRNU;

    int maxVR;
    int maxPR;

    IR.Node[] allocatedOps;

    // allocate without spilling - when MAXLIVE >= maxPR
    public void allocate(IR.Node[] ops, int k) {
        for (IR.Node op : ops) {
            System.out.println(op);
            if (op.getMaxRegister(VR) > maxVR) maxVR = op.getMaxRegister(VR);
        }
        maxVR++;
        VRToPR = new int[maxVR];
        PRToVR = new int[k];
        Arrays.fill(VRToPR, -1);
        System.out.println(Arrays.toString(VRToPR));
        for (int i = 0; i < ops.length; i++) {
            IR.Node currOp = ops[i];
            // LOAD, op1 and op3 need a PR, ARITHOP op2 also needs a PR
            if (currOp.opcode == 0 || (currOp.opcode >= 3 && currOp.opcode <= 7)) {

            }
            // STORE
            else if (currOp.opcode == 1) {

            }
            // LOADI, op3 needs a PR
            else if (currOp.opcode == 2) {
                VRToPR[currOp.op3[VR]] = getAPR();
            }
        }
    }

    public int getAPR() {
        return 0;
    }

    public void freePR() {

    }
}