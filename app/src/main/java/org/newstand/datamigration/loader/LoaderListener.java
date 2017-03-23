package org.newstand.datamigration.loader;

import java.util.Collection;

/**
 * Created by Nick@NewStand.org on 2017/3/7 11:19
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public interface LoaderListener<T> {
    void onStart();

    void onComplete(Collection<T> collection);

    void onErr(Throwable throwable);
}
