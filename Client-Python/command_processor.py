from typing import List
from debug import Debugger
import time

class CommandProcessor:
    SPACE = " "
    BGN_TUPLE = "<"
    END_TUPLE = ">"
    PUT = "put"
    READ = "read"
    TAKE = "take"
    EXIT = "exit"
    SLEEP = "sleep"
    GET_TUPLE_SPACES_STATE = "getTupleSpacesState"

    def __init__(self, client_service):
        self.client_service = client_service

    def parse_input(self):
        exit_flag = False
        try:
            while not exit_flag:
                try:
                    line = input("> ").strip()
                    split = line.split(self.SPACE)
                    command = split[0]

                    if command == self.PUT:
                        self.put(split)
                    elif command == self.READ:
                        self.read(split)
                    elif command == self.TAKE:
                        self.take(split)
                    elif command == self.GET_TUPLE_SPACES_STATE:
                        self.get_tuple_spaces_state()
                    elif command == self.SLEEP:
                        self.sleep(split)
                    elif command == self.EXIT:
                        exit_flag = True
                    else:
                        self.print_usage()
                except EOFError:
                    break
        except KeyboardInterrupt:
            self.client_service.shutdown()

    def put(self, split):
        # check if input is valid
        if not self.input_is_valid(split):
            self.print_usage()
            return

        # get the tuple
        tuple_value = split[1]

        Debugger.debug("put: " + tuple_value)

        # put the tuple
        self.client_service.put(tuple_value)

        print()


    def read(self, split):
        # check if input is valid
        if not self.input_is_valid(split):
            self.print_usage()
            return

        # get the tuple
        tuple_value = split[1]

        Debugger.debug("read: " + tuple_value)

        # read the tuple
        result = self.client_service.read(tuple_value)

        # print the result if
        if result is not None:
            print(result)
        else:
            # Server is down
            return
        print()


    def take(self, split):
        # check if input is valid
        if not self.input_is_valid(split):
            self.print_usage()
            return

        # get the tuple
        tuple_value = split[1]
        Debugger.debug("take: " + tuple_value)

        # take the tuple
        response = self.client_service.take(tuple_value)

        if response is not None:
            print(response)
        else:
            # Server is down
            return
        print()

    def get_tuple_spaces_state(self):
        Debugger.debug("getTupleSpacesState")

        # get the tuple spaces state
        self.client_service.get_tuple_spaces_state()

        print()

    def sleep(self, split):
        if len(split) != 2:
            self.print_usage()
            return

        try:
            time_s = int(split[1])
        except ValueError:
            self.print_usage()
            return

        Debugger.debug(f"sleep: {time_s}")
        time.sleep(time_s)


    def print_usage(self):
        print("Usage:\n"
              "- put <element[,more_elements]>\n"
              "- read <element[,more_elements]>\n"
              "- take <element[,more_elements]>\n"
              "- getTupleSpacesState\n"
              "- sleep <integer>\n"
              "- exit\n")

    def input_is_valid(self, input_data: List[str]) -> bool:
        if (len(input_data) < 2
                or not input_data[1].startswith(self.BGN_TUPLE)
                or not input_data[1].endswith(self.END_TUPLE)
                or len(input_data) > 2):
            return False
        return True