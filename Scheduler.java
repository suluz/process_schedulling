/*
An object to simulate the required process Scheduler.
It contains 2 queues, queue1 being the high priority
queue and queue2 being the low priority queue. Queue1
has absolute priority, and contains priority decay.
Queue2 has an aging mechanism.
*/

import java.util.*;

public class Scheduler
{
	
	//Linked list data structures to store processes
	//myProcess contains raw process data not in queue
	//queue1 and queue2 are the scheduler queues
	//finishedProcess contains terminated processes for stats
	private LinkedList<Process> myProcess;
	private LinkedList<Process> queue1;
	private LinkedList<Process> queue2;
	private LinkedList<Process> finishedProcess;
	
	//Run-time data variables to hold current running process,
	//scheduler running time and current process executed time
	private int currentTime;
	private Process currentProcess;
	private int currentUnitsExecuted;
	private String currentName = null;
	
	//Constants as specified by the assignment
	private final int priorityThreshold = 2;
	private final int queue1Quantum = 5;
	private final int queue2Quantum = 20;
	private final int ageThreshold = 8;
	private final int queue1Threshold = 25;
	
	//Constructor, takes in the raw process data from the input file
	//provided by the driver
	public Scheduler(LinkedList<Process> input)
	{
		myProcess = input;
		queue1 = new LinkedList<Process>();
		queue2 = new LinkedList<Process>();
		finishedProcess = new LinkedList<Process>();
		
		currentTime = 0;
		currentProcess = null;
		currentUnitsExecuted = 0;
	}
	
	//The heart of the Scheduler. 
	public void run()
	{
		//System.out.println("========================================================");
		System.out.println("  name     arrival    end      ready    running  waiting  ");
		//System.out.println("========================================================");
		
		//Before it starts running, it admits any processes created at time = 0.
		admitNewProcess();
		
		//Then methodically loop through these functions in order
		//The last 3 places processes back into the queues.
		//They are in the order of: priority decay in queue1 > new processes > aging priority increase
		do
		{
			//state ready -> state running
			dispatchReadyProcess();
			
			//running
			executeCurrentProcess();
			
			//state new -> state ready
			admitNewProcess();
			
			//state running -> state ready
			interruptRunningProcess();

			//state new -> state ready
			//admitNewProcess();
			
			//age queue2 processes
			//ageQueue2Process();
			if(currentProcess == null)
			{
				ageQueue2Process();
				currentName = null;
			}

			//display statement
			statePrint();
			
		//Keep looping until there are no more processes to run
		}while(!queue1.isEmpty() || !queue2.isEmpty() || currentProcess != null);
		
		//printStats();
	}
	
	//method to admit new processes to the queues
	//checks the raw process data to see if a process
	//creation time matches current scheduler time
	private void admitNewProcess()
	{
		Iterator<Process> iterator = myProcess.iterator();
		while(iterator.hasNext())
		{
			Process p = iterator.next();
			if(currentTime >= p.creationTime)
			{
				placeInQueue(p);
				iterator.remove();
			}
		}
	}
	
	//method to correctly place a process into the right queue
	//if priority is greater than threshold, insert by priority
	//ordering into queue1. If not, add to end of queue2
	private void placeInQueue(Process p)
	{
		if(p.priority > priorityThreshold)
		{
			int indexToPlace = 0;
			for(Process queueProcess : queue1)
			{
				if(queueProcess.priority >= p.priority)
				{
					indexToPlace++;
				}
			}
			queue1.add(indexToPlace, p);
		}else
		{
			queue2.add(p);
		}
	}
	
	//method to simulate the scheduler preempting and non-preempting
	//processes. In our case, the processes are preempt and non-preempt
	//when they terminate, when they ran their quantum, and when a
	//queue2 process is running and there is a process in queue1
	private void interruptRunningProcess()
	{
		if(currentProcess != null)
		{
			
			//Case of process terminated. Record end-time and print stats
			if(currentProcess.isTerminated())
			{
				currentProcess.endTime = currentTime;
				//test line:
				//System.out.println("\nTerminated:");
				//real output:
				System.out.println(currentProcess);
				finishedProcess.add(currentProcess);
				currentProcess = null;
				
			//Case of queue2 process running and a process is in queue1
			//Record last-ran-time and place it back into the right queue
			}
			else if(currentProcess.priority <= priorityThreshold && !queue1.isEmpty())
			{
				currentProcess.lastRanTime = currentTime;
				placeInQueue(currentProcess);
				currentName = currentProcess.name;
				//test the name of interrupted process in Queue 2
				//System.out.println("This is an interrupted process in Q2:" + currentName);
				currentProcess = null;
				
			//Case of queue1 process finishing their quantum
			//Record last-ran-time and queue1-ran-time
			//If queue1-ran-time is greater equal to queue1Threshold, decrease its priority
			//Put the process back into the queue using its new or unchanged priority
			}
			else if(currentUnitsExecuted == queue1Quantum && currentProcess.priority > priorityThreshold)
			{
				currentProcess.lastRanTime = currentTime;
				
				currentProcess.queue1RanTime += queue1Quantum;
				if(currentProcess.queue1RanTime >= queue1Threshold)
				{
					currentProcess.priority--;
					currentProcess.queue1RanTime = 0;
				}
				
				placeInQueue(currentProcess);
				currentName = currentProcess.name;
				currentProcess = null;
				
			//Case of queue2 process finishing their quantum
			//Record last-ran-time and put it back into the right queue
			}
			else if(currentUnitsExecuted == queue2Quantum && currentProcess.priority <= priorityThreshold)
			{
				currentProcess.lastRanTime = currentTime;
				placeInQueue(currentProcess);
				currentName = currentProcess.name;
				//test the name of interrupted process in Queue 2
				//System.out.println("This is an interrupted process in Q2:" + currentName);
				currentProcess = null;
			}
		}
	}
	
