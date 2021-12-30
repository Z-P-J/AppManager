package com.zpj.bus;

import android.annotation.SuppressLint;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A class which implements the {@link EventObserver} and {@link EventLiveData.LiveDataObserver}, .
 * @param <T> The type of event.
 * @param <C> The consumer of event.
 * @author Z-P-J
 */
class EventObserverImpl<T, C extends Consumer<? super T>>
        implements EventObserver<C>, EventLiveData.LiveDataObserver<T>, View.OnAttachStateChangeListener {

    @NonNull
    private final EventLiveData<T> mLiveData;
    private Schedulers.Scheduler mScheduler;

    private final Set<LifecycleBoundObserver> mLifecycleObservers = new HashSet<>(0);
    private final Set<Object> mTags = new HashSet<>(0);
    private final WeakHashMap<View, Object> mAttachViews = new WeakHashMap<>();

    private C mOnChange = null;
    private Runnable mOnAttach = null;
    private Runnable mOnDetach = null;

    EventObserverImpl(@NonNull EventLiveData<T> liveData) {
        this(liveData, null);
    }

    EventObserverImpl(@NonNull EventLiveData<T> liveData, String key) {
        this.mLiveData = liveData;
        if (!TextUtils.isEmpty(key)) {
            mTags.add(key);
        }
    }

    @Override
    public EventObserver<C> observeOn(Schedulers.Scheduler scheduler) {
        this.mScheduler = scheduler;
        return this;
    }

    @Override
    public EventObserver<C> bindTag(Object tag) {
        return bindTag(tag, false);
    }

    @Override
    public EventObserver<C> bindTag(Object tag, boolean disposeBefore) {
        this.mTags.add(tag);
        return this;
    }

    @Override
    public EventObserver<C> bindView(View view) {
        if (view != null) {
            if (!mAttachViews.containsKey(view)) {
                view.addOnAttachStateChangeListener(this);
                mAttachViews.put(view, null);
            }
        }
        return this;
    }

    @Override
    public EventObserver<C> bindLifecycle(LifecycleOwner lifecycleOwner) {
        return bindLifecycle(lifecycleOwner, Lifecycle.Event.ON_DESTROY);
    }

    @Override
    public EventObserver<C> bindLifecycle(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
        LifecycleBoundObserver observer = new LifecycleBoundObserver(lifecycleOwner, this, event);
        synchronized (mLifecycleObservers) {
            if (!mLifecycleObservers.contains(observer)) {
                mLifecycleObservers.add(observer);
                observer.attach();
            }
        }
        return this;
    }

    @Override
    public EventObserver<C> doOnChange(C onChange) {
        this.mOnChange = onChange;
        return this;
    }

    @Override
    public EventObserver<C> doOnAttach(Runnable onAttach) {
        this.mOnAttach = onAttach;
        return this;
    }

    @Override
    public EventObserver<C> doOnDetach(Runnable onDetach) {
        this.mOnDetach = onDetach;
        return this;
    }

    @Override
    public void subscribe(C onNext) {
        doOnChange(onNext).subscribe();
    }

    @Override
    public void subscribe() {
        this.mLiveData.observe(this);
    }

    @Override
    public void onAttach() {
        if (mOnAttach != null) {
            execute(mOnAttach);
        }
    }

    @Override
    public void onDetach() {
        mOnAttach = null;
        mOnChange = null;
        synchronized (mAttachViews) {
            for (View view : mAttachViews.keySet()) {
                if (view != null) {
                    view.removeOnAttachStateChangeListener(this);
                }
            }
            mAttachViews.clear();
        }

        synchronized (mLifecycleObservers) {
            for (LifecycleBoundObserver observer : mLifecycleObservers) {
                observer.detach();
            }
            mLifecycleObservers.clear();
        }

        mTags.clear();

        if (mOnDetach != null) {
            execute(mOnDetach);
        }
        mOnDetach = null;

    }

    @Override
    public boolean isBindTo(Object o) {
        synchronized (mTags) {
            if (mTags.contains(o)) {
                return true;
            }
        }
        if (o instanceof LifecycleOwner) {
            LifecycleOwner owner = (LifecycleOwner) o;
            synchronized (mLifecycleObservers) {
                for (LifecycleBoundObserver observer : mLifecycleObservers) {
                    if (observer.mOwner == owner) {
                        return true;
                    }
                }
            }
        } else if (o instanceof View) {
            synchronized (mAttachViews) {
                return mAttachViews.containsKey(o);
            }
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void onChanged(@Nullable final T t) {
        if (mOnChange != null) {
            execute(new Runnable() {
                @Override
                public void run() {
                    mOnChange.accept(t);
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {

    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        synchronized (mAttachViews) {
            mAttachViews.remove(v);
        }
        v.removeOnAttachStateChangeListener(this);
        detach();
    }

    private void execute(Runnable runnable) {
        if (mScheduler == null) {
            synchronized (EventObserverImpl.this) {
                if (mScheduler == null) {
                    mScheduler = Schedulers.main();
                }
            }
        }
        mScheduler.execute(runnable);
    }

    private void detach() {
        mLiveData.removeObserver(this);
    }

    @SuppressLint("RestrictedApi")
    private static final class LifecycleBoundObserver implements GenericLifecycleObserver {

        @NonNull
        private final LifecycleOwner mOwner;

        @NonNull
        private final EventObserverImpl<?, ?> mObserver;

        @NonNull
        private final Lifecycle.Event mEvent;

        public LifecycleBoundObserver(@NonNull LifecycleOwner mOwner, @NonNull EventObserverImpl<?, ?> mObserver, @NonNull Lifecycle.Event mEvent) {
            this.mOwner = mOwner;
            this.mObserver = mObserver;
            this.mEvent = mEvent;
        }

        private void attach() {
            this.mOwner.getLifecycle().addObserver(this);
        }

        private void detach() {
            this.mOwner.getLifecycle().removeObserver(this);
        }

        @Override
        public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
            if (event == this.mEvent || event == Lifecycle.Event.ON_DESTROY) {
                this.mObserver.detach();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof LifecycleBoundObserver) {
                LifecycleBoundObserver that = (LifecycleBoundObserver) o;
                return mOwner.equals(that.mOwner)
                        && mObserver.equals(that.mObserver)
                        && mEvent == that.mEvent;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mOwner, mObserver, mEvent);
        }
    }

}