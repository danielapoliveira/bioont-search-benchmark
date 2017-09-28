#!/usr/bin/python

import sys


def main(argv):
    if sys.argv[2] == "true":
        f = open("properties_error.txt", "a")
        f.write(sys.argv[1] + "\n")
        f.close()
    else:
        f = open("properties_loaded.txt", "a")
        f.write(sys.argv[1] + "\n")
        f.close()

if __name__ == "__main__":
   main(sys.argv)