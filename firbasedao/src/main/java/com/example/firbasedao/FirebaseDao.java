package com.example.firbasedao;

import androidx.annotation.NonNull;

import com.example.firbasedao.Listeners.RetrievalEventListener;
import com.example.firbasedao.Listeners.TaskListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public abstract class FirebaseDao<T> {
    protected static final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
    protected String tableName;
    public FirebaseDao(String tableName)
    {
        this.tableName = tableName;
    }

    public void get(String id, final RetrievalEventListener<T> retrievalEventListener) {
        DatabaseReference rowReference = dbReference.child(tableName).child(id);
        Query query = rowReference;
        rowReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parseDataSnapshot(dataSnapshot, new RetrievalEventListener<T>() {
                    @Override
                    public void OnDataRetrieved(T t) {
                        retrievalEventListener.OnDataRetrieved(t);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public String GetNewKey()
    {
        return dbReference.child(tableName).push().getKey();
    }

    protected abstract void parseDataSnapshot(DataSnapshot dataSnapshot, RetrievalEventListener<T> retrievalEventListener);

    public void getAll(final RetrievalEventListener<List<T>> retrievalEventListener){
        DatabaseReference rowReference = dbReference.child(tableName);
        rowReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<T> list = new ArrayList<T>();
                final long len = dataSnapshot.getChildrenCount();
                if(len == 0)
                {
                    retrievalEventListener.OnDataRetrieved(list);
                    return;
                }
                RetrievalEventListener<T> listRetrievalEventListener = new RetrievalEventListener<T>() {
                    @Override
                    public void OnDataRetrieved(T t) {
                        list.add(t);
                        if(list.size() == len){
                            retrievalEventListener.OnDataRetrieved(list);
                        }
                    }
                };
                for(DataSnapshot currentDataSnapshot : dataSnapshot.getChildren())
                    parseDataSnapshot(currentDataSnapshot, listRetrievalEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void save(T t, String id, final TaskListener taskListener){
        Task<Void> task = dbReference.child(tableName).child(id).setValue(t);
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                taskListener.OnSuccess();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                taskListener.OnFail();
            }
        });
    }

    public void delete(String id, TaskListener taskListener){
        save(null, id, taskListener);
    }
}

