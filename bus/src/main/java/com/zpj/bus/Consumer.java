package com.zpj.bus;

/**
 * Consume the event.
 * @param <T> The type of observation.
 * @author Z-P-J
 */
public interface Consumer<T> {

    /**
     * Accept the event.
     * @param t The type of observation
     */
    void accept(T t);

}
