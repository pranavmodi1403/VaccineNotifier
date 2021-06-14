package com.modip.vaccinenotifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class MoreAppsAdapter extends RecyclerView.Adapter<MoreAppsAdapter.ViewHolder> {
    private Context context;
    private List<moreAppsModel> list;
    private static MoreAppsAdapter.ClickListener clickListener;
    public MoreAppsAdapter(List<moreAppsModel> list, Context mCtx) {
        this.list = list;
        this.context = mCtx;
    }
    public void setOnItemClickListener(MoreAppsAdapter.ClickListener clickListener) {
        MoreAppsAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    @NonNull
    @Override
    public MoreAppsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.moreapps_record_row, parent, false);
        return new MoreAppsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MoreAppsAdapter.ViewHolder holder, final int position) {
        final moreAppsModel myList = list.get(position);
        Glide.with(context).
                load(myList.getApp_logo_url())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.appLogo);
        holder.appTitle.setText(myList.getApp_name());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView appLogo;
        private TextView appTitle;

        private ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            appLogo = (ImageView) itemView.findViewById(R.id.moreapps_record_image);
            appTitle = (TextView) itemView.findViewById(R.id.moreapps_record_app_title);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }
}
