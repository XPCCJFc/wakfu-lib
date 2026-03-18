package wakfulib.utils.data;


public class LongLightWeightSet extends AbstractLongKeyLightWeightMap {
    public LongLightWeightSet(int initialSize) {
        super(initialSize);
    }

    public LongLightWeightSet() {
        super(10);
    }

    public void add(long key) {
        this.checkCapacity();
        int index = this.insertIndex(key);
        if (index >= 0) {
            this.m_indexes[index] = key;
            ++this.m_size;
        }

    }

    public boolean remove(long key) {
        if (this.m_size == 0) {
            return false;
        } else {
            int index = this.index(key);
            return index >= 0 && this.removeQuick(index);
        }
    }

    public boolean removeQuick(int index) {
        this.m_indexes[index] = this.m_indexes[this.m_size - 1];
        --this.m_size;
        return true;
    }
}
