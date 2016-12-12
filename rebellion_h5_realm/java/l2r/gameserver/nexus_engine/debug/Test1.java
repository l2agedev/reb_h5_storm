/**
 * 
 */
package l2r.gameserver.nexus_engine.debug;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author hNoke
 *
 */
public class Test1
{
	public Lock lock1 = new ReentrantLock();
	public Lock lock2 = new ReentrantLock();
	
	
	public void test()
	{
		Thread t1 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				zazpivat("T1: ");
			}
		});
		
		Thread t2 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				zazpivat("T2: ");
			}
		});
		
		t1.start();
		t2.start();
	}
	
	public void zazpivat(String s)
	{
		for(int i = 0; i < Integer.MAX_VALUE; i ++)
		{
			if(i == Integer.MAX_VALUE / 2)
				System.out.println(s + "JDEME TLESKAT!!");
		}
		
		zatleskat(s);
	}
	
	public synchronized void zatleskat(String s)
	{
		System.out.println(s + "TLESKAM!!");
	}
	
	public static void main(String[] args)
	{
		/*int countToSpawn = -1;
		boolean start = false;
		
		int playersCount = 36;
		int survivorsCount = 34;
		int mutantCount = 3;
		
		if(countToSpawn <= 0)
		{
			int mutants = 1;
			int players = 10;
			
			if(start)
			{
				countToSpawn = (int) Math.floor(((double)playersCount / (double)players) * (double)mutants);
				
				System.out.println(countToSpawn);
				
				if(countToSpawn < 1)
					countToSpawn = 1;
			}
			else
			{
				countToSpawn = (countToSpawn = (int) Math.floor(((double)playersCount / (double)players) * (double)mutants)) - mutantCount;
			}
		}
		
		System.out.println(countToSpawn);*/
		
		boolean result = false;
		
		int currentPlayers = 50;
		int currentMutants = 3;
		
		if(currentMutants == 0)
		{
			if(currentPlayers >= 3)
				result = true;
			else result = false; // at least 3 players are required (one of them will become mutant)
		}
		else if(currentMutants == 1)
		{
			if(currentPlayers >= 2)
				result = true;
			else result = false;
		}
		else
		{
			if(currentPlayers + currentMutants >= 3)
			{
				int mutants = 1;
				int players = 10;
				
				int countToHaveMutants = (int) Math.floor(((double)currentPlayers / (double)players) * (double)mutants);
				if(countToHaveMutants < 1)
					countToHaveMutants = 1;
				
				int toUntransform = 0;
				if(currentMutants > countToHaveMutants)
				{
					toUntransform = currentMutants - countToHaveMutants;
				}
				
				System.out.println("toUntransform = " + toUntransform);
				result = true;
			}
			else result = false;
		}
		
		System.out.println(result);
	}
}
