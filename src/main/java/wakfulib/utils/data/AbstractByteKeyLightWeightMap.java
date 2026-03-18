package wakfulib.utils.data;

import java.util.Arrays;

/**
 * A memory-efficient map implementation that uses a {@code byte} primitive as the key.
 * This class uses a linear array search to find keys, making it "lightweight" and
 * suitable for very small maps where the overhead of a hash table is not justified.
 * It avoids boxing of primitive keys to reduce memory pressure.
 */
public abstract class AbstractByteKeyLightWeightMap {
   protected byte[] m_indexes;
   protected int m_size;

   protected AbstractByteKeyLightWeightMap(int initialSize) {
      this.m_indexes = new byte[initialSize];
      this.m_size = 0;
   }

   public boolean ensureCapacity(int newCapacity) {
      if (newCapacity > this.m_indexes.length) {
         byte[] indexes = this.m_indexes;
         this.m_indexes = new byte[newCapacity];
         System.arraycopy(indexes, 0, this.m_indexes, 0, indexes.length);
         return true;
      } else {
         return false;
      }
   }

   protected final int index(byte key) {
      for(int i = this.m_size - 1; i >= 0; --i) {
         if (key == this.m_indexes[i]) {
            return i;
         }
      }

      return -1;
   }

   protected final int insertIndex(byte key) {
      for(int i = this.m_size - 1; i >= 0; --i) {
         if (key == this.m_indexes[i]) {
            return -i - 1;
         }
      }

      return this.m_size;
   }

   protected final void checkCapacity() {
      if (this.m_size == this.m_indexes.length) {
         this.ensureCapacity(this.m_indexes.length * 2);
      }

   }

   public final boolean contains(byte key) {
      return this.index(key) != -1;
   }

   public final void reset() {
      this.m_size = 0;
   }

   public void clear() {
      this.m_size = 0;
   }

   public final int size() {
      return this.m_size;
   }

   public final byte getQuickKey(int index) {
      return this.m_indexes[index];
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AbstractByteKeyLightWeightMap that = (AbstractByteKeyLightWeightMap)o;
         if (this.m_size != that.m_size) {
            return false;
         } else {
            return Arrays.equals(this.m_indexes, that.m_indexes);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.m_indexes != null ? Arrays.hashCode(this.m_indexes) : 0;
      result = 31 * result + this.m_size;
      return result;
   }
}
