// com/example/app_leads/admin/lead_adapter.java
package com.example.app_leads.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app_leads.R;
import com.example.app_leads.model.Lead;
import java.util.List;

public class Lead_adapter extends RecyclerView.Adapter<Lead_adapter.ViewHolder> {
    private final List<Lead> leads;

    public Lead_adapter(List<Lead> leads) {
        this.leads = leads;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lead, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        Lead lead = leads.get(pos);
        h.tvId.setText("ID: " + lead.getId());
        h.tvRuc.setText("RUC: " + lead.getRuc());
        h.tvEmpresa.setText(lead.getEmpresa());
    }

    @Override
    public int getItemCount() {
        return leads.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvRuc, tvEmpresa;
        ViewHolder(View itemView) {
            super(itemView);
            tvId      = itemView.findViewById(R.id.tv_lead_id);
            tvRuc     = itemView.findViewById(R.id.tv_lead_ruc);
            tvEmpresa = itemView.findViewById(R.id.tv_lead_empresa);
        }
    }
}
