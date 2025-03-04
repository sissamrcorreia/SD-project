package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grcp.ClientService;

public class ClientMain {
    // Set flag to true to print debug messages.
    // The flag can be set using the -debug command line option.
    private static boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    // Helper method to print debug messages.
    private static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            System.err.println("[DEBUG] " + debugMessage);
    }

    public static void main(String[] args) {
        System.out.println(ClientMain.class.getSimpleName());

        // receive and print arguments
		for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-debug")) {
                DEBUG_FLAG = true;
                debug("Debug mode enabled");
                continue;
            }
			debug(String.format("arg[%d] = %s", i, args[i]));
		}
		debug(String.format("Received %d arguments", args.length));

        // check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host:port> <client_id>");
            return;
        }

        // get the host and the port of the server or front-end
        final String host_port = args[0];
        final int client_id = Integer.parseInt(args[1]);
        final String target = host_port;
        debug("Target: " + target);

        CommandProcessor parser = new CommandProcessor(new ClientService(host_port, client_id));
        parser.parseInput();
    }
}
