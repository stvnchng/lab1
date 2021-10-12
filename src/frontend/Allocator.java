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

}