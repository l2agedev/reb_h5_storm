/*
 * Copyright (C) 2014-2015 Vote Rewarding System
 * 
 * This file is part of Vote Rewarding System.
 * 
 * Vote Rewarding System is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Vote Rewarding System is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.randoms.votingengine;

import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.randoms.votingengine.VotingSettings.MessageType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author UnAfraid
 */
public class VotingRewardAPI implements IVoicedCommandHandler, Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(VotingRewardAPI.class);
	
	private static ScheduledFuture<?> _task = null;
	private static final String[] COMMANDS =
	{
		VotingSettings.getInstance().getVotingCommand(),
	};
	
	private final Map<Integer, Player> _tasks = new ConcurrentHashMap<>();
	
	protected VotingRewardAPI()
	{
		stopTask();
		VotingSettings.getInstance();
		
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
		
		if(!VotingSettings.getInstance().isenabled())
		{
			_log.info("VoteReward: Is currently disabled!");
			return;
		}
		
		VotingRewardCache.getInstance().load();
		
		_task = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
		_log.info("VoteReward: Has been successfully loaded!");
	}
	
	private void reloadApi()
	{
		stopTask();
		
		VotingSettings.getInstance();
		
		if(!VotingSettings.getInstance().isenabled())
		{
			_log.info("VoteReward: Vote reward has been disabled...");
			return;
		}
		
		VotingRewardCache.getInstance().load();
		
		_task = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
		_log.info("VoteReward: Has been successfully reloaded...");
	}
	
	
	public static void stopTask()
	{
		if (_task != null)
		{
			_task.cancel(true);
			_task = null;
		}
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String params)
	{
		if (player == null)
			return false;
		
		if (player.isGM() && "reload".equals(params))
		{
			reloadApi();
			player.sendMessage("Reloaded vote reward system...");
			return true;
		}
		
		if(!VotingSettings.getInstance().isenabled())
		{
			player.sendMessage("Vote reward is not enabled at this time!");
			return false;
		}
		
		final long timeRemaining = VotingRewardCache.getInstance().getLastVotedTime(player);
		
		// Make sure player haven't received reward already!
		if (timeRemaining > 0)
		{
			sendReEnterMessage(timeRemaining, player);
			return false;
		}
		
		// Add rewarding task
		if (_tasks.putIfAbsent(player.getObjectId(), player) != null)
		{
			player.sendMessage("You already requested reward, please wait..");
			return false;
		}
		player.sendMessage("You're rewarding request has been enqueued, verifying your vote please wait..");
		return true;
	}
	
	@Override
	public void run()
	{
		if (_tasks.isEmpty())
			return;
		
		for (Player player : _tasks.values())
		{
			if (player == null)
				continue;
			
			try
			{
				new VotingRewardTask(player);
			}
			catch (Exception e)
			{
				player.sendMessage("Failed to deliver rewards!");
				_log.error("Failed to deliver rewards", e);
			}
			finally
			{
				_tasks.remove(player.getObjectId());
			}
		}
	}
	
	public static void sendReEnterMessage(long time, Player player)
	{
		if (time > System.currentTimeMillis())
		{
			final long remainingTime = (time - System.currentTimeMillis()) / 1000;
			final int hours = (int) (remainingTime / 3600);
			final int minutes = (int) ((remainingTime % 3600) / 60);
			final int seconds = (int) ((remainingTime % 3600) % 60);
			
			String msg = VotingSettings.getInstance().getMessage(MessageType.ON_REUSE);
			if (msg != null)
			{
				msg = msg.replaceAll("%hours%", Integer.toString(hours));
				msg = msg.replaceAll("%mins%", Integer.toString(minutes));
				msg = msg.replaceAll("%secs%", Integer.toString(seconds));
				player.sendMessage(msg);
			}
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
	
	public static final VotingRewardAPI getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final VotingRewardAPI INSTANCE = new VotingRewardAPI();
	}
}