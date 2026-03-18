package wakfulib.utils.data;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<E> implements Iterator<E> {
   private final E[] m_array;
   private final int m_arrayLength;
   private final boolean m_bReturnsNull;
   private int m_nextIndex = -1;

   public ArrayIterator(E[] array, boolean returnsNull) {
      this.m_array = array;
      this.m_arrayLength = array.length;
      this.m_bReturnsNull = returnsNull;
      this.searchNextIndex();
   }

   public boolean hasNext() {
      return this.m_nextIndex < this.m_arrayLength;
   }

   public E next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException("Array end reached. Array Size : " + this.m_arrayLength);
      } else {
         E val = this.m_array[this.m_nextIndex];
         this.searchNextIndex();
         return val;
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }

   private void searchNextIndex() {
      if (this.m_bReturnsNull) {
         ++this.m_nextIndex;
      } else {
         ++this.m_nextIndex;

         while(this.m_nextIndex < this.m_arrayLength && this.m_array[this.m_nextIndex] == null) {
            ++this.m_nextIndex;
         }
      }

   }
}
