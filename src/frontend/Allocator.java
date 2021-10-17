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
    int maxVR;
    int VRName = 0;

    static final int SR = 0;
    static final int VR = 1;
    static final int NU = 3;

    public IR.Node[] renameRegisters(LinkedList<IR.Node> ops, boolean flagX) {
        // list of ops after renaming in IR.Node form
        IR.Node[] renamedOps = new IR.Node[ops.size()];
        // pretty-printed ILOC code for -x
        String[] formattedOps = new String[ops.size()];

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
            formattedOps[i]  = formatOperation(currOp, VR);

            if (currOp.getMaxRegister(VR) > maxVR) maxVR = currOp.getMaxRegister(VR);
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
        long SRToVRCount = Arrays.stream(SRToVR).filter(reg -> reg != -1).count();
        if (SRToVRCount > MAXLIVE) MAXLIVE = (int) SRToVRCount;
    }

    private String formatOperation(IR.Node op, int regType) {
        String reg1 = "", reg2 = "", reg3 = "";
        if (op.op1.length > 0 && op.op1[regType] != -1) reg1 = "  \t\tr" + op.op1[regType];
        else if (op.op1[SR] != -1) reg1 = "  \t\t" + op.op1[SR];
        if (op.op2.length > 0 && op.op2[regType] != -1) reg2 = ", \tr" + op.op2[regType];
        if (op.op3.length > 0 && op.op3[regType] != -1) reg3 = "\t=>\t r" + op.op3[regType];
        return Token.Lexeme.valueOf(op.opcode) + reg1 + reg2 + reg3;
    }

    // Stuff for Code Check 2 - allocator algo
    int[] VRToPR;
    int[] PRToVR;
    int[] PRNU;
    int[] VRToSpillLoc;

    // array to keep track of whether currOp is rematerializable
    int[] loadIToVRSpill;
    // array to log all VRs of loadI ops for access by rematerializable ops
    int[] loadIVRMap;

    Deque<Integer> PRStack = new ArrayDeque<>();
    ArrayList<IR.Node> allocatedOps = new ArrayList<>();

    public void allocate(IR.Node[] ops, int k) {
//        System.out.println("Invoking allocator with k = " + k + " and MAXLIVE = " + MAXLIVE);
        for (int idx = k - 1; idx > -1; idx--) {
            PRStack.push(idx);
        }
        if (MAXLIVE > k) {
            spillPR = k - 1;
            PRStack.removeLast();
            PRToVR = new int[spillPR];
            PRNU = new int[spillPR];
        } else {
            PRToVR = new int[k];
            PRNU = new int[k];
        }
        VRToPR = new int[maxVR + 1];

        Arrays.fill(VRToPR, -1);
        Arrays.fill(PRToVR, -1);
        Arrays.fill(PRNU, Integer.MAX_VALUE);

        VRToSpillLoc = new int[VRName];
        loadIToVRSpill = new int[VRName];
        loadIVRMap = new int[maxVR + 1];
        Arrays.fill(VRToSpillLoc, -1);
        Arrays.fill(loadIToVRSpill, -1);
        Arrays.fill(loadIVRMap, -1);

        // traverse block linearly
        for (int i = 0; i < ops.length; i++) {
            IR.Node currOp = ops[i];
//            System.out.println("PRStack: " + PRStack + ", reserved register: " + spillPR + "\nCurrent operation is a(n) " +  currOp);
            // load - need to handle op1 def, op1 use, get pr for op3
            if (currOp.opcode == 0) {
                if (VRToPR[currOp.op1[VR]] == -1) {
//                    System.out.println("op1 got a pr with inputs - vr: " + currOp.op1[VR] + ", nu: " + currOp.op1[NU] + ", op1pr: " + currOp.op1[PR]);
                    currOp.op1[PR] = getAPR(currOp.op1[VR],  currOp.op1[NU], currOp.op1[PR], -1);
                    restore(currOp.op1[VR], currOp.op1[PR]);
                } else {
                    currOp.op1[PR] = VRToPR[currOp.op1[VR]];
                    PRNU[currOp.op1[PR]] = currOp.op1[NU];
                }
                // op1 last use
                if (currOp.op1[NU] == Integer.MAX_VALUE && currOp.op1[PR] != -1) {
                    freeAPR(currOp.op1[PR]);
                    PRStack.push(currOp.op1[PR]);
                }
                // op3 def
                if (VRToPR[currOp.op3[VR]] == -1) {
//                    System.out.println("Handling op3 def, VRToPR: " + Arrays.toString(VRToPR) + "\tPRToVR: " +  Arrays.toString(PRToVR) + "\tPRNU: " + Arrays.toString(PRNU)  + "\nop3 got a pr with inputs vr: " + currOp.op3[VR] + ", nu: " + currOp.op3[NU] + ", op1pr: " + currOp.op1[PR]);
                    currOp.op3[PR] = getAPR(currOp.op3[VR], currOp.op3[NU], currOp.op1[PR], -1);
//                    restore(currOp.op3[VR], currOp.op3[PR]);
//                    if (currOp.op3[NU] == Integer.MAX_VALUE) {
//                        freeAPR(currOp.op3[PR]);
//                        PRStack.push(currOp.op3[PR]);
//                    }
                } else {
//                    currOp.op3[PR] = VRToPR[currOp.op3[VR]];
//                    PRNU[currOp.op3[PR]] = currOp.op3[NU];
                }
//                loadIVRMap[currOp.op3[VR]] = -1;
            }
            // store - need to handle op1 and op3 uses first
            else if (currOp.opcode == 1) {
                if (VRToPR[currOp.op1[VR]] == -1) {
//                    System.out.println("op1 gets a pr with inputs - vr: " + currOp.op1[VR] + " nu: " + currOp.op1[NU]);
                    currOp.op1[PR] = getAPR(currOp.op1[VR],  currOp.op1[NU], currOp.op1[PR], -1);
                    restore(currOp.op1[VR], currOp.op1[PR]);
                } else {
                    currOp.op1[PR] = VRToPR[currOp.op1[VR]];
                    PRNU[currOp.op1[PR]] = currOp.op1[NU];
                }

                if (VRToPR[currOp.op3[VR]] == -1) {
//                    System.out.println("op3 gets a pr with inputs - vr: " + currOp.op3[VR] + " nu: " + currOp.op3[NU]);
                    currOp.op3[PR] = getAPR(currOp.op3[VR], currOp.op3[NU], currOp.op1[PR], -1);
                    restore(currOp.op3[VR], currOp.op3[PR]);
//                    if (currOp.op3[NU] == Integer.MAX_VALUE) {
//                        freeAPR(currOp.op3[PR]);
//                        PRStack.push(currOp.op3[PR]);
//                    }
                } else {
                    currOp.op3[PR] = VRToPR[currOp.op3[VR]];
                    PRNU[currOp.op3[PR]] = currOp.op3[NU];
                }

                if (currOp.op1[NU] == Integer.MAX_VALUE && currOp.op1[PR] != -1) {
//                    System.out.println("op1 freed pr" + currOp.op1[PR]);
                    freeAPR(currOp.op1[PR]);
                    PRStack.push(currOp.op1[PR]);
                }

                if (currOp.op3[NU] == Integer.MAX_VALUE) {
                    freeAPR(currOp.op3[PR]);
                    PRStack.push(currOp.op3[PR]);
                }
            }
            // loadI - need to handle the spill a bit differently
            else if (currOp.opcode == 2){
                if (VRToPR[currOp.op3[VR]] == -1) {
//                    System.out.println("Handling op3 def for loadI, VRToPR: " + Arrays.toString(VRToPR) + "\tPRToVR: " +  Arrays.toString(PRToVR) + "\tPRNU: " + Arrays.toString(PRNU) + "\nop3 got a pr with inputs vr: " + currOp.op3[VR] + ", nu: " + currOp.op3[NU] + ", op1sr: " + currOp.op1[SR]);
                    currOp.op3[PR] = getAPR(currOp.op3[VR], currOp.op3[NU], currOp.op1[PR], currOp.op1[SR]);
                    if (currOp.op3[NU] == Integer.MAX_VALUE) {
                        freeAPR(currOp.op3[PR]);
                        PRStack.push(currOp.op3[PR]);
                    }
                } else {
                    currOp.op3[PR] = VRToPR[currOp.op3[VR]];
                    PRNU[currOp.op3[PR]] = currOp.op3[NU];
                }
                loadIVRMap[currOp.op3[VR]] = currOp.op1[SR];
            }
            else {
                // allocate uses, first for op1 then op2
                if (currOp.usesOP1()) {
                    if (VRToPR[currOp.op1[VR]] == -1) {
//                        System.out.println("arithop, op1 got a pr with inputs - vr: " + currOp.op1[VR] + ", nu: " + currOp.op1[NU] + ", op1pr: " + currOp.op1[PR]);
                        currOp.op1[PR] = getAPR(currOp.op1[VR],  currOp.op1[NU], currOp.op1[PR], -1);
                        restore(currOp.op1[VR], currOp.op1[PR]);
                    } else {
                        currOp.op1[PR] = VRToPR[currOp.op1[VR]];
                        PRNU[currOp.op1[PR]] = currOp.op1[NU];
                    }
                }
                if (currOp.usesOP2()) {
                    if (VRToPR[currOp.op2[VR]] == -1) {
//                        System.out.println("arithop, op2 gets a pr with inputs - vr: " + currOp.op2[VR] + " nu: " + currOp.op2[NU]);
                        currOp.op2[PR] = getAPR(currOp.op2[VR],  currOp.op2[NU], currOp.op1[PR], -1);
                        restore(currOp.op2[VR], currOp.op2[PR]);
                    } else {
                        currOp.op2[PR] = VRToPR[currOp.op2[VR]];
                        PRNU[currOp.op2[PR]] = currOp.op2[NU];
                    }
                }
                // last use?
                boolean op1Freed = false;
                boolean op2Freed = false;
                if (currOp.usesOP1()) {
                    if (currOp.op1[NU] == Integer.MAX_VALUE && currOp.op1[PR] != -1) {
//                        System.out.println("op1 freed pr" + currOp.op1[PR]);
                        freeAPR(currOp.op1[PR]);
                        op1Freed = true;
                    }
                }
                if (currOp.usesOP2()) {
                    if (currOp.op2[NU] == Integer.MAX_VALUE && currOp.op2[PR] != -1) {
//                        System.out.println("op2 freed pr" + currOp.op2[PR]);
                        freeAPR(currOp.op2[PR]);
                        op2Freed = true;
                    }
                }
                if (op1Freed && op2Freed) {
                    if (k < MAXLIVE) {
                        PRStack.push(currOp.op1[PR]);
                        PRStack.push(currOp.op2[PR]);
                    } else {
                        PRStack.push(currOp.op2[PR]);
                        PRStack.push(currOp.op1[PR]);
                    }
                } else if (op1Freed) {
                    PRStack.push(currOp.op1[PR]);
                } else if (op2Freed) {
                    PRStack.push(currOp.op2[PR]);
                }

                // allocate defs - op3
                if (currOp.usesOP3()) {
                    if (VRToPR[currOp.op3[VR]] == -1) {
//                        System.out.println("arithop, op3 got a pr - vr: " + currOp.op3[VR] + ", nu: " + currOp.op3[NU] + ", op1pr: " + currOp.op1[PR]);
                        currOp.op3[PR] = getAPR(currOp.op3[VR], currOp.op3[NU], currOp.op1[PR], -1);
//                        if (currOp.op3[NU] == Integer.MAX_VALUE) {
//                            freeAPR(currOp.op3[PR]);
//                            PRStack.push(currOp.op3[PR]);
//                        }
//                    } else {
//                        currOp.op3[PR] = VRToPR[currOp.op3[VR]];
//                        PRNU[currOp.op3[PR]] = currOp.op3[NU];
                    }
//                    loadIVRMap[currOp.op3[VR]] = -1;
                }
            }
            allocatedOps.add(currOp);
//            System.out.println(allocatedOps.size() + ": " + currOp + "\nVRToPR: " + Arrays.toString(VRToPR) +  "\t\tPRToVR: " + Arrays.toString(PRToVR) +  "\t\tPRNU: " + Arrays.toString(PRNU) + "\nallocating next op...");
//            System.out.println(formatOperation(currOp, PR) + "\n");
        }
        allocatedOps.forEach(op->{
            String opString = formatOperation(op, PR);
            System.out.println(opString);
        });
    }

    public int getAPR(int vr, int nu, int op1pr, int loadI) {
        int x;
        if (!PRStack.isEmpty()) {
            x = PRStack.pop();
        } else {
            if (loadI != -1) {
                loadIToVRSpill[vr] = loadI;
                x = getIdxMaxVal(PRNU);
//                return x;
            } else {
                int[] tempPRNU = PRNU;
//                System.out.println(Arrays.toString(PRNU));
//                if (op1pr != -1) tempPRNU[op1pr] = -1;
                x = getIdxMaxVal(tempPRNU);
            }
            spill(x);
            VRToPR[PRToVR[x]] = -1;
        }
//        System.out.println("getAPR picked pr" + x);
        VRToPR[vr] = x;
        PRToVR[x] = vr;
        PRNU[x] = nu;

        return x;
    }

    public void restore(int vr, int pr) {
//        System.out.println("Restore called with vr = " + vr + ", pr = " + pr + ", loadIToVRSpill[vr]: " + loadIToVRSpill[vr]);
        // handle rematerializable ops
//        if (loadIToVRSpill[vr] != -1) {
//            // handle loadI spillAddr => spillPR
//            int[] loadIOP1 = new int[]{ loadIVRMap[vr], -1, -1, Integer.MAX_VALUE };
//            int[] loadIOP3 = new int[]{ -1, vr, pr, PRNU[pr] };
//            IR.Node loadI = new IR.Node(2, loadIOP1, new int[]{}, loadIOP3, true);
//            allocatedOps.add(loadI);
//            System.out.println(formatOperation(loadI, PR));
//        } else {
            // handle loadI spillAddr => spillPR
            int[] loadIOP1 = new int[]{ VRToSpillLoc[vr], -1, -1, Integer.MAX_VALUE };
            int[] loadIOP3 = new int[]{ -1, -1, spillPR, PRNU[pr] };
            IR.Node loadI = new IR.Node(2, loadIOP1, new int[]{}, loadIOP3, true);
            // handle load pr => spillPR
            int[] loadOP1 = new int[]{ -1, spillPR, spillPR, Integer.MAX_VALUE };
            int[] loadOP3 = new int[]{ -1, vr, pr, PRNU[pr] };
            IR.Node load = new IR.Node(0, loadOP1, new int[]{}, loadOP3);

            allocatedOps.add(loadI);
            allocatedOps.add(load);
//            System.out.println(formatOperation(loadI, PR));
//            System.out.println(formatOperation(load, PR));
//            System.out.println();
//        }
//        System.out.println(allocatedOps.size());
//        System.out.println("VRToSpillLoc: " + Arrays.toString(VRToSpillLoc) + "\t\tloadIToVRSpill: " + Arrays.toString(loadIToVRSpill));
    }

    public void freeAPR(int pr) {
        VRToPR[PRToVR[pr]] = -1;
        PRToVR[pr] = -1;
        PRNU[pr] = Integer.MAX_VALUE;
    }

    int spillAddr = 32768;
    int spillPR = -1;
    public void spill(int pr) {
        int spillVR = PRToVR[pr];
//        System.out.println("PRNU: " + Arrays.toString(PRNU) + "\nSpill called on pr" + pr + " with spillVR = " + spillVR);
//        if (loadIToVRSpill[spillVR] == -1) {
            // handle loadI spillAddr => spillPR
            int[] loadIOP1 = new int[]{ spillAddr, -1, -1 };
            int[] loadIOP3 = new int[]{ -1, spillVR, spillPR };
            IR.Node loadI = new IR.Node(2, loadIOP1, new int[]{}, loadIOP3, true);

            // handle store pr => spillPR
            int[] storeOP1 = new int[]{ -1, spillVR, pr };
            int[] storeOP3 = new int[]{ -1, -1, spillPR };
            IR.Node store = new IR.Node(1, storeOP1, new int[]{}, storeOP3, true);

            allocatedOps.add(loadI);
            allocatedOps.add(store);
//        System.out.println(formatOperation(loadI, PR));
//        System.out.println(formatOperation(store, PR));
//        System.out.println();
        VRToSpillLoc[spillVR] = spillAddr;
        spillAddr += 4;

//        }
//        System.out.println("VRToSpillLoc: " + Arrays.toString(VRToSpillLoc) + "\t\tloadIToVRSpill: " + Arrays.toString(loadIToVRSpill));
    }

    public int getIdxMaxVal(int[] arr) {
        int maxVal = -Integer.MAX_VALUE;
        int maxIdx = -1;
        for (int i = 0; i < arr.length; i++) {
            int num = arr[i];
            if (num > maxVal && num != Integer.MAX_VALUE) {
                maxVal = num;
                maxIdx = i;
            }
        }
        return maxIdx;
    }
}