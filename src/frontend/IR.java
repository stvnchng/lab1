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

    public static class Node {

        private final int opcode;
        private final int[] op1;
        private final int[] op2;
        private final int[] op3;
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
                        System.err.println("Error occurred while building IR");
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
