package com.example.familia.exemplofirebasestorage.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.familia.exemplofirebasestorage.R;
import com.example.familia.exemplofirebasestorage.config.ConfiguracaoFirebase;
import com.example.familia.exemplofirebasestorage.helper.Alerta;
import com.example.familia.exemplofirebasestorage.helper.Permissoes;
import com.example.familia.exemplofirebasestorage.model.Usuario;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button mostrarImagem;
    private ImageView imageView;

    public static final int SELECAO_GALERIA = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        componentes();
        eventos();
        carregarFoto();
    }

    private void componentes(){
        auth = ConfiguracaoFirebase.getAuth();
        mostrarImagem = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissoes = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
            Permissoes solicitarPermissoes = new Permissoes();
            solicitarPermissoes.validarPermissoes(permissoes,MainActivity.this,1);
        }
    }

    private void eventos(){
        mostrarImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarImagem();
            }
        });
    }

    private void salvarImagem(){
        //Para realizar uma alteração no banco de dados é necessário estar logado!
        auth.signInWithEmailAndPassword("jopxoto12@gmail.com","123456").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Logado!",Toast.LENGTH_SHORT).show();
                }else {
                    String ex = "";
                    try{
                        if (task.getException() != null)
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        ex = "Este usuário não está cadastrado";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        ex = "E-mail e senha estão inválidos";
                    }catch (Exception e){
                        ex = "Error ao logar: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this,ex,Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Abrir a galeria de fotos
        Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(i.resolveActivity(getPackageManager()) != null){
            startActivityForResult(i,SELECAO_GALERIA);
        }
    }

    //Salvar a imagem da galeria para o Storage e atualizar a imagem no imageView com base no resultado do intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bitmap imagem = null;
            try {
                switch (requestCode){
                    //Se o resultado da intent for para a galeria
                    case SELECAO_GALERIA:
                        //Gera uma uri com base no local da imagem do proprio aparelho e salva a imagem no objeto Bitmap
                        if(data != null) {
                            Uri localImagemSelecionada = data.getData();
                            imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagemSelecionada);
                        }
                        break;
                }
                //Se o objeto Bitmap não estiver vazio irá atualizar a imagem e irá salvar ela no storage
                if(imagem != null){
                    imageView.setImageBitmap(imagem);
                    //Formata a imagem para poder salvar no storage
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] dadosImagem = baos.toByteArray();
                    //Salvar a imagem no storage
                    if(auth.getCurrentUser() != null) {
                        final String idUsuario = auth.getCurrentUser().getUid();
                        if (!idUsuario.isEmpty()) {
                            final String nomeImagem = UUID.randomUUID().toString();
                            final StorageReference storage = ConfiguracaoFirebase.getStorage()
                                    .child("imagens/users")
                                    .child(idUsuario)
                                    .child(nomeImagem);
                            UploadTask uploadTask = storage.putBytes(dadosImagem);
                            //Tratamento de error
                            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if(!task.isSuccessful()){
                                        if(task.getException() != null)
                                        throw task.getException();
                                    }
                                    return storage.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Uri uri = task.getResult();
                                        salvarFoto(uri,idUsuario,nomeImagem);
                                    }else{
                                        Toast.makeText(MainActivity.this,"Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void salvarFoto(Uri uri,String idUsuario,String idFoto){
        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        usuario.setId_foto(idFoto);
        if(auth.getCurrentUser() != null)
        usuario.setNome(auth.getCurrentUser().getDisplayName());
        usuario.setUrl_foto(uri.toString());
        usuario.salvar();
    }

    private void carregarFoto(){
        //Recupera o usuário atual
        FirebaseUser user = auth.getCurrentUser();
        if(user != null){
            //Carrega a imagem salva
            Uri uri = user.getPhotoUrl();
            if(uri != null){
                imageView.setImageURI(uri);
                //Se não salvar uma imagem, ficará uma imagem padrão para não dar error
            }else{
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int resultado : grantResults){
            if(resultado == PackageManager.PERMISSION_DENIED){
                validarPermissoes();
            }
        }
    }

    private void validarPermissoes(){
        AlertDialog alertDialog = Alerta.getInstance().confirmar(
                this,
                "Permissões negadas",
                "É necessário aceitar as permissões para utilizar o aplicativo",
                "Confirmar",
                new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },
                null,
                null);
        alertDialog.show();
    }
}
