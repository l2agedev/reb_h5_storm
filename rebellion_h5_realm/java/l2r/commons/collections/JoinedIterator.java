package l2r.commons.collections;

import java.util.Iterator;
import java.util.List;

/**
 * An JoinedIterator is an Iterator that wraps a number of Iterators.
 * <p/>
 * This class makes multiple iterators look like one to the caller.
 * When any method from the Iterator interface is called, the JoinedIterator
 * will delegate to a single underlying Iterator. The JoinedIterator will
 * invoke the Iterators in sequence until all Iterators are exhausted.
 *
 * @modify VISTALL
 */
public class JoinedIterator<E> implements Iterator<E>
{
	// wrapped iterators
	private Iterator<E>[] _iterators;

	// index of current iterator in the wrapped iterators array
	private int _currentIteratorIndex;

	// the current iterator
	private Iterator<E> _currentIterator;

	// the last used iterator
	private Iterator<E> _lastUsedIterator;

	public JoinedIterator(List<Iterator<E>> iterators)
	{
		this(iterators.toArray(new Iterator[iterators.size()]));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JoinedIterator(Iterator... iterators)
	{
		if(iterators == null)
			throw new NullPointerException("Unexpected NULL iterators argument");
		_iterators = iterators;
	}

	@Override
	public boolean hasNext()
	{
		updateCurrentIterator();
		return _currentIterator.hasNext();
	}

	@Override
	public E next()
	{
		updateCurrentIterator();
		return _currentIterator.next();
	}

	@Override
	public void remove()
	{
		updateCurrentIterator();
		_lastUsedIterator.remove();
	}

	// call this before any Iterator method to make sure that the current Iterator
	// is not exhausted
	protected void updateCurrentIterator()
	{
		if(_currentIterator == null)
		{
			if(_iterators.length == 0)
				_currentIterator = EmptyIterator.getInstance();
			else
				_currentIterator = _iterators[0];
			// set last used iterator here, in case the user calls remove
			// before calling hasNext() or next() (although they shouldn't)
			_lastUsedIterator = _currentIterator;
		}

		while(!_currentIterator.hasNext() && _currentIteratorIndex < _iterators.length - 1)
		{
			_currentIteratorIndex++;
			_currentIterator = _iterators[_currentIteratorIndex];
		}
	}
}
