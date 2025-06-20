package com.example.app_leads.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_leads.R;
import com.example.app_leads.model.LeadStats;

import java.util.List;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.VH> {

    private final List<LeadStats> items;

    public StatsAdapter(List<LeadStats> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stats, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH vh, int position) {
        LeadStats s = items.get(position);
        vh.tvFecha.setText(s.getFecha());
        vh.tvEjecutivo.setText("Ejecutivo: " + s.getEjecutivo());
        vh.tvSubgerente.setText("Subgerente: " + s.getSubgerente());

        int total = s.getTotal();
        int atendidos = s.getAtendidos();
        int progreso = (total > 0) ? (atendidos * 100 / total) : 0;

        vh.progressBar.setProgress(progreso);
        vh.tvResumen.setText("Pendientes: " + s.getPendientes() +
                " / Atendidos: " + atendidos +
                " / Total: " + total);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvFecha, tvEjecutivo, tvSubgerente, tvResumen;
        ProgressBar progressBar;

        VH(@NonNull View v) {
            super(v);
            tvFecha = v.findViewById(R.id.tv_stat_fecha);
            tvEjecutivo = v.findViewById(R.id.tv_stat_ejecutivo);
            tvSubgerente = v.findViewById(R.id.tv_stat_subgerente);
            progressBar = v.findViewById(R.id.bar_stat);
            tvResumen = v.findViewById(R.id.tv_stat_resumen);
        }
    }
}
