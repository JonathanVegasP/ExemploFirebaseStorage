package com.example.familia.exemplofirebasestorage.helper;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.example.familia.exemplofirebasestorage.R;

public class Alerta {
    private static Alerta instance;
    private Runnable pos = null, neg = null;

    private Alerta() {
    }

    public static synchronized Alerta getInstance(){
        if (instance == null){
            instance = new Alerta();
        }
        return instance;
    }

    public AlertDialog confirmar(Context context, String titulo, String mensagem, String pb, Runnable pos, String nb, Runnable neg){
        this.pos = pos;
        this.neg = neg;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(titulo);
        builder.setMessage(mensagem);
        builder.setPositiveButton(pb, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Alerta.this.pos.run();
            }
        });
        builder.setNegativeButton(nb, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Alerta.this.neg.run();
            }
        });
        builder.setCancelable(false);
        return builder.create();
    }
}
