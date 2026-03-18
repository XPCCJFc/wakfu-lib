package wakfulib.utils.data;

@FunctionalInterface
public interface QuadraFunction<A,B,C,D,R> {

    R apply(A a, B b, C c, D d);

}
