import re
import DictionaryServices
import time
import sys, getopt
from datetime import datetime
import json

createNetworkPattern   = re.compile(r'\[NetworkAService\.createNetwork\]')
uploadNetworkPattern   = re.compile(r'\[NetworkAService\.uploadNetwork\]')
downloadNetworkPattern = re.compile(r'\[NetworkAService\.getCompleteNetwork\]')
setNetworkFlagPattern  = re.compile(r'\[NetworkAService\.setNetworkFlag\]')
queryNetworkPattern    = re.compile(r'\[NetworkAService\.queryNetwork\]')


allAPIs = []
allAPIs.append(createNetworkPattern.pattern)
allAPIs.append(uploadNetworkPattern.pattern)
allAPIs.append(downloadNetworkPattern.pattern)
allAPIs.append(setNetworkFlagPattern.pattern)
allAPIs.append(queryNetworkPattern.pattern)


# remove square brackets [] from transactionID
# createNetworkIDs = [(transactionId.split('['))[1].split(']')[0] for transactionId in createNetworkIDs]
#allAPIs = [(allAPIs.split('['))[1].split(']')[0] for transactionId in allAPIs]

# create  a list of all patterns as a list of strings; all
#allAPIs = ['NetworkAService.createNetwork', 'NetworkAService.uploadNetwork', ..., 'NetworkAService.setNetworkFlag']
allAPIs = [pattern.replace('\\','').replace('[','').replace(']','') for pattern in allAPIs]
#print "allAPIs=", allAPIs

#timeStampPattern  = re.compile(r'(\[\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d{3}\])')
logIdPattern      = re.compile(r'(\[\d{4}-\d{2}-\d{2}-\d{2}-\d{2}-\d{2}-\d{1,3}-\d+\])') 
classAndMethodNamePattern = re.compile(r'(\[[a-zA-Z0-9_]+\.[a-zA-Z0-9_]+\])$') 

createNetworkIDs   = set() 
uploadNetworkIDs   = set()
downloadNetworkIDs = set()
queryNetworkIDs    = set()
setNetworkFlagIDs  = set()

allAPIsWithTransactions = []
#print "sys.argv=", sys.argv



#print 'Number of arguments:', len(sys.argv), 'arguments.'
#print 'Argument List sys.argv=', str(sys.argv)
#print 'Argument List str(sys.argv)=', str(sys.argv)


outputfile = None
inputfile  = None
try:
    opts, args = getopt.getopt(sys.argv[1:],"h:i:o:",["ifile=","ofile="])
    #print 'opts=', opts,   '   args=',  args
except getopt.GetoptError:
    print '\n\tusage:  mine.py -i <ndex.log file> [-o <outputfile>]\n'
    sys.exit(2)
for opt, arg in opts:
    if opt == '-h':
        print '\n\tusage:  mine.py -i <ndex.log file> [-o <outputfile>]\n'
        sys.exit()
    elif opt in ("-i"):
        inputfile = arg
    elif opt in ("-o"):
        outputfile = arg
        
if inputfile is None:
    print '\n\tusage:  mine.py -i <ndex.log file> [-o <outputfile>]\n'
    sys.exit()
            
if outputfile is None:
    outputfile = inputfile + '.performance.txt'
    



#print 'Input file is ', inputfile
#print 'Output file is ', outputfile

#print "type(args)=", type(args)
clientArgs = args[0] if len(args) > 0 else []
#print "type(clientArgs)=", type(clientArgs)
#print "clientArgs=", clientArgs


clientArgsDictionary = json.loads(clientArgs) if len(clientArgs) > 0 else {}
#print "type(clientArgsDictionary)=", type(clientArgsDictionary)
#print 'clientArgsDictionary=', clientArgsDictionary


#print json.dumps(clientArgsDictionary, indent=3)



# in this loop, we find all Unique transaction IDs of the [NetworkAService.createNetwork] API execution paths
# and place these IDs into the createNetworkIDs set
#with open("/Users/vrynkov/log/ndex.log", "r") as f:
with open(inputfile, "r") as f:    
   for line in f:
      if createNetworkPattern.search(line):
         createNetworkIDs |= set(logIdPattern.findall(line))         
      if uploadNetworkPattern.search(line):
         uploadNetworkIDs |= set(logIdPattern.findall(line))
      if downloadNetworkPattern.search(line):
         downloadNetworkIDs |= set(logIdPattern.findall(line))
      if setNetworkFlagPattern.search(line):
         setNetworkFlagIDs |= set(logIdPattern.findall(line)) 
      if queryNetworkPattern.search(line):
         queryNetworkIDs |= set(logIdPattern.findall(line))
   
         
