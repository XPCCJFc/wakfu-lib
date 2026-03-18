package wakfulib.utils.data;

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class ByteObjectLightWeightMap extends AbstractByteKeyLightWeightMap implements Iterable {
   private Object[] m_values;

   public ByteObjectLightWeightMap() {
      this(10);
   }

   public ByteObjectLightWeightMap(int initialCapacity) {
      super(initialCapacity);
      this.m_values = new Object[initialCapacity];
   }

   public boolean ensureCapacity(int newCapacity) {
      int oldCapacity = this.m_indexes.length;
      if (!super.ensureCapacity(newCapacity)) {
         return false;
      } else {
         Object[] values = this.m_values;
         this.m_values = new Object[newCapacity];
         System.arraycopy(values, 0, this.m_values, 0, oldCapacity);
         return true;
      }
   }

   public void put(byte key, Object value) {
      this.checkCapacity();
      int index = this.insertIndex(key);
      if (index < 0) {
         index = -index - 1;
      } else {
         ++this.m_size;
         this.m_indexes[index] = key;
      }

      this.m_values[index] = value;
   }

   public Object remove(byte key) {
      if (this.m_size == 0) {
         return null;
      } else {
         int index = this.index(key);
         if (index < 0) {
            return null;
         } else {
            Object removed = this.m_values[index];
            if (index < this.m_size - 1) {
               this.m_indexes[index] = this.m_indexes[this.m_size - 1];
               this.m_values[index] = this.m_values[this.m_size - 1];
               this.m_indexes[this.m_size - 1] = 0;
               this.m_values[this.m_size - 1] = null;
            } else {
               this.m_indexes[index] = 0;
               this.m_values[index] = null;
            }

            --this.m_size;
            return removed;
         }
      }
   }

   public void clear() {
      super.clear();
      int i = 0;

      for(int size = this.m_values.length; i < size; ++i) {
         this.m_values[i] = null;
      }

   }

   public Object get(byte key) {
      int index = this.index(key);
      return index < 0 ? null : this.m_values[index];
   }

   public final Object getQuickValue(int index) {
      return this.m_values[index];
   }

    @NotNull
    public final Iterator iterator() {
      return new ArrayIterator(this.m_values, false);
   }
}
