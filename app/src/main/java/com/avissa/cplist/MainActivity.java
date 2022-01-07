package com.avissa.cplist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;

    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //untuk icon bagian atas
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.logo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Deklarasi variabel
        recyclerView = findViewById(R.id.recyclerView);

        progressBar = findViewById(R.id.progressBar);

        firebaseFirestore = FirebaseFirestore.getInstance();

        //Deklarasi layout
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        getData();

        //Deklarasi komponen yang digunakan di desain main activity
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener((v) -> {
            startActivity(new Intent(MainActivity.this, TambahKontak.class)); //ketika button fab diklik maka pindah ke tampilan TambahKontak
        });

    }

    private void getData() {

        Query query = firebaseFirestore.collection("Contacts");

        FirestoreRecyclerOptions<KontakClass> response = new FirestoreRecyclerOptions.Builder<KontakClass>()
                .setQuery(query, KontakClass.class).build();

        //memberikan value pada adapter
        adapter = new FirestoreRecyclerAdapter<KontakClass, ContactsHolder>(response) {

            @NonNull
            @Override
            public ContactsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kontak,parent, false);

                return new ContactsHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ContactsHolder holder, int position, @NonNull final KontakClass model) {

                progressBar.setVisibility(View.GONE);

                //mengambil data dari item lalu ditampikan di menu awal
                if (model.getFoto() != null) {
                    Picasso.get().load(model.getFoto()).fit().into(holder.fotoKontak);
                }
                else {
                    Picasso.get().load(R.drawable.person).fit().into(holder.fotoKontak);
                }

                holder.namaKontak.setText(model.getNama());
                holder.teleponKontak.setText(model.getTelepon());

                //method jika per item/list diklik maka akan pindah ke tampilan detail kontak
                holder.itemView.setOnClickListener((v) -> {

                        Intent intent = new Intent(MainActivity.this, DetailKontak.class);
                        intent.putExtra("telepon", model.getTelepon());
                        startActivity(intent);

                });
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                Log.e("Ditemukan Error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    public static class ContactsHolder extends RecyclerView.ViewHolder{

        CircleImageView fotoKontak;
        TextView namaKontak, teleponKontak;
        ConstraintLayout constraintLayout;

        public ContactsHolder(@NonNull View itemView) {
            super(itemView);
            //pemberian nilai(value) dari data diatas
            fotoKontak = itemView.findViewById(R.id.imageViewFoto);
            namaKontak = itemView.findViewById(R.id.textViewNama);
            teleponKontak = itemView.findViewById(R.id.textViewTelepon);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}