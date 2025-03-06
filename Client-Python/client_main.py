import sys
from typing import List

from client_service import ClientService as ClientService
from command_processor import CommandProcessor

DEBUG_FLAG = "-debug" in sys.argv
class ClientMain:
    def debug(debug_message):
        if DEBUG_FLAG:
            print(f"[DEBUG] {debug_message}", file=sys.stderr)

    @staticmethod
    def main(args: List[str]):
        print("ClientMain") # TODO: Check

        # receive and print arguments
        for i, arg in enumerate(args):
            DEBUG_FLAG = True
            ClientMain.debug(f"arg[{i}] = {arg}")

        # check arguments
        if len(args) < 2:
            print("Argument(s) missing!", file=sys.stderr)
            print("Usage: python3 client_main.py <host:port> <client_id>", file=sys.stderr)
            return

        # Get the host and port of the server or front-end
        host_port = args[0]
        client_id = args[1]

        parser = CommandProcessor(ClientService(host_port, client_id))
        parser.parse_input()

if __name__ == "__main__":
    ClientMain.main(sys.argv[1:])