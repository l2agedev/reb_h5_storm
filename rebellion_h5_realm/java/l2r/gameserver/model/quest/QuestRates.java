package l2r.gameserver.model.quest;

import l2r.gameserver.Config;

public class QuestRates
{
	public static QuestRates DEFAULT_RATES = new QuestRates();
	
	public final double dropRate;
	public final double rewardItem;
	public final double rewardAdena;
	public final double rewardExpSp;

	public QuestRates(double dropRate, double rewardItem, double rewardAdena, double rewardExpSp)
	{
		this.dropRate = dropRate;
		this.rewardItem = rewardItem;
		this.rewardAdena = rewardAdena;
		this.rewardExpSp = rewardExpSp;
	}
	
	public QuestRates(double dropRate, double rewardItem, double rewardAdena)
	{
		this.dropRate = dropRate;
		this.rewardItem = rewardItem;
		this.rewardAdena = rewardAdena;
		this.rewardExpSp = Config.RATE_QUESTS_REWARD_EXPSP;
	}
	
	public QuestRates(double dropRate, double rewardItem)
	{
		this.dropRate = dropRate;
		this.rewardItem = rewardItem;
		this.rewardAdena = Config.QUEST_REWARD_ADENA;
		this.rewardExpSp = Config.RATE_QUESTS_REWARD_EXPSP;
	}
	
	public QuestRates(double dropRate)
	{
		this.dropRate = dropRate;
		this.rewardItem = Config.RATE_QUESTS_REWARD;
		this.rewardAdena = Config.QUEST_REWARD_ADENA;
		this.rewardExpSp = Config.RATE_QUESTS_REWARD_EXPSP;
	}
	
	public QuestRates()
	{
		this.dropRate = Config.RATE_QUESTS_DROP;
		this.rewardItem = Config.RATE_QUESTS_REWARD;
		this.rewardAdena = Config.QUEST_REWARD_ADENA;
		this.rewardExpSp = Config.RATE_QUESTS_REWARD_EXPSP;
	}
	
	public boolean isDefault()
	{
		return this == DEFAULT_RATES;
	}
}