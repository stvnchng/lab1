package frontend;

import java.util.LinkedList;

public class IR {
    private final LinkedList<Node> nodes = new LinkedList<>();

    public int size() {
        return nodes.size();
    }

    public void add(Node op) {
        nodes.addLast(op);
    }

    public void printIR() {
        nodes.forEach(System.out::println);
    }

    public LinkedList<Node> getOps() {
        return this.nodes;
    }

    public static class Node {

        public final int opcode;
        public final int[] op1;
        public final int[] op2;
        public final int[] op3;
        private boolean constant = false;

        private static final int SR = 0;
        private static final int VR = 1;
        private static final int PR = 2;
        private static final int NU = 3;

        // loadI
        public Node(int opcode, int[] op1, int[] op2, int[] op3, boolean constant) {
            this.opcode = opcode;
            this.op1 = op1;
            this.op2 = op2;
            this.op3 = op3;
            this.constant = constant;
        }

        // other stuff
        public Node(int opcode, int[] op1, int[] op2, int[] op3) {
            this.opcode = opcode;
            this.op1 = op1;
            this.op2 = op2;
            this.op3 = op3;
        }

        // output
        public Node(int opcode, int[] op1) {
            this.opcode = opcode;
            this.op1 = op1;
            this.op2 = new int[0];
            this.op3 = new int[0];
            this.constant = true;
        }

        // nop
        public Node(int opcode) {
            this.opcode = opcode;
            this.op1 = new int[0];
            this.op2 = new int[0];
            this.op3 = new int[0];
        }

        public boolean isARITHOP() {
            return this.opcode >= 3 && this.opcode <= 7;
        }

        public boolean usesOP1() {
            return this.opcode == 0 || this.opcode == 1 || isARITHOP();
        }

        public boolean usesOP2() {
            return isARITHOP();
        }

        public boolean usesOP3() {
            return !(this.opcode == 8 || this.opcode == 9);
        }

        public int getMaxRegister(int type) {
            int maxRegister = 0;
            if (constant) return -1;
            if (op1.length > 0 && op1[0] > maxSR) maxSR = op1[0];
            if (op2.length > 0 && op2[0] > maxSR) maxSR = op2[0];
            if (op3.length > 0 && op3[0] > maxSR) maxSR = op3[0];
            return maxSR;
        }

        public String getOperandString(int[] operand, boolean constant) {
            StringBuilder cell = new StringBuilder("[ ");
            for (int i = 0; i < operand.length; i++) {
                if (constant) {
                    cell.append("val ").append(operand[i]);
                    continue;
                }
                switch (i) {
                    case SR:
                        cell.append("sr").append(operand[SR]);
                        break;
                    case VR:
                        cell.append(", vr").append(operand[VR]);
                        break;
                    case PR:
                        cell.append(", pr").append(operand[VR]);
                        break;
                    case NU:
                        cell.append(", nu").append(operand[VR]);
                        break;
                    default:
                        System.err.println("Error occurred while building IR operand string...");
                        break;
                }
            }
            return cell + " ],  ";
        }

        @Override
        public String toString() {
            return (Token.Lexeme.valueOf(opcode).equals(Token.Lexeme.add) || Token.Lexeme.valueOf(opcode).equals(Token.Lexeme.sub) || Token.Lexeme.valueOf(opcode).equals(Token.Lexeme.nop)) ?
                    Token.Lexeme.valueOf(opcode) + "\t\t" + getOperandString(op1, constant) + getOperandString(op2, false) + getOperandString(op3, false) :
                    Token.Lexeme.valueOf(opcode) + "\t" + getOperandString(op1, constant) + getOperandString(op2, false) + getOperandString(op3, false);
        }
    }
}
