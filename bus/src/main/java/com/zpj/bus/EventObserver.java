package com.zpj.bus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

/**
 * Observer of bus.
 * @param <T> The type of consumer.
 * @author Z-P-J
 */
public interface EventObserver<T> {

//    IObserver<T> subscribeOn(Scheduler scheduler);

    /**
     * {@link com.zpj.bus.Schedulers.Scheduler}
     * @param scheduler
     * @return
     */
    EventObserver<T> observeOn(Schedulers.Scheduler scheduler);

    /**
     * Bind the tag.
     * @param tag The tag of observer.
     * @return this
     */
    EventObserver<T> bindTag(Object tag);

    /**
     * Bind the tag.
     * @param tag The tag of observer.
     * @param disposeBefore Remove the previous observers which bind the {@param tag}
     * @return this
     */
    EventObserver<T> bindTag(Object tag, boolean disposeBefore);

    /**
     * Bind the view.
     * @param view A View
     * @return this
     */
    EventObserver<T> bindView(View view);

    /**
     *
     * @param lifecycleOwner
     * @return this
     */
    EventObserver<T> bindLifecycle(LifecycleOwner lifecycleOwner);

    /**
     *
     * @param lifecycleOwner
     * @param event
     * @return this
     */
    EventObserver<T> bindLifecycle(LifecycleOwner lifecycleOwner, Lifecycle.Event event);

    /**
     *
     * @param onChange
     * @return this
     */
    EventObserver<T> doOnChange(final T onChange);

    /**
     *
     * @param onAttach
     * @return this
     */
    EventObserver<T> doOnAttach(final Runnable onAttach);

    /**
     *
     * @param onDetach
     * @return this
     */
    EventObserver<T> doOnDetach(final Runnable onDetach);

    /**
     * Subscribe the observer of event.
     * @param onNext
     */
    void subscribe(final T onNext);

    /**
     * Subscribe the observer of event.
     */
    void subscribe();

}
