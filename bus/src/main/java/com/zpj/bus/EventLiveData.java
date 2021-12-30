package com.zpj.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LiveData of event.
 * @param <T> The type of data held by this instance
 * @author Z-P-J
 */
public class EventLiveData<T> {

    private static final int START_VERSION = -1;
    private static final Object NOT_SET = new Object();

    private final Map<LiveDataObserver<T>, ObserverWrapper> mObservers = new HashMap<>();

    // how many observers are in active state
    private final AtomicInteger mActiveCount = new AtomicInteger(0);

    private final AtomicInteger mVersion = new AtomicInteger(START_VERSION);

    private final boolean mIsStickyEvent;

    private volatile Object mData = NOT_SET;

    public EventLiveData(boolean isStickyEvent) {
        this.mIsStickyEvent = isStickyEvent;
    }

    public boolean isStickyEvent() {
        return mIsStickyEvent;
    }

    protected void onActive() {

    }

    protected void onInactive() {
        if (!mIsStickyEvent) {
            mData = NOT_SET;
        }
    }

    public void observe(@NonNull LiveDataObserver<T> observer) {
        synchronized (mObservers) {
            ObserverWrapper wrapper = new ObserverWrapper(observer);
            ObserverWrapper existing = mObservers.get(observer);
            if (existing == null) {
                mObservers.put(observer, wrapper);
            } else {
                if (!mIsStickyEvent) {
                    existing.updateVersion(mVersion.get());
                }
                return;
            }
            if (!mIsStickyEvent) {
                wrapper.updateVersion(mVersion.get());
            }
            observer.onAttach();
            wrapper.activeStateChanged(true);
        }
    }

    /**
     * Removes the given observer from the observers list.
     *
     * @param observer The Observer to receive events.
     */
    public void removeObserver(@NonNull final LiveDataObserver<T> observer) {
        synchronized (mObservers) {
            ObserverWrapper removed = mObservers.remove(observer);
            if (removed == null) {
                return;
            }
            removed.detachObserver();
            removed.activeStateChanged(false);
            observer.onDetach();
        }
    }

    public void removeObservers() {
        synchronized (mObservers) {
            for (Map.Entry<LiveDataObserver<T>, ObserverWrapper> entry : mObservers.entrySet()) {
                removeObserver(entry.getKey());
            }
        }
    }

    public void removeObservers(@NonNull final Object tag) {
        synchronized (mObservers) {
            for (Map.Entry<LiveDataObserver<T>, ObserverWrapper> entry : mObservers.entrySet()) {
                LiveDataObserver<T> key = entry.getKey();
                if (key.isBindTo(tag)) {
                    removeObserver(key);
                }
            }
        }
    }

    public void postValue(T value) {
        synchronized (mObservers) {
            int version = mVersion.incrementAndGet();
            mData = value;
            for (Map.Entry<LiveDataObserver<T>, ObserverWrapper> entry : mObservers.entrySet()) {
                entry.getValue().considerNotify(value, version);
            }
        }
    }

    /**
     * Returns the current value.
     * Note that calling this method on a background thread does not guarantee that the latest
     * value set will be received.
     *
     * @return the current value
     */
    @Nullable
    public T getValue() {
        Object data = mData;
        if (data != NOT_SET) {
            //noinspection unchecked
            return (T) data;
        }
        return null;
    }

    /**
     * Returns true if this LiveData has observers.
     *
     * @return true if this LiveData has observers
     */
    public boolean hasObservers() {
        synchronized (mObservers) {
            return mObservers.size() > 0;
        }
    }

    /**
     * Returns true if this LiveData has active observers.
     *
     * @return true if this LiveData has active observers
     */
    public boolean hasActiveObservers() {
        return mActiveCount.get() > 0;
    }

    private class ObserverWrapper {
        private final LiveDataObserver<T> mObserver;
        private volatile boolean mActive;
        private volatile int mLastVersion = START_VERSION;

        private ObserverWrapper(LiveDataObserver<T> observer) {
            mObserver = observer;
        }

        private void updateVersion(int version) {
            synchronized (this) {
                mLastVersion = version;
            }
        }

        private boolean compareOrUpdateVersion(int version) {
            synchronized (this) {
                if (mLastVersion >= version) {
                    return false;
                }
                mLastVersion = version;
                return true;
            }
        }

        private boolean shouldBeActive() {
            synchronized (mObserver) {
                return mObserver.isActive();
            }
        }

        private synchronized void detachObserver() {
        }

        private synchronized void activeStateChanged(boolean newActive) {
            if (newActive == mActive) {
                return;
            }
            // immediately set active state, so we'd never dispatch anything to inactive
            // owner
            mActive = newActive;
            boolean wasInactive = EventLiveData.this.mActiveCount.get() == 0;
            EventLiveData.this.mActiveCount.addAndGet(mActive ? 1 : -1);
            if (wasInactive && mActive) {
                onActive();
                if (!mIsStickyEvent) {
                    return;
                }
            }
            if (EventLiveData.this.mActiveCount.get() == 0 && !mActive) {
                onInactive();
                return;
            }
            if (mActive) {
                considerNotify(mData, mVersion.get());
            }
        }


        private synchronized void considerNotify(Object data, int version) {
            if (!mActive) {
                return;
            }
            // Check latest state b4 dispatch. Maybe it changed state but we didn't get the event yet.
            //
            // we still first check observer.active to keep it as the entrance for events. So even if
            // the observer moved to an active state, if we've not received that event, we better not
            // notify for a more predictable notification order.
            if (!shouldBeActive()) {
                activeStateChanged(false);
                return;
            }
            if (compareOrUpdateVersion(version)) {
                // noinspection unchecked
                mObserver.onChanged((T) data);
            }
        }

    }

    /**
     * A simple callback that can receive from {@link EventLiveData}.
     * @param <T> The type of the parameter
     * @author Z-P-J
     */
    public interface LiveDataObserver<T> {

        /**
         * Called when the data is changed.
         * @param t  The new data
         */
        void onChanged(@Nullable T t);

        /**
         * On attach the observer.
         */
        void onAttach();

        /**
         * On detach the observer.
         */
        void onDetach();

        /**
         * Call this when {@link EventLiveData#removeObservers(Object)}
         * @param o The object which binds to this observer
         * @return The {@link LiveDataObserver} whether binding the object.
         */
        boolean isBindTo(final Object o);

        /**
         * Whether the event observer is activated.
         * @return active
         */
        boolean isActive();

    }

}
