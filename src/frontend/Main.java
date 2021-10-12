package frontend;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> argList = Arrays.asList(args);
        boolean flagS = false, flagP = false, flagR = false, flagX = false, flagK = false;
        int k = -1;

        // check that the input args has only one flag
        if (argList.stream().filter(arg -> arg.contains("-")).count() > 1) warning();

        // -h flag invokes help menu
        if (argList.contains("-h")) {
            help();
            return;
        }
        // -r flag handles printing out the IRs
        else if (argList.contains("-r")) {
            flagR = true;
        }
        // -p flag invokes parser, which should report on success/failure and # of ops.
        else if (argList.contains("-p")) {
            flagP = true;
        }
        // -s flag invokes scanner on input file
        else if (argList.contains("-s")) {
            flagS = true;
        }
        // -x flag invokes renamer on input file
        else if (argList.contains("-x")) {
            flagX = true;
        }
        // h flag invokes allocate
        else {
            try {
                k = Integer.parseInt(argList.get(0));
                if (k >= 3 && k <= 64) {
                    flagK = true;
                } else {
                    warnK(k);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error: k flag invoked but value provided was not an integer. \nPlease try invoking ./412alloc with a value of k between 3 and 64 inclusive.");
                return;
            }
        }

        String path;
        // handle default behavior
        if (argList.stream().noneMatch(arg -> arg.contains("-")) && !flagK) {
            flagP = true;
            path = args[0];
        } else {
            path = args[1];
        }

        BufferedReader br = new BufferedReader(new FileReader(path));

        Scanner scanner = new Scanner(br);
        Parser parser = new Parser();
        Allocator allocator = new Allocator();

        // go ahead and scan in all the goodies
        scanner.scanInput();

        if (flagS) {
            scanner.printTokens();
        }
        if (flagP) {
            if (scanner.errorsPresent()) {
                scanner.reportErrors();
                return;
            }
            parser.parse(scanner);
            parser.printSummary();
        }
        if (flagR) {
            if (scanner.errorsPresent()) scanner.reportErrors();
            parser.parse(scanner);
            if (parser.errorsPresent()) {
                try {
                    Thread.sleep(10);
                    System.err.println("\nDue to syntax errors, run terminates.");
                } catch (Exception ignored) {}
                return;
            }
            parser.printIR();
        }
        if (flagX || flagK) {
            if (scanner.errorsPresent()) {
                System.out.println("\nError reading input file, simulator not run.");
                return;
            }
            parser.parse(scanner);
            if (parser.errorsPresent()) {
                try {
                    Thread.sleep(10);
                    System.err.println("\nError reading input file, simulator not run.");
                } catch (Exception ignored) {}
                return;
            }
            LinkedList<IR.Node> ops = parser.getIR();
            if (flagK) {
                System.out.println("Invoking allocator with k = " + k);
                return;
            }
            allocator.renameRegisters(ops);
        }
    }

    static void help() {
        System.out.println("Command Syntax:");
        System.out.println("\t\t./412fe [flags] filename");
        System.out.println("\nRequired arguments:");
        System.out.println("\t\tfilename is the pathname (absolute or relative) to the input file.");
        System.out.println("\nOptional flags:");
        System.out.println("\t\t-h\t\tprints this message");
        System.out.println("\t\t-l\t\topens log file \"./Log\" and starts logging.");
        System.out.println("\t\t-v\t\tprints version number");
        System.out.println("\nAt most one of the following three flags:");
        System.out.println("\t\t-s\t\tprints tokens in token stream");
        System.out.println("\t\t-p\t\tinvokes parser and reports on success or failure");
        System.out.println("\t\t-r\t\tprints human readable version of parser's IR");
        System.out.println("\t\t-x\t\tperforms register renaming and prints out resulting modified ILOC code");
        System.out.println("\t\tk\t\tinvokes allocator with a k-limit on registers, where k is an integer between 3 and 64 inclusive.");
        System.out.println("If none is specified, the default action is '-p'.");
    }

    static void warning() {
        System.err.println("WARNING: The frontend only accepts one flag, and will use the highest priority flag. Priority is, from highest to lowest, -h, -r, -p, and -s.");
    }

    static void warnK(int k) {
        System.err.println("WARNING: The value of k must be between 3 and 64 inclusive.");
    }
}
