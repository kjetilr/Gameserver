#!/usr/bin/python
# coding: utf-8
import sys
import glob

def parseOneFile(filename):
    first_ts = long(0)    # First timestamp to be seen
    all_in = long(0)      # Timestamp where all clients have been connected
    ids = set()
    data=[]
    five_minutes = long(300000)       # Seconds in 5 minutes
    num_clients = 0
    # lese forste timestamp
    for line in open(filename,'r').readlines():
        wordList = line.split()
        if len(wordList) != 5:
            print "Wrong line length: " + str(len(wordList)) + " Discarded."
        else:
            id, ts, delay, dummy1, dummy2 = wordList
            t = id, ts, delay
            if first_ts == 0:
                # read timestamp
                first_ts=long(ts)
            data.append(t)
            ids.add(id)

    # Lagre antall unike klienter
    num_clients = len(ids)

    # Finne timestamp for punktet der alle IDer har logget minst en verdi
    for i in data:
        if len(ids) > 0:
            if i[0] in ids:
                ids.remove(i[0])
                all_in = long(i[1])

    # Legge til 5 minutter til første timestamp som er observert
    end_first = first_ts + five_minutes

    print "Number of connected clients in dump: " + str(num_clients)
    print "Nuber of logged delays: " + str(len(data))
    print "first_ts  : " + str(first_ts)
    print "all_in    : " + str(all_in)
    print "end_first : " + str(end_first)
    print "stable interval: " + str(end_first - all_in) + \
        "ms (" + str((end_first - all_in) / 1000) +"s)"

    # Prune data structure to include only stable period
    # Write stable stats to new file
    file = open("stable-" + filename , 'w')
    for t in data:
        if long(t[1]) >= all_in and long(t[1]) <= end_first:
            file.write(t[0] + "\t" + t[1] + "\t" + t[2] + "\n")

    file.close()

for filename in sys.argv[1:]:
    print "\nParsing: " + filename
    parseOneFile(filename)

