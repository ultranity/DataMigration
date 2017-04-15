package org.newstand.datamigration.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.text.TextUtils;

import org.newstand.datamigration.provider.SettingsProvider;
import org.newstand.datamigration.sync.Sleeper;
import org.newstand.logger.Logger;

/**
 * Created by Nick@NewStand.org on 2017/3/13 13:36
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public class SmsContentProviderCompat {

    public static final Uri INBOX_CONTENT_URI = Uri.parse("content://sms/inbox");
    public static final Uri SENT_CONTENT_URI = Uri.parse("content://sms/sent");
    public static final Uri DRAFT_CONTENT_URI = Uri.parse("content://sms/draft");

    public static final String ADDRESS = "address";
    public static final String BODY = "body";
    public static final String DATE_SENT = "date_sent";
    public static final String READ = "read";

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setAsDefaultSmsApp(Context context) {

        String defaultSmsApp = null;
        String currentPn = context.getPackageName();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);
        }

        if (!TextUtils.isEmpty(defaultSmsApp)) {
            SettingsProvider.setDefSmsApp(defaultSmsApp);
        } else {
            return;
        }

        if (!defaultSmsApp.equals(currentPn)) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, currentPn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static boolean waitUtilBecomeDefSmsApp(Context context, int maxTimes) {

        for (int i = 0; i < maxTimes; i++) {
            String defaultSmsApp;
            String currentPn = context.getPackageName();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);
            } else {
                return true;
            }

            if (currentPn.equals(defaultSmsApp)) {
                return true;
            }

            Sleeper.sleepQuietly(1000);
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void restoreDefSmsApp(Context context) {
        String defaultSmsApp = null;

        String me = context.getPackageName();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);
        }

        if (TextUtils.isEmpty(defaultSmsApp)) {
            return;
        }

        if (me.equals(defaultSmsApp)) {
            Logger.w("Current def SMS app is me??? WTF???");
            return;
        }

        Logger.d("defaultSmsApp is %s", defaultSmsApp);

        String previousPkg = SettingsProvider.getDefSmsApp();

        if (TextUtils.isEmpty(previousPkg)) return;

        if (defaultSmsApp.equals(me)) {

            Logger.v("Restoring sms app to %s", previousPkg);

            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, previousPkg);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
