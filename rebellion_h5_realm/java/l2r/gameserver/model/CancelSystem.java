/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model;

import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Skill.SkillType;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.stats.Env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

/**
 * 
 * @author Nik, horato
 * 
 */

public class CancelSystem
{
	protected static final Logger _log = Logger.getLogger(CancelSystem.class.getName());
	private final Map<Integer, BuffRestore> _playerBuffRestore;
	@SuppressWarnings("unused")
	private final ScheduledFuture<?> _thread;
	
	private CancelSystem()
	{
		_playerBuffRestore = new ConcurrentHashMap<Integer, BuffRestore>();
		_thread = ThreadPoolManager.getInstance().scheduleAtFixedRate(new BuffRestoreImpl(), 5000, 5000);
	}

	public void onCancel(Creature cha, Effect eff)
	{
		if (Config.CANCEL_SYSTEM_RESTORE_DELAY <= 0)
			return;
		
		if (cha == null)
			return;
		
		if (eff.getSkill().getSkillType() != SkillType.BUFF)
			return;
		
		// Check if player/summon is in events or oly.
		if (cha.getPlayer() != null)
		{
			if (NexusEvents.isInEvent(cha.getPlayer()) || cha.getPlayer().isInOlympiadMode() || Olympiad.isRegistered(cha.getPlayer()) || Olympiad.isRegisteredInComp(cha.getPlayer()))
				return;
		}
		
		if (_playerBuffRestore.containsKey(cha.getObjectId()))
			_playerBuffRestore.get(cha.getObjectId()).addEffect(eff);
		else
			_playerBuffRestore.put(cha.getObjectId(), new BuffRestore(cha.getObjectId(), eff));
		
		//cha.sendMessage("An effect has been cancelled and it will be restored in 1 minute. If you get cancelled again before the time expires, it will be reset to 1 minute again.");
		//TODO: Does not support strings ! string already available:
		//cha.sendMessage(new CustomMessage("l2r.gameserver.model.CancelSystem.message1", cha));
	}
	
	private class BuffRestoreImpl implements Runnable
	{
		@Override
		public void run()
		{
			List<Integer> restored = new ArrayList<Integer>();
			
			for (Entry<Integer, BuffRestore> n3 : _playerBuffRestore.entrySet())
			{
				int owenerObjId = n3.getKey();
				BuffRestore restore = n3.getValue();
				
				if (restore.tryRestore())
					restored.add(owenerObjId);
			}
			
			for (Integer objId : restored)
				_playerBuffRestore.remove(objId);
		}
		
	}
	
	private class BuffRestore
	{
		private final int _ownerObjId;
		private final Map<Skill, Integer[]> _cancelledEffects;
		private long _startTime;
		private long _restoreTime;
		
		protected BuffRestore(int $ownerObjId, Effect ... effects)
		{
			_ownerObjId = $ownerObjId;
			_cancelledEffects = new HashMap<>();
			for (Effect effect : effects)
				addEffect(effect);
		}
		
		protected boolean tryRestore()
		{
			if (_restoreTime <= System.currentTimeMillis())
			{
				restoreBuffs();
				return true;
			}
			
			return false;
		}
		
		protected void addEffect(Effect eff)
		{
			// If this is the 1st canceled effect, begin the start time counter.
			if (_cancelledEffects.isEmpty())
				_startTime = System.currentTimeMillis();
			
			int classId = (eff.getEffected() != null && eff.getEffected().isPlayer()) ? eff.getEffected().getPlayer().getClassId().getId() : -1;
			_cancelledEffects.put(eff.getSkill(), new Integer[]{eff.getTimeLeft(), classId});
			_restoreTime = System.currentTimeMillis() + Config.CANCEL_SYSTEM_RESTORE_DELAY * 1000;
		}
		
		private void restoreBuffs()
		{
			if (_cancelledEffects.isEmpty())
				return;
			
			GameObject obj = GameObjectsStorage.findObject(_ownerObjId);
			Creature owner = null;
			if (obj != null && obj.isCreature())
				owner = (Creature) obj;
			
			if (owner == null)
				return;
			
			// Check if player/summon is in events or oly.
			if (owner.getPlayer() != null)
			{
				if (NexusEvents.isInEvent(owner.getPlayer()) || owner.getPlayer().isInOlympiadMode() || Olympiad.isRegistered(owner.getPlayer()) || Olympiad.isRegisteredInComp(owner.getPlayer()))
				{
					_cancelledEffects.clear();
					return;
				}
			}

			for (Entry<Skill, Integer[]> n3 : _cancelledEffects.entrySet())
			{
				Skill skill = n3.getKey();
				long duration = n3.getValue()[0] * 1000;
				int classId = n3.getValue()[1];
				
				if (Config.CANCEL_SYSTEM_KEEP_TICKING)
				{
					long timeDiff = _restoreTime - _startTime;
					duration -= timeDiff;
					if (duration < 1000) // Do not restore buffs less than 1sec
						continue;
				}
				
				if (classId > 0 && owner.isPlayer() && owner.getPlayer().getClassId().getId() != classId)
					continue;
				
				// If owner already has the buffs from this effect, do not return the buff.
				if (owner.getEffectList().getEffectsBySkill(skill) != null)
					continue;
				
				for (EffectTemplate et : skill.getEffectTemplates())
				{
					Env env = new Env(owner, owner, skill);
					Effect effect = et.getEffect(env);
					effect.setPeriod(duration);
					owner.getEffectList().addEffect(effect);
					owner.sendChanges();
					owner.updateEffectIcons();
				}
			}
			
			owner.sendMessage("Your cancelled buffs have been restored.");
			//TODO: Does not support strings! String already available:
			//owner.sendMessage(new CustomMessage("l2r.gameserver.model.CancelSystem.message2", owner));
			
			_cancelledEffects.clear();
		}
	}
	
	public static final CancelSystem getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CancelSystem _instance = new CancelSystem();
	}
	
}
