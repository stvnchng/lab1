package frontend;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> argList = Arrays.asList(args);
        boolean flagS = false, flagP = false, flagR = false;

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

        String path;
        // handle default behavior
        if (argList.stream().noneMatch(arg -> arg.contains("-"))) {
            flagP = true;
            path = args[0];
        } else {
            path = args[1];
        }

        BufferedReader br = new BufferedReader(new FileReader(path));

        Scanner scanner = new Scanner(br);
        Parser parser = new Parser();

        // go ahead and scan in all the goodies
        scanner.scanInput();

        if (flagS) {
            scanner.printTokens();
        }
        if (flagP) {
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
        System.out.println("If none is specified, the default action is '-p'.");
    }

    static void warning() {
        System.err.println("WARNING: The frontend only accepts one flag, and will use the highest priority flag. Priority is, from highest to lowest, -h, -r, -p, and -s.");
    }
}
