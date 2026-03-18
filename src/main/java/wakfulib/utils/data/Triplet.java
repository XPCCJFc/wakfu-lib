package wakfulib.utils.data;

import lombok.Data;

@Data
public class Triplet<T, U, V> {
    public final T _1;
    public final U _2;
    public final V _3;
}