#print createNetworkIDs
#print "len(createNetworkIDs)=", len(createNetworkIDs), "\ntransactions=", createNetworkIDs

# now, let' sort the transaction IDs in chronological order (transaction ID consists of (time stamp + thread id))
# we can' sort the set, so we need to convert it to list
createNetworkIDs = list(createNetworkIDs)
createNetworkIDs.sort()
uploadNetworkIDs = list(uploadNetworkIDs)
uploadNetworkIDs.sort()
downloadNetworkIDs = list(downloadNetworkIDs)
downloadNetworkIDs.sort()
queryNetworkIDs    = list(queryNetworkIDs)
queryNetworkIDs.sort()
setNetworkFlagIDs  = list(setNetworkFlagIDs)
setNetworkFlagIDs.sort()
allTransactionsIDs = createNetworkIDs + uploadNetworkIDs + downloadNetworkIDs + setNetworkFlagIDs + queryNetworkIDs
allTransactionsIDs.sort()

'''
--- printing for debugging
'''
#print "createNetworkIDs        =", createNetworkIDs
#print "uploadNetworkIDs        =", uploadNetworkIDs
#print "downloadNetworkIDs      =", downloadNetworkIDs
#print "setNetworkFlagIDs       =", setNetworkFlagIDs
#print "queryNetworkIDs         =", queryNetworkIDs
#print "allTransactionsIDs      =", allTransactionsIDs


#print "len(createNetworkIDs)   =", len(createNetworkIDs)
#print "len(uploadNetworkIDs)   =", len(uploadNetworkIDs)
#print "len(downloadNetworkIDs) =", len(downloadNetworkIDs)
#print "len(setNetworkFlagIDs)  =", len(setNetworkFlagIDs)
#print "len(queryNetworkIDs)    =", len(queryNetworkIDs)
#print "len(allTransactionsIDs) =", len(allTransactionsIDs)


#print "len(createNetworkIDs)=", len(createNetworkIDs), "\ntransactions=", createNetworkIDs

#print createNetworkIDs
#for transactionId in createNetworkIDs:
#    print transactionId  
    
filteredTransactionsFile = inputfile + ".filtered"

# now, create another file and copy there all transactions for the APIs from ndex.log
with open(inputfile, "r") as f, open(filteredTransactionsFile, "w") as w:
    #for transactionId in createNetworkIDs:
    for line in f:
        if logIdPattern.search(line):
            # the line contains transaction ID; extract this ID
            uniqueTransactionId = logIdPattern.search(line).group(1)
                   
            #write this log to the new file
            if uniqueTransactionId in allTransactionsIDs:
                w.write(line)
                       

allTransactions = {}
           
