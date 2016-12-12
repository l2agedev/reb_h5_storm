package l2r.gameserver.stats;

import l2r.commons.lang.ArrayUtils;
import l2r.gameserver.model.Creature;
import l2r.gameserver.stats.funcs.Func;
import l2r.gameserver.stats.funcs.FuncOwner;

public final class Calculator
{
	private Func[] _functions;

	private double _base;
	private double _last;

	public final Stats _stat;
	public final Creature _character;

	public Calculator(Stats stat, Creature character)
	{
		_stat = stat;
		_character = character;
		_functions = Func.EMPTY_FUNC_ARRAY;
	}

	/**
	 * Return the number of Funcs in the Calculator.<BR><BR>
	 */
	public int size()
	{
		return _functions.length;
	}

	/**
	 * Add a Func to the Calculator.<BR><BR>
	 */
	public void addFunc(Func f)
	{
		_functions = ArrayUtils.add(_functions, f);
		ArrayUtils.eqSort(_functions);
	}

	/**
	 * Remove a Func from the Calculator.<BR><BR>
	 */
	public void removeFunc(Func f)
	{
		_functions = ArrayUtils.remove(_functions, f);
		if(_functions.length == 0)
			_functions = Func.EMPTY_FUNC_ARRAY;
		else
			ArrayUtils.eqSort(_functions);
	}

	/**
	 * Remove each Func with the specified owner of the Calculator.<BR><BR>
	 */
	public void removeOwner(Object owner)
	{
		Func[] tmp = _functions;
		for(Func element : tmp)
			if(element.owner == owner)
				removeFunc(element);
	}

	/**
	 * Run each Func of the Calculator.<BR><BR>
	 */
	public void calc(Env env)
	{
		Func[] funcs = _functions;
		_base = env.value;

		boolean overrideLimits = false;
		for(Func func : funcs)
		{
			if(func == null)
				continue;

			if(func.owner instanceof FuncOwner)
			{
				if(!((FuncOwner) func.owner).isFuncEnabled())
					continue;
				if(((FuncOwner) func.owner).overrideLimits())
					overrideLimits = true;
			}
			if(func.getCondition() == null || func.getCondition().test(env))
				func.calc(env);
		}

		if(!overrideLimits)
			env.value = _stat.validate(env.value);

		if (env.value != _last)
			_last = env.value;
	}

	/**
	 * Для отладки
	 */
	public Func[] getFunctions()
	{
		return _functions;
	}

	public double getBase()
	{
		return _base;
	}

	public double getLast()
	{
		return _last;
	}
}