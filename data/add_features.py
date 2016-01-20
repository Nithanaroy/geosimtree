#!/usr/bin/python

import sys, random, os


def main():
    if len(sys.argv) < 3:
        print "Two rumtime arguments required: input file path, output file path"
        return
        
    infile = sys.argv[1]
    outfile = sys.argv[2]
    # TODO: Argument for number of features
    inp = open(infile, 'r')
    out = open(outfile, 'w')

    line = inp.readline()

    while line:
        # latitude,longitude,feature1,feature2,feature3,feature4
        out.write("%s,%.2f,%.2f,%.2f,%.2f\n" % (line.strip(), get_random(-100, 100),
                get_random(-1000, 1000), get_random(-100, 100), get_random(-1000, 1000)))

        line = inp.readline()
    
    inp.close()
    out.close()


def get_random(start, stop):
    return random.random() * stop * 2 + start


if __name__ == '__main__':
    main()
