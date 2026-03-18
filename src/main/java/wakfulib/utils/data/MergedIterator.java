package wakfulib.utils.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MergedIterator<T>
    implements Iterator<T> {
    private final List<Iterator<? extends T>> iterators = new ArrayList<>(2);
    private Iterator<? extends T> lastIterator = null;
    private int lastIteratorIndex = 0;

    public MergedIterator() {
    }

    @SafeVarargs
    public MergedIterator(Iterator<? extends T>... its) {

        this.iterators.addAll(Arrays.asList(its));

        if (its.length > 0) {
            this.lastIterator = its[0];
        }

    }

    public MergedIterator(Iterator<T> it) {
        this.iterators.add(it);
        this.lastIterator = it;
    }

    public void merge(Iterator<T> it) {
        this.iterators.add(it);
        if (this.lastIterator == null) {
            this.lastIterator = it;
        }
    }

    public boolean hasNext() {
        return this.lastIterator != null && this.lastIterator.hasNext();
    }

    public T next() {
        var o = this.lastIterator.next();
        if (! this.lastIterator.hasNext()) {
            ++ this.lastIteratorIndex;
            this.lastIterator = this.lastIteratorIndex >= this.iterators.size() ? null : this.iterators.get(this.lastIteratorIndex);
        }
        return o;
    }

    public void remove() {
    }
}