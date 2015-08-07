import re
import DictionaryServices
import time
import sys, getopt
from datetime import datetime

createNetworkPattern = re.compile(r'\[NetworkAService\.createNetwork\]')

timeStampPattern  = re.compile(r'(\[\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d{3}\])')
logIdPattern      = re.compile(r'(\[\d{4}-\d{2}-\d{2}-\d{2}-\d{2}-\d{2}-\d{1,3}-\d+\])') 
classAndMethodNamePattern = re.compile(r'(\[[a-zA-Z0-9_]+\.[a-zA-Z0-9_]+\])$') 

createNetworkIDs = set() 

#print "sys.argv=", sys.argv


print "__name__=" , __name__
print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)

outputfile = None
try:
    opts, args = getopt.getopt(sys.argv[1:],"h:i:o:",["ifile=","ofile="])
    print 'opts=', opts,   '   args=',  args
except getopt.GetoptError:
    print 'test.py -i <ndex.log file> [-o <outputfile>]'
    sys.exit(2)
for opt, arg in opts:
    if opt == '-h':
        print 'test.py -i <ndex.log file> [-o <outputfile>]'
        sys.exit()
    elif opt in ("-i", "--ifile"):
        inputfile = arg
    elif opt in ("-o", "--ofile"):
        outputfile = arg

if outputfile is None:
    outputfile = inputfile + '.performance.txt'

print 'Input file is ', inputfile
print 'Output file is ', outputfile

#print ' Exit now ... bye'
#sys.exit(0)


# in this loop, we find all Unique transaction IDs of the [NetworkAService.createNetwork] API execution paths
# and place these IDs into the createNetworkIDs set
#with open("/Users/vrynkov/log/ndex.log", "r") as f:
with open(inputfile, "r") as f:    
   for line in f:
      if createNetworkPattern.search(line):
         #print line
         createNetworkIDs |= set(logIdPattern.findall(line))


#print createNetworkIDs
#print "len(createNetworkIDs)=", len(createNetworkIDs), "\ntransactions=", createNetworkIDs

# now, let' sort the transaction IDs in chronological order (transaction ID consists of (time stamp + thread id))
# we can' sort the set, so we need to convert it to list
createNetworkIDs = list(createNetworkIDs)
createNetworkIDs.sort()

#print "len(createNetworkIDs)=", len(createNetworkIDs), "\ntransactions=", createNetworkIDs

#print createNetworkIDs
#for transactionId in createNetworkIDs:
#    print transactionId  
    
filteredTransactionsFile = inputfile + ".filtered"

# now, create another file and copy there all [NetworkAService.createNetwork] from ndex.log
with open(inputfile, "r") as f, open(filteredTransactionsFile, "w") as w:
    #for transactionId in createNetworkIDs:
           for line in f:
               if logIdPattern.search(line):
                   # the line contains transaction ID; extract this ID
                   uniqueTransactionId = logIdPattern.search(line).group()
                   
                   #write this log to the new file
                   if uniqueTransactionId in createNetworkIDs:
                       w.write(line)
                       

allCreateNetworkTransactions = {}
           
