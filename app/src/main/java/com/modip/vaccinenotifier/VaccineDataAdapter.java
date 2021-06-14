package com.modip.vaccinenotifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VaccineDataAdapter extends RecyclerView.Adapter<VaccineDataAdapter.ViewHolder> {
    private Context context;
    private List<VaccineDataModel> list;
    public VaccineDataAdapter(List<VaccineDataModel> list, Context mCtx) {
        this.list = list;
        this.context = mCtx;
    }

    @NonNull
    @Override
    public VaccineDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vaccinedata_single_item, parent, false);
        return new VaccineDataAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VaccineDataAdapter.ViewHolder holder, final int position) {
        final VaccineDataModel myList = list.get(position);
        holder.tv_centerName.setText(myList.name);
        holder.tv_vaccineAvailability.setText("Available Slots: "+myList.availableCapacity);
        holder.tv_ageGroup.setText("Age Limit: "+myList.ageLimit);
        holder.tv_vaccineName.setText("Vacccine: "+myList.vaccineName);
        holder.tv_dose1Capacity.setText("Dose 1 Slots: "+myList.dose1Capacity);
        holder.tv_dose2Capacity.setText("Dose 2 Slots: "+myList.dose2Capacity);
        holder.tv_pin.setText("Pincode: "+myList.pincode);
        holder.tv_date.setText("Date: "+myList.date);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tv_centerName;
        TextView tv_vaccineAvailability;
        TextView tv_ageGroup;
        TextView tv_vaccineName;
        TextView tv_dose1Capacity;
        TextView tv_dose2Capacity;
        TextView tv_pin;
        TextView tv_date;

        private ViewHolder(View itemView) {
            super(itemView);
            tv_centerName = itemView.findViewById(R.id.center_name);
            tv_vaccineAvailability = itemView.findViewById(R.id.vaccine_availability);
            tv_ageGroup = itemView.findViewById(R.id.age_group);
            tv_dose1Capacity = itemView.findViewById(R.id.dose1_availability);
            tv_dose2Capacity = itemView.findViewById(R.id.dose2_availability);
            tv_vaccineName = itemView.findViewById(R.id.vaccine_name);
            tv_pin = itemView.findViewById(R.id.pincode);
            tv_date = itemView.findViewById(R.id.date);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