	//Method to simulate the scheduler dispatch
	//If there are no running processes, take a process from queue1
	//if it exists. If not, take it from queue2.
	private void dispatchReadyProcess()
	{
		boolean newDispatched = false;
		
		//Checks queue1 first
		if(currentProcess == null && !queue1.isEmpty())
		{
			currentProcess = queue1.poll();
			newDispatched = true;
		}
		//Checks queue2 if none in queue1
		else if(currentProcess == null && !queue2.isEmpty())
		{
			currentProcess = queue2.poll();
			currentProcess.age = 0;
			newDispatched = true;
		}
		
		//If process is dispatched and it is the first time, record the ready time
		//If process has ran before, sum up the extra waiting time using last-ran-time
		if(newDispatched)
		{
			if(currentProcess.readyTime == -1)
			{
				currentProcess.readyTime = currentTime;
			}
			if(currentProcess.lastRanTime != -1)
			{
				currentProcess.waitingTime += currentTime - currentProcess.lastRanTime;
			}
			//System.out.println(currentProcess.waitingTime);
			currentUnitsExecuted = 0;
		}
	}
	
	//Method to simulate giving a process cpu time
	private void executeCurrentProcess()
	{
		if(currentProcess != null)
		{
			currentProcess.execute();
			currentTime = currentTime + 1;
			currentUnitsExecuted = currentUnitsExecuted + 1;
		}
	}
	
	//Method to simulate the aging mechanism
	//Increases the age of all processes in queue2 as processes are ran.
	//If the age is past a threshold, increase its priority.
	//If the new priority is over the priority threshold, place it in queue1
	private void ageQueue2Process()
	{
		Iterator<Process> iterator = queue2.iterator();
		while(iterator.hasNext())
		{
			Process p = iterator.next();
			//test the name of Q2 process in Q2
			/*
			if(p.name == currentName)
			{
				System.out.println("Find it!" + p.name);
			}
			*/
			if(p.name != currentName && currentTime != p.creationTime)
			{
				p.age++;
			}
			
			if(p.age >= ageThreshold)
			{
				p.priority++;
				p.age = 0;
			}
			
			if(p.priority > priorityThreshold)
			{
				placeInQueue(p);
				iterator.remove();
			}
		}
	}
/*
	private void ageQueue2()
	{
		if(currentProcess.isTerminated())
		{
			ageQueue2Process();
		}
		if(!currentProcess.isTerminated() && !queue1.isEmpty() && currentUnitsExecuted == queue1Quantum)
		{
			ageQueue2Process();
		}
		if(!currentProcess.isTerminated() && queue1.isEmpty() && currentUnitsExecuted == queue2Quantum)
		{
			ageQueue2Process();
		}
	}
*/	
	//Method to print out the the max and average Turn Around Times and Waiting times.
	private void printStats()
	{
		int maxTurnAround = Integer.MIN_VALUE;
		int totalTurnAround = 0;
		
		int maxWaiting = Integer.MIN_VALUE;
		int totalWaiting = 0;
		
		int taskCompleted = finishedProcess.size();
		
		for(Process p : finishedProcess)
		{
			//turnaroundtime = end-time - creation-time
			int turnAround = p.endTime - p.creationTime;
			if(turnAround > maxTurnAround)
			{
				maxTurnAround = turnAround;
			}
			totalTurnAround += turnAround;
			
			//waitingtime = the time from creation to its 1st run + waiting time
			int waiting = p.readyTime - p.creationTime + p.waitingTime;
			if(waiting > maxWaiting)
			{
				maxWaiting = waiting;
			}
			totalWaiting += waiting;
		}
		
		double avgTurnAround = (double)totalTurnAround / (double)taskCompleted;
		double avgWaiting = (double)totalWaiting / (double)taskCompleted;
		
		System.out.println("\nTaskcompleted = " + taskCompleted);
		System.out.println("Maxturnaroundtime = " + maxTurnAround);
		System.out.println("Averageturnaroundtime = " + avgTurnAround);
		System.out.println("Maxwaitingtime = " + maxWaiting);
		System.out.println("Averagewaitingtime = " + avgWaiting);
	}
	
	//print the output by steps
	private void statePrint()
	{
		if(currentProcess == null)
		{
			Iterator<Process> iterator1 = queue1.iterator();
			Iterator<Process> iterator2 = queue2.iterator();
			System.out.println("Time(" + currentTime + ")");
			System.out.println("This is Queue 1:");
			System.out.println("  name     arrival    end      ready    running  waiting	age	priority");
			while(iterator1.hasNext())
			{
				Process p1 = iterator1.next();
				System.out.println(p1 + " " + p1.age + "  " + p1.priority);
			}
			System.out.println("This is Queue 2:");
			System.out.println("  name     arrival    end      ready    running  waiting	age	priority");
			while(iterator2.hasNext())
			{
				Process p2 = iterator2.next();
				System.out.println(p2 + " " + p2.age + "  " + p2.priority);
			}
			System.out.println("\n");
		}
	}
}
