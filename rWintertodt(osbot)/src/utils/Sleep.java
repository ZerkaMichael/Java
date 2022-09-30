package utils;

import static org.osbot.rs07.script.MethodProvider.random;
import java.util.function.BooleanSupplier;

public final class Sleep extends org.osbot.rs07.utility.ConditionalSleep {

    private final BooleanSupplier condition;

    public Sleep(final BooleanSupplier condition, final int timeout) {
        super(timeout);
        this.condition = condition;
    }

    public Sleep(final BooleanSupplier condition, final int timeout, final int interval) {
        super(timeout, interval);
        this.condition = condition;
    }

    @Override
    public final boolean condition() throws InterruptedException {
        return condition.getAsBoolean();
    }

    public static boolean sleepUntil(final BooleanSupplier condition, final int timeout) {
        return new Sleep(condition, timeout).sleep();
    }

    public static boolean sleepUntil(final BooleanSupplier condition, final int timeout, final int interval) {
        return new Sleep(condition, timeout, interval).sleep();
    }

    public static int weighted(int min, int target, int max){
        int lower = random(min, target);
        int upper = random(target, max);
        int weighted = random(lower, upper);
        return weighted;
    }
}
