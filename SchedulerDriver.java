/*
Class to parse the input file and to run an instance
of the scheduler.

Usage is: java SchedulerDriver inputfilename
*/

import java.io.*;
import java.util.*;

public class SchedulerDriver
{
	public static void main(String[] args)
	{
		try
		{
			BufferedReader inputFile = new BufferedReader(new FileReader(args[0]));
			
			//Linked list containing raw process data
			LinkedList<Process> myProcess = new LinkedList<Process>();
			
			//parse each line in file
			String line;
			while((line = inputFile.readLine()) != null)
			{
				String[] token = line.split("\\s");
				if(token.length >= 5)
				{
					String name = token[0];
					int creationTime = Integer.parseInt(token[1]);
					int priority = Integer.parseInt(token[2]);
					int age = Integer.parseInt(token[3]);
					int cpuTime = Integer.parseInt(token[4]);
					Process currentProcess = new Process(name, creationTime, priority, age, cpuTime);
					myProcess.add(currentProcess);
				}
			}
			
			//create an instance of scheduler
			Scheduler myScheduler = new Scheduler(myProcess);
			
			//start running the scheduler
			myScheduler.run();
			
		}
		catch(ArrayIndexOutOfBoundsException ofbe)
		{
			System.out.println("No input filename specified.");
		}
		catch(FileNotFoundException fnfe)
		{
			System.out.println("Input file does not exist.");
		}
		catch(IOException ioe)
		{
			System.out.println("Error accessing the file.");
		}
		catch(NumberFormatException nfe)
		{
			System.out.println("File does not contain valid process data.");
		}
	}
}

