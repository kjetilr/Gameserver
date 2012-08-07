 #!/bin/bash

 DURATION=300			# Duration of test (in seconds)
 SERVER_PORT=9060		# Server port to use for experiment
 THREADS=48			# Number of threads for server
 SINGLETHREADED="false"		# Run singlethreaded version of server
 LOAD_INTENSITY=10000		# Synthetic load intesity
 LOAD_VECTOR_SIZE=1000000	# Size of synthetic load array
 INSTANCES=5			# Number of instances for each client playback
 DELAY=200			# Delay between start of each replay instance
 INCREMENT=20			# Number of increments between each test step
 LOG_NUM_CLI=1			# Number of clients in one log
 MAX_CLIENTS=800		# Maximum number of clients
 COUNTER=0
 NUM_SELECTORS=8         # Number of selectors on server.
 SERVER="jefferson.ndlab.net"
 CLIENT1="washington.ndlab.net"
 CLIENT2="bush.ndlab.net"
 CLIENT3="clinton.ndlab.net"
 #CLIENT4="reagan.ndlab.net"
 
 # Coordination-script to run a batch of tests for the gameserver project
 # To be run from washington.ndlab.net

 while [  $COUNTER -lt  $MAX_CLIENTS ];
 do    
     
 # Increment counter
     COUNTER=`echo "${COUNTER} + ${INCREMENT}" | bc`
     
 # Perform run for both single-threaded and multi-threaded version
     for i in `seq 1 2`;
     do
	 if [ $i -eq 1 ]
	 then
	     SINGLETHREADED="false"
	 else
	     SINGLETHREADED="true"
	 fi       
	 
	 echo "COUNTER: $COUNTER"
	 INSTANCES=`echo "$COUNTER / $LOG_NUM_CLI" | bc`
	 echo "INSTANCES: $INSTANCES"
	 NUM_INS=`echo "$INSTANCES / 3" | bc`
	 echo "Instances per machine: $NUM_INS"
	 
 # Set server options for this run
	 SERVER_OPTS="$SERVER_PORT $THREADS $SINGLETHREADED $LOAD_INTENSITY $LOAD_VECTOR_SIZE $NUM_SELECTORS $DURATION"
	 echo "SERVER_OPTS: $SERVER_OPTS"
 # Set client options for this run
	 CLIENT_OPTS="$NUM_INS $DELAY $SERVER_PORT $THREADS $NUM_SELECTORS $DURATION"
	 echo "CLIENT_OPTS: $CLIENT_OPTS"
	 
 # 1) Run server at jefferson. Capture pid
	 echo "Starting server on jefferson"
	 ssh tests@${SERVER} /work/tests/gameserver/scripts/run_server.sh $SERVER_OPTS
	 
	 if [ $INSTANCES -ge 1 ]
	 then
 # 2) Run n clients at each client computer
 # Locally, no need for ssh
 # washington
	     echo "Starting clients on washington"
	     bash /home/tests/gameserver/scripts/run_clients.sh $CLIENT_OPTS
	     INSTANCES=`echo "$INSTANCES - 1" | bc`
	 fi
	 
	 if [ $INSTANCES -ge 1 ]
	 then
 #bush
	     echo "Starting clients on bush"
	     ssh tests@${CLIENT2} /home/tests/gameserver/scripts/run_clients.sh $CLIENT_OPTS
	     INSTANCES=`echo "$INSTANCES - 1" | bc`
	 fi
	 
	 if [ $INSTANCES -ge 1 ]
	 then
 #clinton
	     echo "Starting clients on clinton"
	     ssh tests@${CLIENT3} /home/tests/gameserver/scripts/run_clients.sh $CLIENT_OPTS
	     INSTANCES=`echo "$INSTANCES - 1" | bc`
	 fi
	 
#	 if [ $INSTANCES -ge 1 ]
#	 then
 #reagan
#	     echo "Starting clients on reagan"
#	     ssh tests@${CLIENT4} /home/tests/gameserver/scripts/run_clients.sh $CLIENT_OPTS
#	     INSTANCES=`echo "$INSTANCES - 1" | bc`
#	 fi
	 
 ## Sleep for duration of test
	 echo "Waiting $DURATION seconds for test to finish"
	 sleep $DURATION
	 
 # 3) Kill server and any lingering client processes
 # Server: jefferson
	 echo "Killing server on jefferson"
	 PIDS=`ssh tests@${SERVER} ps -eo pid,user,comm,args | grep NioServer | grep tests | grep -v grep | grep -v screen | awk '{ print $1 }'`
	 
	 echo "PIDS: ${PIDS}"
	 for i in $PIDS; do
	 	ssh tests@${SERVER} kill -9 $i;
	 done
 
# Clients: 
	 PIDSTRING="ps -eo pid,user,comm,args | grep PlaybackClient | grep tests | grep -v grep | grep -v screen | awk '{ print \$1 }'"	 
 # washington
	 echo "Killing client(s) on washington"
	 PIDS=`ssh tests@${CLIENT1} $PIDSTRING`
	 echo "PIDS: ${PIDS}"
	 for i in $PIDS; do 	
	 	ssh tests@${CLIENT1} kill -9 $i;
	 done 
	 
 # bush
	 echo "Killing client(s) on bush"
	 PIDS=`ssh tests@${CLIENT2} $PIDSTRING`
	 echo "PIDS: ${PIDS}"
	 for i in $PIDS; do	
	 	ssh tests@${CLIENT2} kill -9 $i;
	 done 
	 
 # clinton
	 echo "Killing client(s) on clinton"
	 PIDS=`ssh tests@${CLIENT3} $PIDSTRING`
	 echo "PIDS: ${PIDS}"
	 for i in $PIDS; do
	 	ssh tests@${CLIENT3} kill -9 $i;
	 done
	 
 # reagan
#	 echo "Killing client(s) on reagan"
#	 PIDS=`ssh tests@${CLIENT4} $PIDSTRING`
#	 echo "PIDS: ${PIDS}"
# 	 for i in $PIDS; do
#	 	ssh tests@${CLIENT4} kill -9 $i;
# 	 done
	 
 # 3) After finished test, rename dumps and copy them to a safe location
	 echo "Copying delay trace to storage"
	 ssh tests@${SERVER} mv /work/tests/gameserver/src/out/delay_statistics.txt /work/tests/traces/delay-${COUNTER}-clients-st-${SINGLETHREADED}.txt
	 
	 echo "Copying cpu trace to storage"
	 ssh tests@${SERVER} mv /work/tests/gameserver/src/out/cpu_statistics.txt /work/tests/traces/cpu-${COUNTER}-clients-st-${SINGLETHREADED}.txt

	 echo "Copying droped info trace to storage"
	 ssh tests@${SERVER} mv /work/tests/gameserver/src/out/timeouts_statistics.txt  /work/tests/traces/drops-${COUNTER}-clients-st-${SINGLETHREADED}.txt

	 sleep 30
	 
     done # Singlethreaded for
     
 done # Number of instances while
 
 echo "Done!"
 
