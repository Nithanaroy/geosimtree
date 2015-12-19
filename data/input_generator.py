#!/usr/bin/python

import sys, random, os


def main():
    lines = int(sys.argv[2])
    outfile = sys.argv[1]
    # TODO: Argument for number of features
    f = open(outfile, 'w')
    print os.getcwd()
    for i in range(0, lines):
        f.write("%f,%f %.2f,%.2f,%.2f,%.2f\n" % (get_random(-180, 180), get_random(-180, 180), get_random(-100, 100),
                get_random(-1000, 1000), get_random(-100, 100), get_random(-1000, 1000)))
    f.close()


def get_random(start, stop):
    return random.random() * stop * 2 + start


if __name__ == '__main__':
    main()
