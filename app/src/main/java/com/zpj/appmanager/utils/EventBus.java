package com.zpj.appmanager.utils;

import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

import com.zpj.appmanager.ui.activity.MainActivity;
import com.zpj.fragmentation.dialog.IDialog;
import com.zpj.rxbus.RxBus;
import com.zpj.utils.Callback;

import io.reactivex.functions.Consumer;

public class EventBus {

    public static final String KEY_FAB_EVENT = "event_fab";
    public static final String KEY_COLOR_CHANGE_EVENT = "event_color_change";
    public static final String KEY_HIDE_LOADING_EVENT = "event_hide_loading";
    public static final String KEY_SHOW_LOADING_EVENT = "event_show_loading";
    public static final String KEY_MAIN_ACTION_EVENT = "event_main_action";
    public static final String KEY_USER_INFO_CHANGE_EVENT = "event_user_info_change";
    public static final String KEY_SKIN_CHANGE_EVENT = "event_skin_change_change";
    public static final String KEY_REFRESH_EVENT = "event_refresh_change";
    private static final String KEY_SCROLL_EVENT = "event_scroll";
    private static final String KEY_IMAGE_UPLOAD_EVENT = "event_image_upload";
    private static final String KEY_SEARCH_EVENT = "event_search";
    private static final String KEY_KEYWORD_CHANGE_EVENT = "event_keyword_change";
    private static final String KEY_GET_APP_INFO_EVENT = "event_get_app_info";

    public static final String KEY_CROP_EVENT = "event_crop";

    public static final String KEY_SIGN_OUT_EVENT = "event_sign_out";
    public static final String KEY_QQ_SIGN_IN = "event_qq_sign_in";
    public static final String KEY_SIGN_IN_EVENT = "event_sign_in";
    public static final String KEY_SIGN_UP_EVENT = "event_sign_up";

    public static void post(Object o) {
        RxBus.post(o);
    }

    public static void post(Object o, long delay) {
        RxBus.postDelayed(o, delay);
    }

    private static class GetActivityEvent {

        private final Callback<MainActivity> callback;

        private GetActivityEvent(Callback<MainActivity> callback) {
            this.callback = callback;
        }

    }

    public static void getActivity(Callback<MainActivity> callback) {
        RxBus.post(new GetActivityEvent(callback));
    }

    public static void onGetActivityEvent(MainActivity mainActivity) {
        RxBus.observe(mainActivity, GetActivityEvent.class)
                .bindToLife(mainActivity)
                .doOnNext(new Consumer<GetActivityEvent>() {
                    @Override
                    public void accept(GetActivityEvent event) throws Exception {
                        if (event.callback != null) {
                            event.callback.onCallback(mainActivity);
                        }
                    }
                })
                .subscribe();
    }

    public static void sendSkinChangeEvent() {
        RxBus.post(KEY_SKIN_CHANGE_EVENT);
    }

    public static void onSkinChangeEvent(LifecycleOwner lifecycleOwner, Consumer<String> consumer) {
        registerObserver(lifecycleOwner, KEY_SKIN_CHANGE_EVENT, consumer);
    }

    public static void onSkinChangeEvent(View view, Consumer<String> consumer) {
        RxBus.observe(view, KEY_SKIN_CHANGE_EVENT)
                .bindView(view)
                .doOnNext(consumer)
                .subscribe();
    }

    public static void sendSearchEvent(String keyword) {
        RxBus.post(KEY_SEARCH_EVENT, keyword);
    }

    public static void onSearchEvent(LifecycleOwner lifecycleOwner, RxBus.SingleConsumer<String> consumer) {
        registerObserver(lifecycleOwner, KEY_SEARCH_EVENT, String.class, consumer);
    }

    public static void sendKeywordChangeEvent(String keyword) {
        RxBus.post(KEY_KEYWORD_CHANGE_EVENT, keyword);
    }

    private static class NullableObject {
        private final Object o;

        private NullableObject(Object o) {
            this.o = o;
        }

        public Object getObject() {
            return o;
        }
    }

    public static void hideLoading() {
        RxBus.post(KEY_HIDE_LOADING_EVENT, new NullableObject(null));
    }

    public static void hideLoading(int delay) {
        RxBus.postDelayed(KEY_HIDE_LOADING_EVENT, new NullableObject(null), delay);
    }

    public static void hideLoading(IDialog.OnDismissListener listener) {
        RxBus.post(KEY_HIDE_LOADING_EVENT, new NullableObject(listener));
    }

    public static void hideLoading(long delay, IDialog.OnDismissListener listener) {
        RxBus.postDelayed(KEY_HIDE_LOADING_EVENT, new NullableObject(listener), delay);
    }

    public static void onHideLoadingEvent(LifecycleOwner lifecycleOwner, Consumer<IDialog.OnDismissListener> consumer) {
        RxBus.observe(lifecycleOwner, KEY_HIDE_LOADING_EVENT, NullableObject.class)
                .bindToLife(lifecycleOwner)
                .doOnNext(new RxBus.SingleConsumer<NullableObject>() {
                    @Override
                    public void onAccept(NullableObject nullableObject) throws Exception {
                        if (consumer != null) {
                            if (nullableObject.getObject() instanceof IDialog.OnDismissListener) {
                                consumer.accept((IDialog.OnDismissListener) nullableObject.getObject());
                            } else {
                                consumer.accept(null);
                            }
                        }
                    }
                })
                .subscribe();
    }

    public static void showLoading(String text) {
        RxBus.post(KEY_SHOW_LOADING_EVENT, text, false);
    }

    public static void showLoading(String text, boolean isUpdate) {
        RxBus.post(KEY_SHOW_LOADING_EVENT, text, isUpdate);
    }

    public static void onShowLoadingEvent(LifecycleOwner lifecycleOwner, RxBus.PairConsumer<String, Boolean> next) {
        RxBus.observe(lifecycleOwner, KEY_SHOW_LOADING_EVENT, String.class, Boolean.class)
                .bindToLife(lifecycleOwner)
                .doOnNext(next)
                .subscribe();
    }


    public static <T> void registerObserver(View view, Class<T> type, Consumer<T> next) {
        RxBus.observe(view, type)
                .doOnNext(next)
                .subscribe();
    }

    public static <T> void registerObserver(LifecycleOwner lifecycleOwner, Class<T> type, Consumer<T> next) {
        RxBus.observe(lifecycleOwner, type)
                .bindToLife(lifecycleOwner)
                .doOnNext(next)
                .subscribe();
    }

    public static void registerObserver(LifecycleOwner lifecycleOwner, String key, Consumer<String> next) {
        RxBus.observe(lifecycleOwner, key)
                .bindToLife(lifecycleOwner)
                .doOnNext(next)
                .subscribe();
    }

    public static <T> void registerObserver(LifecycleOwner lifecycleOwner, String key, Class<T> type, RxBus.SingleConsumer<T> next) {
        RxBus.observe(lifecycleOwner, key, type)
                .bindToLife(lifecycleOwner)
                .doOnNext(next)
                .subscribe();
    }

    public static <T> void registerObserver(Object o, String key, Class<T> type, RxBus.SingleConsumer<T> next) {
        RxBus.observe(o, key, type)
                .doOnNext(next)
                .subscribe();
    }

//    public static void unSubscribe(Object o) {
//        RxBus.removeObservers(o);
//    }


}