# parse all the transactions from the newly generated createNetworks.log file, one transaction ID at a time
with open(filteredTransactionsFile, "r") as f:  #
            
   # myMap = {}
    listOfMethods = []
    
    for line in f:

        try:
            timeStamp, logType, clientIPAddress, transactionId, userAccount, logEntry, classAndMethodName = re.findall('\[(.*?)\]', line)
        except ValueError:
            continue
        
        if transactionId not in allCreateNetworkTransactions.keys():
            allCreateNetworkTransactions[transactionId] = { 'transactionProperty' : { 'clientIPAddress' : clientIPAddress,
                                                                                      'clientRTT' : None,
                                                                                      'userAccount' : userAccount,
                                                                                      'numberOfElements' : 0,
                                                                                      'networkName' : None,
                                                                                      'UUID' : None
                                                                                    },
                                                            'transactionData' : {}
                                                           } 
            
        if classAndMethodName not in allCreateNetworkTransactions[transactionId]['transactionData'].keys():
                 
            allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName] = {
                                         'methodName' : classAndMethodName,                                                 
                                         'timeStart' : None,
                                         'timeEnd' : None,
                                         'deltaTime' : None,
                                         'size' : 0,
                                         'name' : None,
                                         'memoryStart' : None,
                                         'memoryEnd' : None,
                                         'deltaMemory' : None }
            
            if classAndMethodName not in listOfMethods:
                listOfMethods.append(classAndMethodName)

        
        if logEntry:
            if logEntry.startswith('start:', 0, 6):
                allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['timeStart']=timeStamp
                
            if logEntry.startswith('end:', 0, 4):            
                allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['timeEnd']=timeStamp
                
                startTimeStamp = datetime.strptime(allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['timeStart'], "%Y-%m-%d %H:%M:%S,%f")
                endTimeStamp = datetime.strptime(timeStamp, "%Y-%m-%d %H:%M:%S,%f")
                timeDifference = endTimeStamp-startTimeStamp
                
                # timeDifference has type timedelta; timedelta has microseconds, seconds and days field; but not minutes and hours
                # here, we get the time difference in seconds
                timeDifferenceInSeconds = timeDifference.microseconds/1000000.0  + timeDifference.seconds + 24*60*60*timeDifference.days
                #print timeDifference, "{0:.3f}".format(timeDifferenceInSeconds)

                # get the time difference in seconds with 3 digits in fraction part 
                
                # allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['deltaTime']="{0:.3f}".format(timeDifferenceInSeconds)
                allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['deltaTime']="{:,.3f}".format(timeDifferenceInSeconds)                
            
            if logEntry.startswith('memory:', 0, 7):    
                heap = (logEntry.split('heap='))[1].split()[0]
                max  = (logEntry.split('max='))[1].split()[0]
                used = (logEntry.split('used='))[1].split()[0]
                free = logEntry.split('free=')[1].split()[0]
                
                if allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart'] is None:
                    allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart'] = {'heap':heap, 'max':max, 'used':used, 'free':free}
                else:    
                    allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['memoryEnd'] = {'heap':heap, 'max':max, 'used':used, 'free':free}
                    deltaHeap = int(heap.replace(',','')) - int((allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart']['heap']).replace(',',''))
                    deltaMax  = int(max.replace(',',''))  - int((allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart']['max']).replace(',',''))
                    deltaUsed = int(used.replace(',','')) - int((allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart']['used']).replace(',',''))
                    deltaFree = int(free.replace(',','')) - int((allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart']['free']).replace(',',''))
                    allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['deltaMemory'] = \
                       {'heap':"{:,}".format(deltaHeap), 'max':"{:,}".format(deltaMax), 'used':"{:,}".format(deltaUsed), 'free':"{:,}".format(deltaFree)}

            if "name=" in logEntry:
                networkName = (logEntry.split('name=\''))[1].split('\'')[0]
                allCreateNetworkTransactions[transactionId]['transactionProperty']['networkName'] = networkName

            if "UUID=" in logEntry:
                networkUUID = (logEntry.split('UUID=\''))[1].split('\'')[0]
                allCreateNetworkTransactions[transactionId]['transactionProperty']['UUID'] = networkUUID

            if "size=" in logEntry:
                propertySize = int((logEntry.split('size='))[1].split()[0])
                if (allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['size'] == 0) : 
                    allCreateNetworkTransactions[transactionId]['transactionProperty']['numberOfElements'] += propertySize
                    
                allCreateNetworkTransactions[transactionId]['transactionData'][classAndMethodName]['size'] = propertySize
                    
 # remove square brackets [] from transactionID
createNetworkIDs = [(transactionId.split('['))[1].split(']')[0] for transactionId in createNetworkIDs]


transactionsWithoutClientRTT = len(createNetworkIDs) - len(args)
print 'transactionsWithoutClientRTT=', transactionsWithoutClientRTT

print "type(args)=", type(args)

if len(args) > 0:
    
    loopCounter = 0
    
    # fill in transactionProperty:clientRTT with the arguments received from the caller
    for transactionId in createNetworkIDs:
        transaction = allCreateNetworkTransactions[transactionId]['transactionProperty']
        loopCounter += 1
        
        if loopCounter <= transactionsWithoutClientRTT:
            continue
        
        clientRTT = args.pop(0)
        allCreateNetworkTransactions[transactionId]['transactionProperty']['clientRTT'] = clientRTT
        

# print all transactions
print "allCreateNetworkTransactions:"

for transactionId in createNetworkIDs:
    transaction = allCreateNetworkTransactions[transactionId]

    print "   transactionId:", transactionId
    print "      transactionProperty:"
    print "         ", transaction['transactionProperty']
    print "      transactionData:"
        
    for method in listOfMethods:
        print "         ", transaction['transactionData'][method]
    
#for method in listOfMethods:
#    print method
     
# create headers
with open(outputfile, "w") as w:
    # print headers
    w.write("Transaction Id\tNetwork Name\tNetwork UUID\tClient RTT\tNumber of Elements\t")
    
    for method in listOfMethods:
        w.write(method)
        w.write("\t")
    
        transactionData = allCreateNetworkTransactions[createNetworkIDs[0]]['transactionData']
        if transactionData[method]['deltaMemory'] is not None:
            w.write(method + ".memoryDeltaHeap\t")
            w.write(method + ".memoryDeltaMax\t")
            w.write(method + ".memoryDeltaFree\t")
            w.write(method + ".memoryDeltaUsed\t")  

    w.write("\n") 
    
    
    #headers created, now write data
    for transactionId in createNetworkIDs:
        transaction = allCreateNetworkTransactions[transactionId]

        w.write(transactionId+"\t")   
        w.write(transaction['transactionProperty']['networkName']+"\t")
        w.write(transaction['transactionProperty']['UUID']+"\t")
        if transaction['transactionProperty']['clientRTT'] is not None: 
             w.write(transaction['transactionProperty']['clientRTT'])
        w.write("\t")
        w.write("{:,}".format(transaction['transactionProperty']['numberOfElements'])+"\t") 
            
        for method in listOfMethods:
            if transaction['transactionData'][method]['deltaTime'] is not None:
                w.write(transaction['transactionData'][method]['deltaTime'] + "\t")
            else:
                w.write("\t")
            
            if transaction['transactionData'][method]['deltaMemory'] is not None:
                w.write(str(transaction['transactionData'][method]['deltaMemory']['heap']) + "\t")
                w.write(str(transaction['transactionData'][method]['deltaMemory']['max']) + "\t")
                w.write(str(transaction['transactionData'][method]['deltaMemory']['used']) + "\t")
                w.write(str(transaction['transactionData'][method]['deltaMemory']['free']) + "\t")

        w.write('\n')


    


