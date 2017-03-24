package org.newstand.datamigration.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.newstand.datamigration.R;
import org.newstand.datamigration.worker.backup.session.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;

/**
 * Created by Nick@NewStand.org on 2017/3/7 15:14
 * E-Mail: NewStand@163.com
 * All right reserved.
 */
public class SessionListAdapter extends RecyclerView.Adapter<SessionListViewHolder> {

    @Getter
    private final List<Session> sessionList;

    @Getter
    private Context context;

    public SessionListAdapter(Context context) {
        this.sessionList = new ArrayList<>();
        this.context = context;
    }

    public void update(Collection<Session> src) {
        synchronized (sessionList) {
            sessionList.clear();
            sessionList.addAll(src);
        }
        update();
    }

    public void update() {
        notifyDataSetChanged();
    }

    @Override
    public SessionListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.data_item_template_with_checkable, parent, false);
        final SessionListViewHolder holder = new SessionListViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(holder);
            }
        });
        return holder;
    }

    protected void onItemClick(SessionListViewHolder holder) {

    }

    @Override
    public void onBindViewHolder(SessionListViewHolder holder, final int position) {
        Session s = getSessionList().get(position);
        onBindViewHolder(holder, s);
    }

    protected void onBindViewHolder(SessionListViewHolder holder, final Session r) {
        holder.getLineOneTextView().setText(r.getName());
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }
}
