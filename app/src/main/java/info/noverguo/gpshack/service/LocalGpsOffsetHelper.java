package info.noverguo.gpshack.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.noverguo.gpshack.BuildConfig;
import info.noverguo.gpshack.IGpsOffsetService;
import info.noverguo.gpshack.callback.ResultCallback;

/**
 * Created by noverguo on 2016/6/8.
 *
 * context 是宿主的context,在此将宿主绑定到了IGpsOffsetService(RemoteOffsetGpsService)
 *RemoteOffsetGpsService里面和binder进行绑定
 * binder里面
 */
public class LocalGpsOffsetHelper {
    private static final String TAG = LocalGpsOffsetHelper.class.getSimpleName();
    private static LocalGpsOffsetHelper sInst;
    private Context context;
    private IGpsOffsetService remotegpsOffsetService;
    private boolean init = false;

    ExecutorService executorService;
    LinkedList<Runnable> tasks = new LinkedList<>();

    private ServiceConnection mRemoteConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            if (BuildConfig.DEBUG) Log.i(TAG, "onServiceConnected");
            synchronized (LocalGpsOffsetHelper.this) {
                //service和binder绑定了的!
                //获得另一个进程中的Service传递过来的IBinder对象-service
                remotegpsOffsetService = IGpsOffsetService.Stub.asInterface(service);
            }
            runIfNeed();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (BuildConfig.DEBUG) Log.i(TAG, "onServiceDisconnected");
            synchronized (LocalGpsOffsetHelper.this) {
                init = false;
                remotegpsOffsetService = null;
            }
        }
    };

    public static LocalGpsOffsetHelper get(Context context) {
        if (sInst == null) {
            synchronized (LocalGpsOffsetHelper.class) {
                if (sInst == null) {
                    sInst = new LocalGpsOffsetHelper(context);
                }
            }
        }
        return sInst;
    }

    private LocalGpsOffsetHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 将宿主和 IGpsOffsetService 绑定到了一起
     */
    private boolean init() {
        synchronized (this) {
            if (init) {
                return true;
            }
            init = true;
        }
        if (remotegpsOffsetService == null) {
            Intent intent = new Intent(IGpsOffsetService.class.getName());
            intent.setPackage("info.noverguo.gpshack");
            try {
                context.bindService(intent, mRemoteConnection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void runIfNeed() {
        if (remotegpsOffsetService == null) {
            return;
        }
        synchronized (tasks) {
            if (tasks.isEmpty()) {
                return;
            }
        }
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Runnable task;
                synchronized (tasks) {
                    if (tasks.isEmpty()) {
                        return;
                    }
                    task = tasks.removeFirst();
                }
                task.run();
                runIfNeed();
            }
        });
    }

    public void getLatitudeOffset(final ResultCallback<Double> callback) {
        if (callback == null) {
            return;
        }
        if (remotegpsOffsetService != null) {
            IGpsOffsetService gpsOffsetService;
            synchronized (this) {
                gpsOffsetService = remotegpsOffsetService;
            }
            if (gpsOffsetService != null) {
                try {
                    if (BuildConfig.DEBUG) Log.i(TAG, "getLatitudeOffset.sync");
                    callback.onResult(remotegpsOffsetService.getLatitudeOffset());
                } catch (RemoteException e) {
                    callback.onError(e);
                }
                return;
            }
        }
        if (!init()) {
            return;
        }
        synchronized (tasks) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.DEBUG) Log.i(TAG, "getLatitudeOffset.task");
                    try {
                        callback.onResult(remotegpsOffsetService.getLatitudeOffset());
                    } catch (RemoteException e) {
                        callback.onError(e);
                    }
                }
            });
        }
        runIfNeed();
    }

    public void getLongitudeOffset(final ResultCallback<Double> callback) {
        if (callback == null) {
            return;
        }
        if (remotegpsOffsetService != null) {
            IGpsOffsetService gpsOffsetService;
            synchronized (this) {
                gpsOffsetService = remotegpsOffsetService;
            }
            if (gpsOffsetService != null) {
                try {
                    if (BuildConfig.DEBUG) Log.i(TAG, "getLongitudeOffset.sync");
                    callback.onResult(remotegpsOffsetService.getLongitudeOffset());
                } catch (RemoteException e) {
                    callback.onError(e);
                }
                return;
            }
        }
        if (!init()) {
            return;
        }
        synchronized (tasks) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.DEBUG) Log.i(TAG, "getLongitudeOffset.task");
                    try {
                        callback.onResult(remotegpsOffsetService.getLongitudeOffset());
                    } catch (RemoteException e) {
                        callback.onError(e);
                    }
                }
            });
        }
        runIfNeed();
    }

    public void getLatitude(final ResultCallback<Double> callback) {
        if (callback == null) {
            return;
        }
        if (remotegpsOffsetService != null) {
            IGpsOffsetService gpsOffsetService;
            synchronized (this) {
                gpsOffsetService = remotegpsOffsetService;
            }
            if (gpsOffsetService != null) {
                try {
                    if (BuildConfig.DEBUG) Log.i(TAG, "getLatitude.sync");
                    callback.onResult(remotegpsOffsetService.getLatitude());
                } catch (RemoteException e) {
                    callback.onError(e);
                }
                return;
            }
        }
        if (!init()) {
            return;
        }
        synchronized (tasks) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.DEBUG) Log.i(TAG, "getLatitude.task");
                    try {
                        callback.onResult(remotegpsOffsetService.getLatitude());
                    } catch (RemoteException e) {
                        callback.onError(e);
                    }
                }
            });
        }
        runIfNeed();
    }

    public void getLongitude(final ResultCallback<Double> callback) {
        if (callback == null) {
            return;
        }
        if (remotegpsOffsetService != null) {
            IGpsOffsetService gpsOffsetService;
            synchronized (this) {
                gpsOffsetService = remotegpsOffsetService;
            }
            if (gpsOffsetService != null) {
                try {
                    if (BuildConfig.DEBUG) Log.i(TAG, "getLongitude.sync");
                    callback.onResult(remotegpsOffsetService.getLongitude());
                } catch (RemoteException e) {
                    callback.onError(e);
                }
                return;
            }
        }
        if (!init()) {
            return;
        }
        synchronized (tasks) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.DEBUG) Log.i(TAG, "getLongitude.task");
                    try {
                        callback.onResult(remotegpsOffsetService.getLongitude());
                    } catch (RemoteException e) {
                        callback.onError(e);
                    }
                }
            });
        }
        runIfNeed();
    }
}
