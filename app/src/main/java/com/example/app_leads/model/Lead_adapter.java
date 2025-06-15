package com.example.app_leads.model;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_leads.R;

import java.util.List;

public class Lead_adapter extends RecyclerView.Adapter<Lead_adapter.VH> {
    /**
     * Listener para clic en un lead
     */
    public interface OnLeadClickListener {
        void onLeadClick(Lead lead);
    }

    private final List<Lead> lista;
    private final @LayoutRes int layoutRes;
    private final OnLeadClickListener listener;

    /**
     * Constructor sin listener (para uso genérico)
     */
    public Lead_adapter(List<Lead> lista, @LayoutRes int layoutRes) {
        this(lista, layoutRes, null);
    }

    /**
     * Constructor con listener para clics
     */
    public Lead_adapter(List<Lead> lista, @LayoutRes int layoutRes, OnLeadClickListener listener) {
        this.lista     = lista;
        this.layoutRes = layoutRes;
        this.listener  = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Lead lead = lista.get(position);

        // 1) ID
        holder.tvId.setText("ID: " + lead.getId());

        // 2) Estado y color
        String estado = lead.getEstado();
        holder.tvEstado.setText(estado);
        if ("Pendiente".equalsIgnoreCase(estado)) {
            holder.tvEstado.setTextColor(Color.RED);
        } else {
            holder.tvEstado.setTextColor(Color.parseColor("#008577"));
        }

        // 3) Empresa
        holder.tvEmpresa.setText(lead.getEmpresa());

        // 4) Ejecutivo (solo existe en el layout de subgerente)
        if (holder.tvEjecutivo != null) {
            holder.tvEjecutivo.setText(lead.getEjecutivoName());
        }

        // 5) Fecha
        holder.tvFechaRegistro.setText(lead.getFechaRegistro());

        // 6) Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLeadClick(lead);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvId;
        TextView tvEstado;
        TextView tvEmpresa;
        TextView tvEjecutivo;      // null en layouts sin este TextView
        TextView tvFechaRegistro;

        VH(View itemView) {
            super(itemView);
            tvId            = itemView.findViewById(R.id.tv_id);
            tvEstado        = itemView.findViewById(R.id.tv_estado);
            tvEmpresa       = itemView.findViewById(R.id.tv_empresa);
            tvEjecutivo     = itemView.findViewById(R.id.tv_ejecutivo);
            tvFechaRegistro = itemView.findViewById(R.id.tv_fecha_registro);
        }
    }
}
