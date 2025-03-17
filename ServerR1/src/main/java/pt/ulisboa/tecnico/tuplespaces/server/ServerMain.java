package pt.ulisboa.tecnico.tuplespaces.server;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
public class ServerMain {
  // Set flag to true to print debug messages.
  // The flag can be set using the -debug command line option.
  private static boolean DEBUG_FLAG = (System.getProperty("debug") != null);

  // Helper method to print debug messages.
  public static void debug(String className, String debugMessage) {
      if (DEBUG_FLAG)
          System.err.println("[DEBUG] " + className + ": " + debugMessage);
  }

    public static void main(String[] args) throws IOException, InterruptedException {
      System.out.println(ServerMain.class.getSimpleName());

      for (String arg : args) {
        if (arg.equals("-debug")) {
            DEBUG_FLAG = true;
            debug(ServerMain.class.getSimpleName(), "Debug mode enabled");
            break;
        }
      }

      if (DEBUG_FLAG) {
        args = java.util.Arrays.stream(args)
            .filter(arg -> !arg.equals("-debug"))
            .toArray(String[]::new);

        // Receive and print arguments
        for (int i = 0; i < args.length; i++) {
          debug(ServerMain.class.getSimpleName(), String.format("arg[%d] = %s", i, args[i]));
        }
      }
      debug(ServerMain.class.getSimpleName(), String.format("Received %d arguments", args.length));


      // Check arguments
      if (args.length < 1) {
        System.err.println("Argument(s) missing!");
        System.err.printf("Usage: java %s port%n", ServerMain.class.getSimpleName());
        return;
      }

      final int port = Integer.parseInt(args[0]);
      final BindableService impl = new ServiceImpl();
      debug(ServerMain.class.getSimpleName(), "Server will listen on port " + port);

      // Create a new server to listen on port
      Server server = ServerBuilder.forPort(port).addService(impl).build();

      // Start the server
      server.start();

      // Server threads are running in the background.
      debug(ServerMain.class.getSimpleName(), "Server started, listening on " + port);

      // Do not exit the main thread. Wait until server is terminated.
      server.awaitTermination();
	}
}