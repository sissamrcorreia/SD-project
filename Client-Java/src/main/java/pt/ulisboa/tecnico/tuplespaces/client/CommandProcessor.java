package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.Scanner;

import pt.ulisboa.tecnico.tuplespaces.client.grcp.ClientService;

public class CommandProcessor {
    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";
    private static final String PUT = "put";
    private static final String READ = "read";
    private static final String TAKE = "take";
    private static final String SLEEP = "sleep";
    private static final String EXIT = "exit";
    private static final String GET_TUPLE_SPACES_STATE = "getTupleSpacesState";

    private final ClientService clientService;

    public CommandProcessor(ClientService clientService) {
        this.clientService = clientService;
    }

    void parseInput() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String[] split = line.split(SPACE);
             switch (split[0]) {
                case PUT:
                    this.put(split);
                    break;

                case READ:
                    this.read(split);
                    break;

                case TAKE:
                    this.take(split);
                    break;

                case GET_TUPLE_SPACES_STATE:
                    this.getTupleSpacesState();
                    break;

                case SLEEP:
                    this.sleep(split);
                    break;

                case EXIT:
                    exit = true;
                    break;

                default:
                    this.printUsage();
                    break;
             }
        }
        scanner.close();
        clientService.shutdown();
    }

    private void put(String[] split){
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        ClientMain.debug(CommandProcessor.class.getSimpleName(), "put: " + split[1]);

        // get the tuple
        String tuple = split[1];

        // put the tuple
        this.clientService.put(tuple);
        System.out.println("OK");
        System.out.println();
    }

    private void read(String[] split){
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];

        ClientMain.debug(CommandProcessor.class.getSimpleName(), "read: " + tuple);

        // read the tuple
        String result = this.clientService.read(tuple);
        ClientMain.debug(CommandProcessor.class.getSimpleName(), "read: " + result);

        // print the result if
        if (result != null) {
            System.out.println("OK");
            System.out.println(result);
        } else {
            System.out.println("Tuple not found");
            return;
        }
        System.out.println();
    }


    private void take(String[] split) {
         // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];

        // take the tuple
        String response = this.clientService.take(tuple);
        ClientMain.debug(CommandProcessor.class.getSimpleName(), "take: " + response);

        if (response != null) {
            System.out.println("OK");
            System.out.println(response);
        } else {
            System.out.println("Tuple not found");
            return;
        }
        System.out.println();
    }

    private void getTupleSpacesState() {
        ClientMain.debug(CommandProcessor.class.getSimpleName(), "getTupleSpacesState");
        System.out.println("OK");

        // get the tuple spaces state
        this.clientService.getTupleSpacesState();

        System.out.println();
    }

    private void sleep(String[] split) {
      if (split.length != 2){
        this.printUsage();
        return;
      }
      Integer time;

      // checks if input String can be parsed as an Integer
      try {
         time = Integer.parseInt(split[1]);
      } catch (NumberFormatException e) {
        this.printUsage();
        return;
      }

      try {
        ClientMain.debug(CommandProcessor.class.getSimpleName(), "sleep: " + time);
        Thread.sleep(time*1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                "- put <element[,more_elements]>\n" +
                "- read <element[,more_elements]>\n" +
                "- take <element[,more_elements]>\n" +
                "- getTupleSpacesState <server>\n" +
                "- sleep <integer>\n" +
                "- exit\n");
    }

    private boolean inputIsValid(String[] input){
        if (input.length < 2
            ||
            !input[1].substring(0,1).equals(BGN_TUPLE)
            ||
            !input[1].endsWith(END_TUPLE)
            ||
            input.length > 2
            ) {
            return false;
        }
        else {
            return true;
        }
    }
}
