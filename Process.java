/*
An object to simulate a process.
Stores process related data such as name, creation time,
priority, age, cpu burst time, and run-time data such as
ready time, total waiting time, total running time, end time.
*/

public class Process
{
	
	//Process creation data from input file
	protected String name;
	protected int creationTime;
	protected int priority;
	protected int age;
	protected int cpuTime;
	
	//Run-time data
	protected int endTime;
	protected int readyTime;
	protected int runningTime;
	protected int lastRanTime;
	protected int waitingTime;
	protected int queue1RanTime;
	
	//Constructor
	public Process(String n, int cT, int p, int a, int cpuT)
	{
		name = n;
		creationTime = cT;
		priority = p;
		age = a;
		cpuTime = cpuT;
		
		endTime = -1;
		readyTime = -1;
		runningTime = 0;
		lastRanTime = -1;
		waitingTime = 0;
		queue1RanTime = 0;
	}
	
	//method to simulate a process running for 1 unit
	public void execute()
	{
		runningTime++;
	}
	
	//method to test if the process has finished
	public boolean isTerminated()
	{
		return cpuTime == runningTime;
	}
	
	//Method to print the process data when it is terminated
	public String toString()
	{
		//String s = String.format("  %-9s%-9s%-9s%-9s%-9s%-9s", name, creationTime, endTime, readyTime - creationTime, runningTime, waitingTime);
		String s = String.format("  %-9s%-9s%-9s%-9s%-9s%-9s", name, creationTime, endTime, readyTime, runningTime, waitingTime);
		//s += "\n--------------------------------------------------------";
		return s;
	}
}
