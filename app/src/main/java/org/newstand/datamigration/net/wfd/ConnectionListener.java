package org.newstand.datamigration.net.wfd;

import android.net.NetworkInfo;

/**
 * Created by Nick@NewStand.org on 2017/3/14 16:21
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public interface ConnectionListener {
    void onRequestConnectionInfo();

    void onWifiP2PConnectionChanged(NetworkInfo networkInfo);
}
