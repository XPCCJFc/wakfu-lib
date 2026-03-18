package wakfulib.utils.random;

public interface RandomProvider {
    int nextInt(int max);

    long nextLong(long max);

    boolean nextBoolean(double probability);
}
