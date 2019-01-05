package com.example.familia.exemplofirebasestorage.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class Permissoes {

    public void validarPermissoes(String[] permissoes, Activity activity, int requestCode){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            List<String> listPermissoes = new ArrayList<>();
            for(String permissao : permissoes){
                if(ContextCompat.checkSelfPermission(activity,permissao) == PackageManager.PERMISSION_DENIED){
                    listPermissoes.add(permissao);
                }
            }
            if(listPermissoes.size() > 0){
                String[] solicitarPermissoes = new String[listPermissoes.size()];
                listPermissoes.toArray(solicitarPermissoes);
                ActivityCompat.requestPermissions(activity,solicitarPermissoes,requestCode);
            }
        }
    }

}
