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
  private static void debug(String debugMessage) {
      if (DEBUG_FLAG)
          System.err.println("[DEBUG] " + debugMessage);
  }

    public static void main(String[] args) throws IOException, InterruptedException {
      System.out.println(ServerMain.class.getSimpleName());

      // Receive and print arguments
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-debug")) {
                DEBUG_FLAG = true;
                debug("Debug mode enabled");
                continue;
            }
			  debug(String.format("arg[%d] = %s", i, args[i]));
      }
		  debug(String.format("Received %d arguments", args.length));

      // Check arguments
      if (args.length < 1) {
        System.err.println("Argument(s) missing!");
        System.err.printf("Usage: java %s port%n", ServerMain.class.getName());
        return;
      }

      final int port = Integer.parseInt(args[0]);
      final BindableService impl = new ServiceImpl();
      debug("Server will listen on port " + port);

      // Create a new server to listen on port
      Server server = ServerBuilder.forPort(port).addService(impl).build();

      // Start the server
      server.start();

      // Server threads are running in the background.
      debug("Server started, listening on " + port);

      // Do not exit the main thread. Wait until server is terminated.
      server.awaitTermination();
	}
}