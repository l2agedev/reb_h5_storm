package l2r.gameserver.handler.admincommands.impl;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.InvisibleType;
import l2r.gameserver.network.serverpackets.Earthquake;
import l2r.gameserver.network.serverpackets.ExRedSky;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.PlaySound.Type;
import l2r.gameserver.network.serverpackets.SSQInfo;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.SunRise;
import l2r.gameserver.network.serverpackets.SunSet;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Util;

import java.util.Collections;
import java.util.List;


public class AdminEffects implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_invis,
		admin_vis,
		admin_offline_vis,
		admin_offline_invis,
		admin_earthquake,
		admin_block,
		admin_unblock,
		admin_changename,
		admin_gmspeed,
		admin_invul,
		admin_setinvul,
		admin_getinvul,
		admin_social,
		admin_abnormal,
		admin_transform,
        admin_callskill,
		admin_showmovie,
		admin_para_all,
		admin_unpara_all,
		admin_para,
		admin_unpara,
		admin_effect,
		admin_bighead,
		admin_shrinkhead,
		admin_social_menu,
		admin_effect_menu,
		admin_abnormal_menu,
		admin_play_sounds,
		admin_play_sound,
		admin_atmosphere,
		admin_set_displayeffect
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		int val,id,lvl;
		AbnormalEffect ae = AbnormalEffect.NULL;
		GameObject target = activeChar.getTarget();

		switch(command)
		{
			case admin_invis:
			case admin_vis:
				if(activeChar.isInvisible())
				{
					activeChar.setInvisibleType(InvisibleType.NONE);
					activeChar.broadcastCharInfo();
					if(activeChar.getPet() != null)
						activeChar.getPet().broadcastCharInfo();
				}
				else
				{
					activeChar.setInvisibleType(InvisibleType.NORMAL);
					activeChar.broadcastCharInfo(); // Broadcast so GMs can see hidden.
					World.removeObjectFromPlayers(activeChar);
				}
				break;
			case admin_gmspeed:
				if(wordList.length < 2)
					val = 0;
				else
					try
				{
						val = Integer.parseInt(wordList[1]);
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message1", activeChar));
					return false;
				}
				List<Effect> superhaste = activeChar.getEffectList().getEffectsBySkillId(7029);
				int sh_level = superhaste == null ? 0 : superhaste.isEmpty() ? 0 : superhaste.get(0).getSkill().getLevel();

				if(val == 0)
				{
					if(sh_level != 0)
						activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true); //снимаем еффект
					activeChar.unsetVar("gm_gmspeed");
				}
				else if(val >= 1 && val <= 4)
				{
					if(Config.SAVE_GM_EFFECTS)
						activeChar.setVar("gm_gmspeed", String.valueOf(val), -1);
					if(val != sh_level)
					{
						if(sh_level != 0)
							activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true); //снимаем еффект
						activeChar.doCast(SkillTable.getInstance().getInfo(7029, val), activeChar, true);
					}
				}
				else
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message2", activeChar));
				break;
			case admin_invul:
				handleInvul(activeChar, activeChar);
				if(activeChar.isInvul())
				{
					if(Config.SAVE_GM_EFFECTS)
						activeChar.setVar("gm_invul", "true", -1);
				}
				else
					activeChar.unsetVar("gm_invul");
				break;
		}
		
		if (!activeChar.isGM())
			return false;
		
		switch(command)
		{
			case admin_offline_vis:
				for(Player player : GameObjectsStorage.getAllPlayers())
					if(player != null && player.isInOfflineMode())
					{
						player.setInvisibleType(InvisibleType.NONE);
						player.decayMe();
						player.spawnMe();
					}
				break;
			case admin_offline_invis:
				for(Player player : GameObjectsStorage.getAllPlayers())
					if(player != null && player.isInOfflineMode())
					{
						player.setInvisibleType(InvisibleType.NORMAL);
						player.decayMe();
					}
				break;
			case admin_earthquake:
				try
				{
					int intensity = Integer.parseInt(wordList[1]);
					int duration = Integer.parseInt(wordList[2]);
					activeChar.broadcastPacket(new Earthquake(activeChar.getLoc(), intensity, duration));
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message3", activeChar));
					return false;
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_block:
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				if(((Creature) target).isBlocked())
					return false;
				((Creature) target).abortAttack(true, false);
				((Creature) target).abortCast(true, false);
				((Creature) target).block();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message4", activeChar));
				break;
			case admin_unblock:
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				if(!((Creature) target).isBlocked())
					return false;
				((Creature) target).unblock();
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message5", activeChar));
				break;
			case admin_changename:
				if(wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message6", activeChar));
					return false;
				}
				if(target == null)
					target = activeChar;
				if(!target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				String oldName = ((Creature) target).getName();
				String newName = Util.joinStrings(" ", wordList, 1);

				((Creature) target).setName(newName);
				((Creature) target).broadcastCharInfo();

				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message7", activeChar, oldName, newName));
				break;
			case admin_setinvul:
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				handleInvul(activeChar, (Player) target);
				break;
			case admin_getinvul:
				if(target != null && target.isCreature())
					if (!((Creature) target).isInvul()) {
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message8", activeChar, target.getName(), target.getObjectId()));
					} else {
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message9", activeChar, target.getName(), target.getObjectId()));
					}
				break;
			case admin_social_menu:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/social.htm"));
				break;
			case admin_social:
				try
				{
					if(wordList.length < 2)
						val = Rnd.get(1, 7);
					else
						val = Integer.parseInt(wordList[1]);
					
					int radius = 0;
					if (wordList.length > 2)
						radius = Integer.parseInt(wordList[2]);
					
					if (radius > 0)
					{
						for (Creature trgt : World.getAroundCharacters(activeChar, radius, 500))
							trgt.broadcastPacket(new SocialAction(trgt.getObjectId(), val));
					}
					else
					{
						if(target == null || target == activeChar)
							activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), val));
						else if(target.isCreature())
							((Creature) target).broadcastPacket(new SocialAction(target.getObjectId(), val));
					}
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message10", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/social.htm"));
				return true;
			case admin_abnormal_menu:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/abnormal.htm"));
				break;
			case admin_abnormal:
				try
				{
					int radius = 0;
					if(wordList.length > 1)
						ae = AbnormalEffect.getByName(wordList[1]);
					if(wordList.length > 2)
						radius = Integer.parseInt(wordList[2]);
					
					if (radius > 0)
					{
						for (Creature trgt : World.getAroundCharacters(activeChar, radius, 500))
						{
							if(ae == AbnormalEffect.NULL)
							{
								trgt.startAbnormalEffect(AbnormalEffect.NULL);
								//TODO: Does not support custom messages, string already available: l2r.gameserver.handler.admincommands.impl.admineffects.message11
								trgt.sendMessage(activeChar.isLangRus() ? "Аномальные эффекты очищается администратора." : "Abnormal effects clearned by admin.");
								//trgt.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message11", trgt));
								if(trgt != activeChar)
									//TODO: Does not support custom messages, string already available: l2r.gameserver.handler.admincommands.impl.admineffects.message12
									trgt.sendMessage(activeChar.isLangRus() ? "Аномальные эффекты очищается." : "Abnormal effects clearned.");
									//trgt.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message12", trgt));
							}
							else
							{
								trgt.startAbnormalEffect(ae);
								//TODO: Does not support custom messages, string already available: l2r.gameserver.handler.admincommands.impl.admineffects.message13
								trgt.sendMessage(activeChar.isLangRus() ? "Администратор добавил ненормальные действия: " : "Admin added abnormal effect: " + ae.getName());
								//trgt.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message13", trgt, ae.getName()));
								if(trgt != activeChar)
									//TODO: Does not support custom messages, string already available: l2r.gameserver.handler.admincommands.impl.admineffects.message14
									trgt.sendMessage(activeChar.isLangRus() ? "Добавлено ненормальные действия: " : "Added abnormal effect: " + ae.getName());
									//trgt.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message14", trgt, ae.getName()));
							}
						}
					}
					else
					{
						Creature effectTarget = target == null ? activeChar : (Creature) target;
						if(ae == AbnormalEffect.NULL)
						{
							effectTarget.startAbnormalEffect(AbnormalEffect.NULL);
							//TODO: Does not support custom messages, string already available: l2r.gameserver.handler.admincommands.impl.admineffects.message15
							effectTarget.sendMessage(activeChar.isLangRus() ? "Аномальные эффекты очищается администратора." : "Abnormal effects clearned by admin.");
							//effectTarget.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message15", effectTarget));
							if(effectTarget != activeChar)
								//TODO: Does not support custom messages, string already available: l2r.gameserver.handler.admincommands.impl.admineffects.message16
								effectTarget.sendMessage(activeChar.isLangRus() ? "Аномальные эффекты очищается." : "Abnormal effects clearned.");
								//effectTarget.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message16", effectTarget));
						}
						else
						{
							effectTarget.startAbnormalEffect(ae);
							//TODO: Does not support custom messages, string already available: l2r.gameserver.handler.admincommands.impl.admineffects.message17
							effectTarget.sendMessage(activeChar.isLangRus() ? "Администратор добавил ненормальные действия: " : "Admin added abnormal effect: " + ae.getName());
							//effectTarget.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message17", effectTarget, ae.getName()));
							if(effectTarget != activeChar)
								//TODO: Does not support custom messages, string already available: l2r.gameserver.handler.admincommands.impl.admineffects.message18
								effectTarget.sendMessage(activeChar.isLangRus() ? "Добавлено ненормальные действия: " : "Added abnormal effect: " + ae.getName());
								//effectTarget.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message18", effectTarget, ae.getName()));
						}
					}
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message19", activeChar));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message20", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/abnormal.htm"));
				return true;
			case admin_transform:
				try
				{
					if(wordList.length > 2)
						handleTransform(activeChar, Integer.parseInt(wordList[1]), Integer.parseInt(wordList[2]));
					else if (wordList.length > 1)
						handleTransform(activeChar, Integer.parseInt(wordList[1]));
					else if (activeChar.getTarget() != null)
						activeChar.getTarget().getPlayer().setTransformation(0);
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message21", activeChar));
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/transform.htm"));
					return false;
				}
				
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/transform.htm"));
				return true;
            case admin_callskill:
                try
				{
					id = Integer.parseInt(wordList[1]);
                    lvl = Integer.parseInt(wordList[2]);
                    @SuppressWarnings("unused")
					List<Effect> trasform = activeChar.getEffectList().getEffectsBySkillId(id);
				}
				catch(Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message22", activeChar));
					return false;
				}
                activeChar.doCast(SkillTable.getInstance().getInfo(id, lvl), (Creature) ((activeChar.getTarget() == null || !activeChar.getTarget().isCreature()) ? activeChar : activeChar.getTarget()), true);
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_showmovie:
				if (wordList.length < 2)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message23", activeChar));
					return false;
				}
				try
				{
					id = Integer.parseInt(wordList[1]);
				}
				catch (NumberFormatException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message24", activeChar));
					return false;
				}
				activeChar.showQuestMovie(id);
				break;
			case admin_para_all:
				try
				{
					id = Integer.parseInt(wordList[1]);
					for (Creature plr : activeChar.getAroundCharacters(id, 1000))
					{
						if (plr != null && plr.isPlayer() && !plr.isParalyzed())
						{
							plr.startParalyzed();
							plr.startAbnormalEffect(AbnormalEffect.HOLD_2);
							plr.abortAttack(true, true);
							plr.abortCast(true, true);
							//TODO: Does not support custom messages, string already available: l2r.gameserver.handler.admincommands.impl.admineffects.message25
							plr.sendMessage("You have been paralyzed by a GM.");
							// plr.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message25", plr));
						}
					}
					
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message26", activeChar, id));
				}
				catch (NumberFormatException | ArrayIndexOutOfBoundsException aiooe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message27", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_unpara_all:
				try
				{
					id = Integer.parseInt(wordList[1]);
					for (Creature plr : activeChar.getAroundCharacters(id, 1000))
					{
						if (plr != null && plr.isPlayer() && plr.isParalyzed())
						{
							plr.stopParalyzed();
							plr.stopAbnormalEffect(AbnormalEffect.HOLD_2);
							//TODO: Does not support custom messages. String already exists: l2r.gameserver.handler.admincommands.impl.admineffects.message28
							plr.sendMessage("Paralyze has been realeased.");
							//plr.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message28", plr));
						}
					}
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message29", activeChar, id));
				}
				catch (NumberFormatException | ArrayIndexOutOfBoundsException aiooe)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message30", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_para:
				Creature tgt = (Creature) (target.isCreature() ? target : null);
				tgt.startParalyzed();
				tgt.startAbnormalEffect(AbnormalEffect.HOLD_2);
				tgt.abortAttack(true, true);
				tgt.abortCast(true, true);
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_unpara:
				Creature tgt2 = (Creature) (target.isCreature() ? target : null);
				tgt2.stopAbnormalEffect(AbnormalEffect.HOLD_2);
				tgt2.stopParalyzed();
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_effect_menu:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
			case admin_effect:
				try
				{
					GameObject obj = activeChar.getTarget();
					if (obj == null)
						obj = activeChar;
					if (!obj.isCreature())
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message31", activeChar));
					else
					{
						int level = 1, hittime = 1;
						int skill = Integer.parseInt(wordList[1]);
						if (wordList.length > 2)
							level = Integer.parseInt(wordList[2]);
						if (wordList.length > 3)
							hittime = Integer.parseInt(wordList[3]);
						
						((Creature) obj).broadcastPacket(new MagicSkillUse((Creature) target, activeChar, skill, level, hittime, 0));
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message32", activeChar, obj.getName(), skill, level));
					}
					
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message33", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_bighead:
				if (activeChar.getTarget() != null && activeChar.getTarget().isCreature())
					((Creature) activeChar.getTarget()).startAbnormalEffect(AbnormalEffect.BIG_HEAD);
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_shrinkhead:
				if (activeChar.getTarget() != null && activeChar.getTarget().isCreature())
					((Creature) activeChar.getTarget()).stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_play_sound:
			case admin_play_sounds:
				try
				{
					activeChar.broadcastPacket(new PlaySound(Type.MUSIC, wordList[1], 0, 0, null));
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message34", activeChar, wordList[1]));
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message35", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_atmosphere:
				try
				{
					String type = wordList[1];
					String state = wordList[2];
					int duration = 60;
					if (wordList.length > 3)
						duration = Integer.parseInt(wordList[3]);
					
					L2GameServerPacket packet = null;
					
					if (type.equals("signsky"))
					{
						if (state.equals("dawn"))
							packet = new SSQInfo(2);
						else if (state.equals("dusk"))
							packet = new SSQInfo(1);
					}
					else if (type.equals("sky"))
					{
						if (state.equals("night"))
							packet = new SunSet();
						else if (state.equals("day"))
							packet = new SunRise();
						else if (state.equals("red"))
							if (duration != 0)
								packet = new ExRedSky(duration);
							else
								packet = new ExRedSky(10);
					}
					else
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message36", activeChar));
					if (packet != null)
					{
						for (Player player : GameObjectsStorage.getAllPlayersForIterate())
							player.sendPacket(packet);
					}
				}
				catch (Exception ex)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message37", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
			case admin_set_displayeffect:
				target = activeChar.getTarget();
				if (!target.isNpc())
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message38", activeChar));
					return false;
				}
				try
				{
					//int diplayeffect = Integer.parseInt(wordList[1]);
					//npc.setDisplayEffect(diplayeffect);
				}
				catch (Exception e)
				{
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message39", activeChar));
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/effects_menu.htm"));
				break;
		}

		return true;
	}

	private void handleInvul(Player activeChar, Player target)
	{
		if(target.isInvul())
		{
			target.setIsInvul(false);
			target.stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			if(target.getPet() != null)
			{
				target.getPet().setIsInvul(false);
				target.getPet().stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			}
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message40", activeChar, target.getName()));
		}
		else
		{
			target.setIsInvul(true);
			target.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			if(target.getPet() != null)
			{
				target.getPet().setIsInvul(true);
				target.getPet().startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			}
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message41", activeChar, target.getName()));
		}
	}

	private void handleTransform(Player activeChar, int transformation)
	{
		handleTransform(activeChar, transformation, 0);
	}
	
	private void handleTransform(Player activeChar, int transformation, int radius)
	{
		Skill skill = null;
		if (transformation > 0)
		{
			for (Skill sk : SkillTable.getInstance().getAllSkills().values())
			{
				if (skill != null)
					break;
				
				for (EffectTemplate et : sk.getEffectTemplates())
				{
					if (et.getEffectType() == EffectType.Transformation && et.getParam().getInteger("value") == transformation)
					{
						skill = sk;
						break;
					}
				}
			}
		}
		
		if (radius > 0)
		{
			for (Creature character : activeChar.getAroundCharacters(radius, 200))
			{
				if (character.isPlayer())
				{
					if (skill != null)
					{
						if (character.getPlayer().getTransformation() != 0)
							character.getPlayer().setTransformation(0);
						character.getPlayer().callSkill(skill, Collections.singletonList(character), false);
					}
					else
					{
						if (character.getPlayer().getTransformation() != 0)
							character.getPlayer().setTransformation(0);
						character.getPlayer().setTransformation(transformation);
					}
				}
			}
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message42", activeChar, radius));
			return;
		}
		else
		{
			if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer())
			{
				if (skill != null)
				{
					if (activeChar.getTarget().getPlayer().getTransformation() != 0)
						activeChar.getTarget().getPlayer().setTransformation(0);
					activeChar.getTarget().getPlayer().callSkill(skill, Collections.singletonList((Creature)activeChar.getTarget().getPlayer()), false);
				}
				else
				{
					if (activeChar.getTarget().getPlayer().getTransformation() != 0)
						activeChar.getTarget().getPlayer().setTransformation(0);
					activeChar.getTarget().getPlayer().setTransformation(transformation);
				}
				return;
			}
				
			else
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.admineffects.message43", activeChar));
		}
	}
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}