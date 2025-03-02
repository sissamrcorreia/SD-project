package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import pt.ulisboa.tecnico.SingleServer.contract.SingleServerGrpc;

import pt.ulisboa.tecnico.tuplespaces.client.grcp.ClientService;

public class ClientMain {

    public static void main(String[] args) {

        /** Set flag to true to print debug messages. 
	    * The flag can be set using the -Ddebug command line option. */
	    //private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null); //fix

        /** Helper method to print debug messages. */
        //private static void debug(String debugMessage) {
            //if (DEBUG_FLAG)
                //System.err.println(debugMessage);
        //}

        System.out.println(ClientMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length != 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host:port> <client_id>");
            return;
        }

        // get the host and the port of the server or front-end
        final String host_port = args[0];
        final int client_id = Integer.parseInt(args[1]);
        final String target = host_port + ":" + client_id;

        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        SingleServerGrpc.SingleServerBlockingStub stub = SingleServerGrpc.newBlockingStub(channel);

        channel.shutdown();

        CommandProcessor parser = new CommandProcessor(new ClientService(host_port, client_id));
        parser.parseInput();
    }
}
