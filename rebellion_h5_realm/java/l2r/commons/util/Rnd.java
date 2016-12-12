package l2r.commons.util;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;


public class Rnd
{
	private Rnd() {}
	
	private static final Random random = new Random();
	
	private static final ThreadLocal<RandomGenerator> rnd = new ThreadLocalGeneratorHolder();
	
	private static AtomicLong seedUniquifier = new AtomicLong(8682522807148012L);
	
	static final class ThreadLocalGeneratorHolder extends ThreadLocal<RandomGenerator>
	{
		@Override
		public RandomGenerator initialValue()
		{
			return new MersenneTwister(seedUniquifier.getAndIncrement() + System.nanoTime());
		}
	}

	private static RandomGenerator rnd()
	{
		return rnd.get();
	}

	public static double get() // get random number from 0 to 1
	{
		return rnd().nextDouble();
	}

	/**
	 * Gets a random number from 0(inclusive) to n(exclusive)
	 *
	 * @param n The superior limit (exclusive)
	 * @return A number from 0 to n-1
	 */
	public static int get(int n)
	{
		return rnd().nextInt(n);
	}

	public static long get(long n)
	{
		return (long) (rnd().nextDouble() * n);
	}

	public static int get(int min, int max) // get random number from min to max (not max-1 !)
	{
		return min + get(max - min + 1);
	}

	public static long get(long min, long max) // get random number from min to max (not max-1 !)
	{
		return min + get(max - min + 1);
	}

	public static int nextInt()
	{
		return rnd().nextInt();
	}

	public static double nextDouble()
	{
		return rnd().nextDouble();
	}

	public static double nextGaussian()
	{
		return rnd().nextGaussian();
	}

	public static boolean nextBoolean()
	{
		return rnd().nextBoolean();
	}

	/**
	 * Рандомайзер для подсчета шансов.<br>
	 * Рекомендуется к использованию вместо Rnd.get()
	 * @param chance в процентах от 0 до 100
	 * @return true в случае успешного выпадания.
	 * <li>Если chance <= 0, вернет false
	 * <li>Если chance >= 100, вернет true
	 */
	public static boolean chance(int chance)
	{
		return chance >= 1 && (chance > 99 || rnd().nextInt(99) + 1 <= chance);
	}

	/**
	 * Рандомайзер для подсчета шансов.<br>
	 * Рекомендуется к использованию вместо Rnd.get() если нужны очень маленькие шансы
	 * @param chance в процентах от 0 до 100
	 * @return true в случае успешного выпадания.
	 * <li>Если chance <= 0, вернет false
	 * <li>Если chance >= 100, вернет true
	 */
	public static boolean chance(double chance)
	{
		return rnd().nextDouble() <= chance / 100.;
	}

	public static <E> E get(E[] list)
	{
		if (list == null || list.length == 0)
			return null;
		
		return list[get(list.length)];
	}

	public static int get(int[] list)
	{
		return list[get(list.length)];
	}

	public static <E> E get(List<E> list)
	{
		if (list == null || list.isEmpty())
			return null;
		
		return list.get(get(list.size()));
	}

	public static byte[] nextBytes(byte[] array)
	{
		random.nextBytes(array);
		return array;
	}

	public static int nextInt(int n)
	{
		if(n < 0)
			return random.nextInt(-n) * -1;
		if(n == 0)
			return 0;
		return random.nextInt(n);
	}

	public static <E> E get(Collection<E> set)
	{
		if ((set == null) || set.isEmpty())
		{
			return null;
		}
		
		int rnd = Rnd.get(set.size());
		int i = 0;
		for (E obj : set)
		{
			if (rnd == i++)
			{
				return obj;
			}
		}
		
		return null;
	}
}