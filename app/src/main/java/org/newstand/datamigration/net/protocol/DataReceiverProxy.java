package org.newstand.datamigration.net.protocol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.google.common.base.Preconditions;

import org.newstand.datamigration.common.Consumer;
import org.newstand.datamigration.common.Holder;
import org.newstand.datamigration.data.model.DataCategory;
import org.newstand.datamigration.data.model.DataRecord;
import org.newstand.datamigration.net.BadResError;
import org.newstand.datamigration.net.CategoryReceiver;
import org.newstand.datamigration.net.DataRecordReceiver;
import org.newstand.datamigration.net.IORES;
import org.newstand.datamigration.net.OverviewReceiver;
import org.newstand.datamigration.net.ReceiveSettings;
import org.newstand.datamigration.net.server.TransportServer;
import org.newstand.datamigration.provider.SettingsProvider;
import org.newstand.datamigration.worker.transport.Session;
import org.newstand.datamigration.worker.transport.Stats;
import org.newstand.datamigration.worker.transport.TransportListener;
import org.newstand.logger.Logger;

import java.io.IOException;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Nick@NewStand.org on 2017/4/14 10:29
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public class DataReceiverProxy {

    @WorkerThread
    public static void receive(final Context context, final TransportServer transportServer,
                               final TransportListener listener, Session session) {
        receiveInternal(context, transportServer, listener, session);
    }

    @WorkerThread
    public static void receive(final Context context, final TransportServer transportServer,
                               final TransportListener listener) {
        receiveInternal(context, transportServer, listener, Session.create());
    }

    private static void receiveInternal(final Context context, final TransportServer transportServer,
                                        final TransportListener listener, Session session) {

        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(transportServer);
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(transportServer.getInputStream());
        //noinspection ResultOfMethodCallIgnored
        Preconditions.checkNotNull(transportServer.getOutputStream());

        OverviewReceiver overviewReceiver = OverviewReceiver.with(transportServer.getInputStream(), transportServer.getOutputStream());
        try {
            overviewReceiver.receive(null);
        } catch (IOException e) {
            // Overview header fail, can not move~
            listener.onAbort(e);
            return;
        }

        SimpleStats stats = new SimpleStats();
        stats.init(overviewReceiver.getHeader().getFileCount());
        listener.setStats(stats);
        listener.onStart();

        Set<DataCategory> dataCategories = overviewReceiver.getHeader().getDataCategories();

        int CATEGORY_SIZE = dataCategories.size();

        for (int i = 0; i < CATEGORY_SIZE; i++) {

            CategoryReceiver categoryReceiver = CategoryReceiver.with(transportServer.getInputStream(), transportServer.getOutputStream());
            try {
                categoryReceiver.receive(null);
                Logger.d("Received header: " + categoryReceiver.getHeader());

                CategoryHeader categoryHeader = categoryReceiver.getHeader();

                DataCategory category = categoryHeader.getDataCategory();

                int FILE_COUNT = categoryHeader.getFileCount();

                ReceiveSettings settings = new ReceiveSettings();

                final Holder<String> fileNameHolder = new Holder<>();

                settings.setNameConsumer(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) {
                        fileNameHolder.setData(s);
                    }
                });

                settings.setDestDir(SettingsProvider.getReceivedDirByCategory(category, session));

                for (int c = 0; c < FILE_COUNT; c++) {
                    int res = DataRecordReceiver.with(transportServer.getInputStream(),
                            transportServer.getOutputStream())
                            .receive(settings);
                    DataRecord record = new DataRecord();
                    record.setDisplayName(fileNameHolder.getData());
                    if (res == IORES.OK) {
                        listener.onPieceSuccess(record);
                        stats.onSuccess();
                    } else {
                        listener.onPieceFail(record, new BadResError(res));
                        stats.onFail();
                    }
                }
            } catch (IOException e) {
                listener.onAbort(e);
            }
        }

        listener.onComplete();

        transportServer.stop();
    }

    @ToString
    private static class SimpleStats implements Stats {

        @Setter(AccessLevel.PACKAGE)
        @Getter
        private int total, left, success, fail;

        private void init(int size) {
            total = left = size;
            Logger.d("init status %s", toString());
        }

        private void onPiece() {
            left--;
        }

        @Override
        public void onSuccess() {
            success++;
            onPiece();
        }

        @Override
        public void onFail() {
            fail++;
            onPiece();
        }

        @Override
        public Stats merge(Stats with) {

            total += with.getTotal();
            left += with.getLeft();
            success += with.getSuccess();
            fail += with.getFail();

            return this;
        }
    }
}
