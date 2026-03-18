package wakfulib.utils.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import wakfulib.utils.ArrayUtils;

public final class CombinedIterator<E> implements Iterator<E> {

    private final Iterator<E>[] iterators;
    private int index;
    private Iterator<E> currentIterator;
    
    public CombinedIterator(Collection<? extends Iterable<E>> collections) {
        if (collections.size() == 0) throw new IllegalArgumentException("CombinedIterator needs at least one iterator");
        this.iterators = collections.stream()
            .map(Iterable::iterator)
            .toArray(ArrayUtils.genericArray(Iterator[]::new));
        index = 0;
        this.currentIterator = iterators[index];
    }

    public <A> CombinedIterator(Collection<A> collections, Function<A, Iterator<E>> mapper) {
        if (collections.size() == 0) throw new IllegalArgumentException("CombinedIterator needs at least one iterator");
        this.iterators = collections.stream()
            .map(mapper)
            .toArray(ArrayUtils.genericArray(Iterator[]::new));
        index = 0;
        this.currentIterator = iterators[index];
    }

    @Override
    public boolean hasNext() {
        for (;;) {
            if (currentIterator.hasNext()) {
                return true;
            }

            if (iterators.length < index) {
                index++;
                currentIterator = iterators[index];
            } else {
                return false;
            }
        }
    }

    @Override
    public E next() {
        for (;;) {
            try {
                return currentIterator.next();
            } catch (NoSuchElementException e) {
                if (iterators.length < index) {
                    index++;
                    currentIterator = iterators[index];
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public void remove() {
        currentIterator.remove();
    }

}
