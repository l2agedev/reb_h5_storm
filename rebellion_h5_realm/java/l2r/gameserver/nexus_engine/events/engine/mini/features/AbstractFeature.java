package l2r.gameserver.nexus_engine.events.engine.mini.features;

import l2r.gameserver.nexus_engine.events.engine.base.EventType;
import l2r.gameserver.nexus_engine.events.engine.mini.EventMode.FeatureType;
import l2r.gameserver.nexus_interface.PlayerEventInfo;

import java.util.List;

import javolution.text.TextBuilder;
import javolution.util.FastTable;

/**
 * @author hNoke
 * a mother class of all EventMode's configs (=Features)
 */
public abstract class AbstractFeature
{
	protected EventType _event;
	protected String _params;
	
	//protected FastMap<String, String> _configs = new FastMap<String, String>();
	protected List<FeatureConfig> _configs = new FastTable<FeatureConfig>();
	
	public abstract FeatureType getType();
	
	protected abstract void initValues();
	
	public AbstractFeature(EventType event)
	{
		_event = event;
	}
	
	// config, description
	public abstract boolean checkPlayer(PlayerEventInfo player);
	
	protected String[] splitParams(String params)
	{
		return params.split(",");
	}
	
	public String getParams()
	{
		return _params;
	}
	
	protected void addConfig(String name, String desc, int inputFormType)
	{
		_configs.add(new FeatureConfig(name, desc, inputFormType));
	}
	
	public FeatureConfig getConfig(String name)
	{
		for(FeatureConfig c : _configs)
		{
			if(c.name.equals(name))
				return c;
		}
		return null;
	}
	
	public class FeatureConfig
	{
		public String name, desc;
		public int inputFormType;
		
		protected FeatureConfig(String name, String desc, int inputFormType)
		{
			this.name = name;
			this.desc = desc;
			this.inputFormType = inputFormType;
		}
	}
	
	public void setValueFor(String configName, String value)
	{
		String[] splitted = _params.split(",");
		
		int index = 0;
		for(FeatureConfig c : _configs)
		{
			if(c.name.equals(configName))
				break;
			
			index++;
		}
		
		if(splitted.length < index)
			return;
		else
			splitted[index] = value;
		
		TextBuilder tb = new TextBuilder();
		for(String s : splitted)
		{
			tb.append(s + ",");
		}
		String result = tb.toString();
		_params = result.substring(0, result.length() - 1);
		initValues();
	}
	
	public String getValueFor(String configName)
	{
		String[] splitted = _params.split(",");
		
		int index = 0;
		for(FeatureConfig c : _configs)
		{
			if(c.name.equals(configName))
				break;
			
			index++;
		}
		
		if(splitted.length < index)
		{
			return "N/A";
		}
		else
		{
			return splitted[index];
		}
	}
	
	public List<FeatureConfig> getConfigs()
	{
		return _configs;
	}
}
