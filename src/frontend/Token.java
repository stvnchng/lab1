package frontend;

import java.util.HashMap;
import java.util.Map;

public class Token {
    enum POS {
        MEMOP,
        LOADI,
        ARITHOP,
        OUTPUT,
        NOP,
        CONST,
        REG,
        COMMA,
        INTO,
        NEWLINE,
        ENDFILE,
        SPACE,
        ERROR
    }

    enum Lexeme {
        load(0),
        store(1),
        loadI(2),
        add(3),
        sub(4),
        mult(5),
        lshift(6),
        rshift(7),
        output(8),
        nop(9);

        private final int opcode;

        private static final Map<Integer, Lexeme> map = new HashMap<>();

        static {
            for (Lexeme lexeme : Lexeme.values()) {
                map.put(lexeme.opcode, lexeme);
            }
        }

        Lexeme(int opcode) {
            this.opcode = opcode;
        }

        public int getInt() {
            return this.opcode;
        }

        public static Lexeme valueOf(int opcode) {
            return map.get(opcode);
        }
    }

    private final POS pos;
    private final String lexeme;
    public int lineNum;

    public Token(POS pos, String lexeme, int lineNum) {
        this.pos = pos;
        this.lexeme = lexeme;
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        String standardToken = "< " + this.pos + ", \"" + this.lexeme + "\" >";
        String errorMsg = "   \t\"" + this.lexeme + "\" is not a valid word.";
        return this.pos == POS.ERROR ? errorMsg : standardToken;
    }

    public POS getPOS() {
        return this.pos;
    }

    public String getLexeme() {
        return this.lexeme;
    }

    public int getOpcode(String lexeme) {
        return Lexeme.valueOf(lexeme).getInt();
    }
}
