import sys
import inspect
import os

class Debugger:
    # Set flag to true to print debug messages.
    # The flag can be set using the -debug command line option.
    DEBUG_FLAG = "-debug" in sys.argv

    # Helper method to print debug messages.
    @classmethod
    def debug(cls, debug_message):
        if cls.DEBUG_FLAG:
            frame = inspect.currentframe().f_back
            filename = os.path.basename(frame.f_globals["__file__"]) if "__file__" in frame.f_globals else "<stdin>"
            print(f"[DEBUG] {filename}: {debug_message}", file=sys.stderr)