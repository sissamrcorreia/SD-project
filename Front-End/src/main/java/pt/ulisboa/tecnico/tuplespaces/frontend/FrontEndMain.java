package pt.ulisboa.tecnico.tuplespaces.frontend;

import java.io.IOException;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.ulisboa.tecnico.tuplespaces.frontend.grcp.FrontEndService;

public class FrontEndMain {
    // Set flag to true to print debug messages.
    // The flag can be set using the -debug command line option.
    private static boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    // Helper method to print debug messages.
    public static void debug(String className, String debugMessage) {
        if (DEBUG_FLAG)
            System.err.println("[DEBUG] " + className + ": " + debugMessage);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(FrontEndMain.class.getSimpleName());

        for (String arg : args) {
            if (arg.equals("-debug")) {
                DEBUG_FLAG = true;
                debug(FrontEndMain.class.getSimpleName(), "Debug mode enabled");
                break;
            }
          }

        if (DEBUG_FLAG) {
            args = java.util.Arrays.stream(args)
                .filter(arg -> !arg.equals("-debug"))
                .toArray(String[]::new);

            // Receive and print arguments
            for (int i = 0; i < args.length; i++) {
              debug(FrontEndMain.class.getSimpleName(), String.format("arg[%d] = %s", i, args[i]));
            }
          }
          debug(FrontEndMain.class.getSimpleName(), String.format("Received %d arguments", args.length));

        // check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host:port> <client_port>");
            return;
        }

        // get the ports
        final int port = Integer.parseInt(args[0]);
        String tupleSpacesHost_port = args[1];

        FrontEndService Service = new FrontEndService(tupleSpacesHost_port);

        debug(FrontEndMain.class.getSimpleName(), "FrontEnd will connect to TupleSpaces on " + tupleSpacesHost_port);

        // Create a new server to listen on port
        Server frontEnd = ServerBuilder.forPort(port).addService(Service).build();

        // Start the server
        frontEnd.start();

        // Server threads are running in the background.
        debug(FrontEndMain.class.getSimpleName(), "FrontEnd started, listening on " + port);

        // Do not exit the main thread. Wait until it is terminated.
        frontEnd.awaitTermination();
    }
}