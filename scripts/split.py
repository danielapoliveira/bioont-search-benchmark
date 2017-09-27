#!/usr/bin/python

import sys

def main(argv):
    for arg in sys.argv[1:]:
        t = str(arg)
        t = t.split("application-")[-1].split(".properties")[0]
        print t
        return t     

if __name__ == "__main__":
   main(sys.argv)
