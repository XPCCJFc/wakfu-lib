package wakfulib.utils.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Node<T> {

    public Node<T> next;
    public Node<T> previous;
    public T value;

    public static <T> Node<T> fromArray(T[] array) {
        Node<T> p = null;
        Node<T> h = null;
        for (T t : array) {
            p = new Node<>(null, p, t);
            if (h == null) {
                h = p;
            }
            if (p.previous != null) {
                p.previous.next = p;
            }
        }
        return h;
    }

    public static <T> Node<T> fromIterable(Iterable<T> array) {
        Node<T> p = null;
        Node<T> h = null;
        for (T t : array) {
            p = new Node<>(null, p, t);
            if (h == null) {
                h = p;
            }
            if (p.previous != null) {
                p.previous.next = p;
            }
        }
        return h;
    }

    public boolean hasNext() {
        return next != null;
    }

    public boolean hasPrevious() {
        return previous != null;
    }

    public Node<T> remove() {
        Node<T> nexts = next;
        if (previous != null) {
            previous.next = next;
        }
        if (next != null) {
            next.previous = previous;
        }
        this.next = null;
        this.previous = null;
        return nexts;
    }

    public Node<T> getFront() {
        Node<T> res = this;
        while (res.next != null) {
            res = res.getNext();
        }
        return res;
    }

    public Node<T> getBack() {
        Node<T> res = this;
        while (res.previous != null) {
            res = res.getPrevious();
        }
        return res;
    }

    public Node<T> toFront() {
        Node<T> prev = next;
        Node<T> front = getFront();
        remove();
        front.setNext(this);
        previous = front;
        return prev;
    }

    @Override
    public String toString() {
        Node<T> back = this;
        StringBuilder sb = new StringBuilder();
        if (back.previous != null) {
            sb.append("X, ");
        }
        if (back.next != null) {
            do {
                sb.append(back.value).append(", ");
                back = back.next;
            }  while (back.next != null);
        }
        sb.append(back.value);
        return sb.toString();
    }
}
