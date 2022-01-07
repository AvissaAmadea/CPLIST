package com.avissa.cplist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class DetailKontak extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;
    //untuk menyimpan gambar diletakkan di storage
    StorageReference storageReference;

    ImageView FotoKontak;
    EditText textNama, textTelepon, textSosmed, textAlamat;
    Button buttonEdit, buttonBack, buttonDelete;
    ProgressBar progressBar;

    Uri filePath;
    String fotoUrl, teleponId;

    static final int IMAGE_REQUEST = 1; //variabel yg digunakan untuk membuka foto di galeri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kontak);

        //untuk icon bagian atas
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.logo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Deklarasi variabel
        FotoKontak = findViewById(R.id.foto);
        textNama = findViewById(R.id.inputNama);
        textTelepon = findViewById(R.id.inputTelepon);
        textSosmed = findViewById(R.id.inputSosmed);
        textAlamat = findViewById(R.id.inputAlamat);

        buttonEdit = findViewById(R.id.buttonUpdate);
        buttonBack = findViewById(R.id.buttonKembali);
        buttonDelete = findViewById(R.id.buttonHapus);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        teleponId = getIntent().getExtras().getString("telepon");

        readData();

        FotoKontak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ambilGambar();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hapusData();
            }
        });

        //fungsi button back
        buttonBack.setOnClickListener((v) -> { finish(); });

    }

    private void readData() {
        firebaseFirestore.collection("Contacts").whereEqualTo("telepon", teleponId).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                textNama.setText(document.getString("nama"));
                                textTelepon.setText(document.getString("telepon"));
                                textSosmed.setText(document.getString("sosmed"));
                                textAlamat.setText(document.getString("alamat"));
                                fotoUrl = document.getString("foto");

                                if (fotoUrl != null) {
                                    Picasso.get().load(fotoUrl).fit().into(FotoKontak);
                                }
                                else {
                                    Picasso.get().load(R.drawable.person).fit().into(FotoKontak);
                                }
                            }
                        }
                    }
                });
    }

    private void uploadImage() {
        if (filePath != null) {
            final StorageReference ref = storageReference.child(textTelepon.getText().toString());
            UploadTask uploadTask = ref.putFile(filePath);

            Task<Uri> uriTask = uploadTask.continueWithTask((task) -> {
                return ref.getDownloadUrl();
            }).addOnCompleteListener((task) -> {
                Uri imagePath = task.getResult();

                fotoUrl = imagePath.toString();
                simpanData(textNama.getText().toString(),
                        textTelepon.getText().toString(),
                        textSosmed.getText().toString(),
                        textAlamat.getText().toString(),
                        fotoUrl);

                Toast.makeText(DetailKontak.this, "Data Berhasil disimpan", Toast.LENGTH_SHORT).show();
                finish();
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.VISIBLE);
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int)progress);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(DetailKontak.this, "Gagal menyimpan data" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        else {
            simpanData(textNama.getText().toString(),
                    textTelepon.getText().toString(),
                    textSosmed.getText().toString(),
                    textAlamat.getText().toString(),
                    fotoUrl);
            Toast.makeText(this, "Kontak telah diupdate", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    //method untuk simpan data kontak
    private void simpanData(String nama, String telepon, String sosmed, String alamat, String foto) { //membuat parameter
        //Map = yg digunakan untuk menyimpan fields dalam 1 objek
        Map<String, Object> kontakData = new HashMap<>();

        kontakData.put("nama", nama);
        kontakData.put("telepon", telepon);
        kontakData.put("sosmed", sosmed);
        kontakData.put("alamat", alamat);
        kontakData.put("foto", foto);

        //fungsi untuk menyimpan data dalam database
        firebaseFirestore.collection("Contacts").document(telepon).set(kontakData).isSuccessful(); //document(telepon) = nomot telepon digunakan untuk identitas data //set(kontakData) = yg disimpan data dalam kontakData

    }

    //method untuk ambil foto
    private void ambilGambar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), IMAGE_REQUEST);
    }

    //method untuk memindahkan gambar dari galleri ke aplikasi
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            Picasso.get().load(filePath).fit().into(FotoKontak);
        }
        else {
            Toast.makeText(this, "Tidak ada gambar yang dipilih", Toast.LENGTH_SHORT).show();
        }
    }

    private void hapusData() {
        firebaseFirestore.collection("Contacts").document(teleponId).delete();
        storageReference.child(teleponId).delete();

        Toast.makeText(this, "Kontak telah dihapus", Toast.LENGTH_SHORT).show();
        finish();
    }

}