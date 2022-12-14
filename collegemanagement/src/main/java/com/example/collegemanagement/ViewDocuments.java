package com.example.collegemanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ViewDocuments extends AppCompatActivity {

    RecyclerDocumentAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Map.Entry<String, String>> list = new ArrayList<>();
    String collection, userType;
    Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_documents);
        Intent intent = getIntent();

        userType = intent.getStringExtra("userType");
        collection = intent.getStringExtra("collection");
        String heading = collection.substring(0, 1).toUpperCase()+collection.substring(1) + " List";
        ((TextView)findViewById(R.id.viewCollection)).setText(heading);

        HashMap<String, Integer> drawables = new HashMap<>();
        drawables.put("student", R.drawable.student);
        drawables.put("teacher", R.drawable.teacher);
        drawables.put("marks", R.drawable.marks);
        drawables.put("subject", R.drawable.subject);
        drawables.put("attendance",R.drawable.attendance);
        //noinspection ConstantConditions
        ((ImageView)findViewById(R.id.viewDocImage)).setImageResource(drawables.get(collection));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        addBtn = findViewById(R.id.button);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(collection.equals("teacher"))
            addBtn.setOnClickListener(v -> {
                Intent i = new Intent(getApplicationContext(), AddUser.class);
                startActivity(i);
            });
        else
            addBtn.setOnClickListener(v->{
                Intent intent1 = new Intent(ViewDocuments.this, AddCollection.class);
                intent1.putExtra("collection", collection);
                startActivity(intent1);
            });

        db.collection(collection)
                .addSnapshotListener((documentSnapshots, e) -> {
                    if (e != null)
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    list.clear();
                    if (documentSnapshots != null) {
                        for (DocumentSnapshot doc: documentSnapshots) {
                            Map<String, Object> data = doc.getData();
                            String item = "";
                            for(Map.Entry<String, Object> entry: Objects.requireNonNull(data).entrySet())
                                item = item.concat(entry.getKey() + ": " + entry.getValue() + "\n");
                            list.add(new AbstractMap.SimpleEntry<>(doc.getId(), item));
                        }
                    }
                    adapter = new RecyclerDocumentAdapter(this, list);
                    recyclerView.setAdapter(adapter);
                });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            switch (direction) {
                case ItemTouchHelper.LEFT:
                    new AlertDialog.Builder(viewHolder.itemView.getContext())
                            .setMessage("Are you sure you want to delete this record?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                int pos = viewHolder.getAdapterPosition();
                                Toast.makeText(ViewDocuments.this, "Deleted:"+ pos, Toast.LENGTH_SHORT).show();
                                db.collection(collection).document(adapter.getAdapterId(pos)).delete().addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext().getApplicationContext(), " Data deleted successfully ", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext().getApplicationContext(), " Error: "+e.getMessage(), Toast.LENGTH_LONG).show());
                                adapter.notifyItemRemoved(pos);
                            })
                            .setNegativeButton("No", (dialog, which) -> adapter.notifyItemChanged(viewHolder.getAdapterPosition()))
                            .create()
                            .show();
                    break;

                case ItemTouchHelper.RIGHT:
                    Intent update = new Intent(getApplicationContext(), UpdateDocument.class);
                    update.putExtra("collection", collection);
                    update.putExtra("docId", adapter.getAdapterId(viewHolder.getAdapterPosition()));
                    startActivity(update);
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    break;
            }
        }
    };
}