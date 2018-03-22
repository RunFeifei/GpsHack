package info.noverguo.gpshack.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;

/**
 * Created by noverguo on 2016/6/8.
 */
public abstract class SuperActionReceiver extends BroadcastReceiver {
    boolean unregister = false;
    Context context;
    public SuperActionReceiver(Context context) {
        this.context = context.getApplicationContext();
    }
    public synchronized boolean unregister() {
        if (unregister) {
            return false;
        }
        unregister = true;
        context.unregisterReceiver(this);
        return true;
    }
}
