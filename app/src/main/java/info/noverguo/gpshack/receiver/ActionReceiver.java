package info.noverguo.gpshack.receiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.widget.Toast;

import info.noverguo.gpshack.service.GpsOffsetServiceBinder;

/**
 * Created by noverguo on 2016/7/18.
 * 监测上下左右 并设置经纬度
 */
public class ActionReceiver extends SuperActionReceiver {
    private static final String ACTION_LEFT = "info.noverguo.gpshack.receiver.ActionReceiver.ACTION_LEFT";
    private static final String ACTION_UP = "info.noverguo.gpshack.receiver.ActionReceiver.ACTION_UP";
    private static final String ACTION_DOWN = "info.noverguo.gpshack.receiver.ActionReceiver.ACTION_DOWN";
    private static final String ACTION_RIGHT = "info.noverguo.gpshack.receiver.ActionReceiver.ACTION_RIGHT";
    private static final double DISTANCE = 0.0003;
    GpsOffsetServiceBinder gpsOffsetServiceBinder;

    public ActionReceiver(Context context) {
        super(context);
    }

    public void setGpsOffsetServiceBinder(GpsOffsetServiceBinder gpsOffsetServiceBinder) {
        this.gpsOffsetServiceBinder = gpsOffsetServiceBinder;
    }
    Toast toast;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (gpsOffsetServiceBinder == null) {
            gpsOffsetServiceBinder = GpsOffsetServiceBinder.get(context);
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        if (ACTION_LEFT.equals(action)) {
            gpsOffsetServiceBinder.setLongitudeOffset(gpsOffsetServiceBinder.getLongitudeOffset() - DISTANCE);
            toast = Toast.makeText(context.getApplicationContext(), "向左移动", Toast.LENGTH_SHORT);
        } else if (ACTION_RIGHT.equals(action)) {
            gpsOffsetServiceBinder.setLongitudeOffset(gpsOffsetServiceBinder.getLongitudeOffset() + DISTANCE);
            toast = Toast.makeText(context.getApplicationContext(), "向右移动", Toast.LENGTH_SHORT);
        } else if (ACTION_UP.equals(action)) {
            gpsOffsetServiceBinder.setLatitudeOffset(gpsOffsetServiceBinder.getLatitudeOffset() + DISTANCE);
            toast = Toast.makeText(context.getApplicationContext(), "向上移动", Toast.LENGTH_SHORT);
        } else if (ACTION_DOWN.equals(action)) {
            gpsOffsetServiceBinder.setLatitudeOffset(gpsOffsetServiceBinder.getLatitudeOffset() - DISTANCE);
            toast = Toast.makeText(context.getApplicationContext(), "向下移动", Toast.LENGTH_SHORT);
        }
        if (toast != null) {
            ResetReceiver.sendReset(context.getApplicationContext());
            toast.show();
        }
    }

    public static PendingIntent getLeftPendingIntent(Context context) {
        return PendingIntent.getBroadcast(context, 1, new Intent(ActionReceiver.ACTION_LEFT), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getUpPendingIntent(Context context) {
        return PendingIntent.getBroadcast(context, 1, new Intent(ActionReceiver.ACTION_UP), PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public static PendingIntent getDownPendingIntent(Context context) {
        return PendingIntent.getBroadcast(context, 1, new Intent(ActionReceiver.ACTION_DOWN), PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public static PendingIntent getRightPendingIntent(Context context) {
        return PendingIntent.getBroadcast(context, 1, new Intent(ActionReceiver.ACTION_RIGHT), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionReceiver.ACTION_LEFT);
        intentFilter.addAction(ActionReceiver.ACTION_UP);
        intentFilter.addAction(ActionReceiver.ACTION_DOWN);
        intentFilter.addAction(ActionReceiver.ACTION_RIGHT);
        return intentFilter;
    }

    public static ActionReceiver register(Context context) {
        ActionReceiver actionReceiver = new ActionReceiver(context);
        context.registerReceiver(actionReceiver, getIntentFilter());
        return actionReceiver;
    }
}