# parse all the transactions from the newly generated filtered ndex log file, one transaction ID at a time
with open(filteredTransactionsFile, "r") as f:
            
    ##listOfMethods = []
    allAPIsAndTransactionIDs = dict()
    for line in f:

        try:
            timeStamp, logType, clientIPAddress, transactionId, userAccount, logEntry, classAndMethodName = re.findall('\[(.*?)\]', line)
        except ValueError:
            continue
        
        if transactionId not in allTransactions.keys():
            allTransactions[transactionId] = { 'transactionProperty' : { 'clientIPAddress' : clientIPAddress,
                                                                         'clientRTT' : None,
                                                                         'userAccount' : userAccount,
                                                                         'numberOfElements' : 0,
                                                                         'networkName' : None,
                                                                         'UUID' : None,
                                                                         'api' : None,
                                                                         'fileSize' : None,
                                                                         'depth' : None,
                                                                         'nodesRetrieved' : None,
                                                                         'edgesRetrieved' : None
                                                                        },
                                                'transactionData' : {}
                                            } 
        #if classAndMethodName in clientArgsDictionary:
        #    allTransactions[transactionId]['transactionData'][classAndMethodName]
            
        if classAndMethodName not in allTransactions[transactionId]['transactionData'].keys():
                 
            allTransactions[transactionId]['transactionData'][classAndMethodName] = {
                                         'methodName' : classAndMethodName,                                                 
                                         'timeStart' : None,
                                         'timeEnd' : None,
                                         'deltaTime' : None,
                                         'size' : 0,
                                         'name' : None,
                                         'memoryStart' : None,
                                         'memoryEnd' : None,
                                         'deltaMemory' : None }
            
            #if classAndMethodName not in listOfMethods:
            #    listOfMethods.append(classAndMethodName)

        
        if logEntry:
            if classAndMethodName in allAPIs:
                allTransactions[transactionId]['transactionProperty']['api'] = classAndMethodName
                
                # create a dictionary with <Class.MethodName> as a key and another dictionary of  (transaction IDs and APIs) as value, for example:    
                # allAPIsAndTransactionIDs= 
                #   { "NetworkAService.getCompleteNetwork": {   "methods": ["NetworkAService.getCompleteNetwork"], 
                #                                               "transactions": ["2015-08-25-14-59-28-529-172", "2015-08-25-14-59-38-714-172", "2015-08-25-14-59-50-779-225", "2015-08-25-15-00-02-068-225"]
                #                                           }, 
                #     "NetworkAService.setNetworkFlag":     {   "methods": ["NetworkAService.setNetworkFlag", "ClientTaskProcessor.run", "AddNetworkToCacheTask.call"], 
                #                                               "transactions": ["2015-08-25-14-59-28-669-173", "2015-08-25-14-59-38-744-173", "2015-08-25-14-59-52-024-226", "2015-08-25-15-00-02-140-226"]
                #                                           },
                #     "NetworkAService.queryNetwork":       {   "methods": ["NetworkAService.queryNetwork"], 
                #                                               "transactions": ["2015-08-25-15-00-14-201-279", "2015-08-25-15-00-14-725-305", "2015-08-25-15-00-26-845-359", "2015-08-25-15-00-27-432-386"]
                #                                           }, 
                #     "NetworkAService.uploadNetwork":      {   "methods": ["NetworkAService.uploadNetwork", "ClientTaskProcessor.run", "FileUploadTask.processFile"], 
                #                                                "transactions": ["2015-08-25-14-58-54-510-92", "2015-08-25-14-59-06-272-119", "2015-08-25-14-59-18-060-146", "2015-08-25-14-59-40-261-199", "2015-08-25-15-00-03-785-253", "2015-08-25-15-00-16-285-333"]
                #                                           }, 
                #     "NetworkAService.createNetwork":      {   "methods": ["NetworkAService.createNetwork", "NdexNetworkCloneService.cloneNetwork", "NdexNetworkCloneService.cloneNetworkElements", 
                #                                                           "NdexNetworkCloneService.cloneBaseTerms", "NdexNetworkCloneService.cloneCitations", "NdexNetworkCloneService.cloneSupports",
                #                                                           "NdexNetworkCloneService.cloneFunctionTermVertex", "NdexNetworkCloneService.cloneNodes", "NdexNetworkCloneService.cloneEdges", 
                #                                                           "NdexNetworkCloneService.createLinksFunctionTerm"], 
                #                                               "transactions": ["2015-08-25-14-58-37-158-39", "2015-08-25-14-58-41-327-66"]
                #                                            }
                #   }                
                if classAndMethodName not in allAPIsAndTransactionIDs.keys():
                    allAPIsAndTransactionIDs[classAndMethodName] = dict();
                    allAPIsAndTransactionIDs[classAndMethodName]['transactions']=[]
                    allAPIsAndTransactionIDs[classAndMethodName]['methods']=[classAndMethodName]
                    
                if transactionId not in allAPIsAndTransactionIDs[classAndMethodName]['transactions']:
                    allAPIsAndTransactionIDs[classAndMethodName]['transactions'].append(transactionId)
                
            else:
                for api in allAPIsAndTransactionIDs:
                    allTransactionsForThisApi = allAPIsAndTransactionIDs[api]['transactions']
                    if transactionId in allTransactionsForThisApi:
                        if classAndMethodName not in  allAPIsAndTransactionIDs[api]['methods']:
                            allAPIsAndTransactionIDs[api]['methods'].append(classAndMethodName)
                    
                #if allAPIsAndTransactionIDs[classAndMethodName]['transactions'].append(transactionId)
                


            if logEntry.startswith('start:', 0, 6):
                allTransactions[transactionId]['transactionData'][classAndMethodName]['timeStart']=timeStamp
                
            if logEntry.startswith('end:', 0, 4):            
                allTransactions[transactionId]['transactionData'][classAndMethodName]['timeEnd']=timeStamp
                
                startTimeStamp = datetime.strptime(allTransactions[transactionId]['transactionData'][classAndMethodName]['timeStart'], "%Y-%m-%d %H:%M:%S,%f")
                endTimeStamp = datetime.strptime(timeStamp, "%Y-%m-%d %H:%M:%S,%f")
                timeDifference = endTimeStamp-startTimeStamp
                
                # timeDifference has type timedelta; timedelta has microseconds, seconds and days field; but not minutes and hours
                # here, we get the time difference in seconds
                timeDifferenceInSeconds = timeDifference.microseconds/1000000.0  + timeDifference.seconds + 24*60*60*timeDifference.days
                #print timeDifference, "{0:.3f}".format(timeDifferenceInSeconds)

                # get the time difference in seconds with 3 digits in fraction part        
                # allTransactions[transactionId]['transactionData'][classAndMethodName]['deltaTime']="{0:.3f}".format(timeDifferenceInSeconds)
                allTransactions[transactionId]['transactionData'][classAndMethodName]['deltaTime']="{:,.3f}".format(timeDifferenceInSeconds)                
            
            if logEntry.startswith('memory:', 0, 7):    
                heap = (logEntry.split('heap='))[1].split()[0]
                max  = (logEntry.split('max='))[1].split()[0]
                used = (logEntry.split('used='))[1].split()[0]
                free = logEntry.split('free=')[1].split()[0]
                
                if allTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart'] is None:
                    allTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart'] = {'heap':heap, 'max':max, 'used':used, 'free':free}
                else:    
                    allTransactions[transactionId]['transactionData'][classAndMethodName]['memoryEnd'] = {'heap':heap, 'max':max, 'used':used, 'free':free}
                    deltaHeap = int(heap.replace(',','')) - int((allTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart']['heap']).replace(',',''))
                    deltaMax  = int(max.replace(',',''))  - int((allTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart']['max']).replace(',',''))
                    deltaUsed = int(used.replace(',','')) - int((allTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart']['used']).replace(',',''))
                    deltaFree = int(free.replace(',','')) - int((allTransactions[transactionId]['transactionData'][classAndMethodName]['memoryStart']['free']).replace(',',''))
                    allTransactions[transactionId]['transactionData'][classAndMethodName]['deltaMemory'] = \
                       {'heap':"{:,}".format(deltaHeap), 'max':"{:,}".format(deltaMax), 'used':"{:,}".format(deltaUsed), 'free':"{:,}".format(deltaFree)}

            if "name=" in logEntry:
                networkName = (logEntry.split('name=\''))[1].split('\'')[0]
                allTransactions[transactionId]['transactionProperty']['networkName'] = networkName

            if "UUID=" in logEntry:
                networkUUID = (logEntry.split('UUID=\''))[1].split('\'')[0]
                allTransactions[transactionId]['transactionProperty']['UUID'] = networkUUID

            if "size=" in logEntry:
                propertySize = int((logEntry.split('size='))[1].split()[0])
                if (allTransactions[transactionId]['transactionData'][classAndMethodName]['size'] == 0) : 
                    allTransactions[transactionId]['transactionProperty']['numberOfElements'] += propertySize
                    
                allTransactions[transactionId]['transactionData'][classAndMethodName]['size'] = propertySize

            if "fileSize=" in logEntry:
                allTransactions[transactionId]['transactionProperty']['fileSize'] = int((logEntry.split('fileSize='))[1].split()[0])
            
            if "depth=" in logEntry:
                allTransactions[transactionId]['transactionProperty']['depth'] = int((logEntry.split('depth='))[1].split()[0])

            if "nodes=" in logEntry:
                allTransactions[transactionId]['transactionProperty']['nodesRetrieved'] = int((logEntry.split('nodes='))[1].split()[0])
                
            if "edges=" in logEntry:
                allTransactions[transactionId]['transactionProperty']['edgesRetrieved'] = int((logEntry.split('edges='))[1].split()[0])                            
                    
 # remove square brackets [] from transactionID
createNetworkIDs = [(transactionId.split('['))[1].split(']')[0] for transactionId in createNetworkIDs]



''' -------------------------------------------------------------------------------------------------------- 
transactionsWithoutClientRTT = len(createNetworkIDs) - len(args)
print 'transactionsWithoutClientRTT=', transactionsWithoutClientRTT


print "allTransactionsIDs=", allTransactionsIDs
print "type(args)=", type(args)

print "type(sorted(allTransactions))=", type(sorted(allTransactions))
print "type(allTransactions)=", type(allTransactions)
--------------------------------------------------------------------------------------------------------  '''


print "\nclient args=", json.dumps(clientArgsDictionary, indent=10)
print "\nallAPIs=", json.dumps(allAPIs, indent=10)
print "\nallAPIsAndTransactionIDs=", json.dumps(allAPIsAndTransactionIDs, indent=10)


#print "\n\nallTransactions=", json.dumps(allTransactions, indent=6, sort_keys=True)


# create headers
with open(outputfile, "w") as w:
    for api in  allAPIs:

        if api not in allAPIsAndTransactionIDs.keys():
            continue
        
        apiTransactions    = allAPIsAndTransactionIDs[api]
        apiMethods         = allAPIsAndTransactionIDs[api]['methods']
        apiTransactionsIDs = allAPIsAndTransactionIDs[api]['transactions']
    
        #print "api                =", api
        #print "apiMethods         =", apiMethods
        #print "apiTransactionsIDs =", apiTransactionsIDs
    
    
        clientNetworkNames = []
        clientFileSizes = []
        clientRTTs = []      
        clientReadOnlyRTTs = []                                  
        clientNodesRetrieved = []             
        clientEdgesRetrieved = []                     
        clientNodes = []             
        clientEdges = []
    
        # check if file name was passed to this script as argument from the client (caller of the script)
        if api in clientArgsDictionary:
            clientDataDictionary = clientArgsDictionary[api]
            if "networkName" in clientDataDictionary:
                clientNetworkNames = clientDataDictionary["networkName"]
            if "fileSize" in clientDataDictionary:
                clientFileSizes = clientDataDictionary["fileSize"] 
            if "clientRTT" in clientDataDictionary:
                clientRTTs = clientDataDictionary["clientRTT"]      
            if "clientReadOnlyRTT" in clientDataDictionary:
                clientReadOnlyRTTs = clientDataDictionary["clientReadOnlyRTT"]                                  
            if "nodesRetrieved" in clientDataDictionary:
                clientNodesRetrieved = clientDataDictionary["nodesRetrieved"]             
            if "edgesRetrieved" in clientDataDictionary:
                clientEdgesRetrieved = clientDataDictionary["edgesRetrieved"]                     
            if "nodes" in clientDataDictionary:
                clientNodes = clientDataDictionary["nodes"]             
            if "edges" in clientDataDictionary:
                clientEdges = clientDataDictionary["edges"]                      
                    
    
        # print headers
        if api in "NetworkAService.queryNetwork":
            w.write("Transaction Id\tNetwork File\tFile Size\tNetwork UUID\tClient RTT\tNumber of Elements\tNodes\tEdges\tNodes Retrieved\tEdges Retrieved\tDepth\t")
        else:
            w.write("Transaction Id\tNetwork File\tFile Size\tNetwork UUID\tClient RTT\tNumber of Elements\t")
        
        for method in apiMethods:
            w.write(method)
            w.write("\t")
    
            transactionData = allTransactions[apiTransactionsIDs[0]]['transactionData']
            if transactionData[method]['deltaMemory'] is not None:
                w.write(method + ".memoryDeltaHeap\t")
                w.write(method + ".memoryDeltaMax\t")
                w.write(method + ".memoryDeltaFree\t")
                w.write(method + ".memoryDeltaUsed\t")  

        w.write("\n")      
        
        #headers created, now write data
        for transactionId in apiTransactionsIDs:
            transaction = allTransactions[transactionId]
            #print "transaction=", transaction

            w.write(transactionId+"\t") 
            
            if transaction['transactionProperty']['networkName'] is not None:
                w.write(transaction['transactionProperty']['networkName']+"\t")
            else:
                if len(clientNetworkNames) > 0:
                    w.write(clientNetworkNames.pop(0) +"\t")                      
                else:
                    w.write("" +"\t")  


            if transaction['transactionProperty']['fileSize'] is not None:
                w.write("{:,}".format(transaction['transactionProperty']['fileSize'])+"\t")
            else:
                if len(clientFileSizes) > 0:
                    fs = clientFileSizes.pop(0)
                    w.write(fs +"\t")                      
                else:
                    w.write("" +"\t")               
                        
            if transaction['transactionProperty']['UUID'] is not None: 
                w.write(transaction['transactionProperty']['UUID']+"\t")
            else:
                w.write("" +"\t")
            
            if transaction['transactionProperty']['clientRTT'] is not None: 
                 w.write(transaction['transactionProperty']['clientRTT'])
            else:
                if len(clientRTTs) > 0:
                    w.write(clientRTTs.pop(0))                 
                else:
                    w.write("") 
                 
            w.write("\t")
            w.write("{:,}".format(transaction['transactionProperty']['numberOfElements'])+"\t")
            
       
            if api in "NetworkAService.queryNetwork": 
                
                if len(clientNodes) > 0: 
                    w.write(clientNodes.pop(0)+"\t")
                else:
                    w.write("\t")
                    
                if len(clientEdges) > 0: 
                    w.write(clientEdges.pop(0)+"\t")
                else:
                    w.write("\t")   
                                                      
                if transaction['transactionProperty']['nodesRetrieved'] is not None: 
                    w.write("{:,}".format(transaction['transactionProperty']['nodesRetrieved'])+"\t")
                else:
                    w.write("\t")
                    
                if transaction['transactionProperty']['edgesRetrieved'] is not None: 
                    w.write("{:,}".format(transaction['transactionProperty']['edgesRetrieved'])+"\t")
                else:
                    w.write("\t") 
                                        
                if transaction['transactionProperty']['depth'] is not None: 
                    w.write("{:,}".format(transaction['transactionProperty']['depth'])+"\t")
                else:
                    w.write("\t")                    
          
            
            for method in apiMethods:
                try:
                    if transaction['transactionData'][method]['deltaTime'] is not None:
                        w.write(transaction['transactionData'][method]['deltaTime'] + "\t")
                    else:
                        w.write("\t")
                except KeyError:
                    w.write("\t")
                    continue
            
                if transaction['transactionData'][method]['deltaMemory'] is not None:
                    w.write(str(transaction['transactionData'][method]['deltaMemory']['heap']) + "\t")
                    w.write(str(transaction['transactionData'][method]['deltaMemory']['max']) + "\t")
                    w.write(str(transaction['transactionData'][method]['deltaMemory']['used']) + "\t")
                    w.write(str(transaction['transactionData'][method]['deltaMemory']['free']) + "\t")

            w.write('\n')
        
        w.write('\n')
        w.write('\n')



'''
    #headers created, now write data
    for transactionId in createNetworkIDs:
        transaction = allTransactions[transactionId]

        w.write(transactionId+"\t") 
        if transaction['transactionProperty']['networkName'] is not None:
            w.write(transaction['transactionProperty']['networkName']+"\t")
        else:
            w.write("" +"\t")
        if transaction['transactionProperty']['UUID'] is not None:  
            w.write(transaction['transactionProperty']['UUID']+"\t")
        else:
            w.write("" +"\t")
            
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

    
'''

''' --------------------------------------------------------------------------------------------------------  
if len(args) > 0:
    
    loopCounter = 0
    
    # fill in transactionProperty:clientRTT with the arguments received from the caller
    for transactionId in createNetworkIDs:
        transaction = allTransactions[transactionId]['transactionProperty']
        loopCounter += 1
        
        if loopCounter <= transactionsWithoutClientRTT:
            continue
        
        clientRTT = args.pop(0)
        allTransactions[transactionId]['transactionProperty']['clientRTT'] = clientRTT
-------------------------------------------------------------------------------------------------------- '''        

