package com.saes.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    String receiverId, receiverName, senderRoom, receiverRoom;
    String senderId, senderName;
    DatabaseReference dbReferenceSender, dbReferenceReceiver, userReference;
    ImageView sendBtn;
    EditText messageText;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        ChatApp.setAppInForeground(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatApp.setAppInForeground(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userReference = FirebaseDatabase.getInstance().getReference("users");
        receiverId = getIntent().getStringExtra("id");
        receiverName = getIntent().getStringExtra("name");

        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "ID de usuario no válido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        senderRoom = currentUserId + receiverId;
        receiverRoom = receiverId + currentUserId;

        getSupportActionBar().setTitle(receiverName);
        if (receiverId != null)
        {
            senderRoom = FirebaseAuth.getInstance().getUid() + receiverId;
            receiverRoom = receiverId + FirebaseAuth.getInstance().getUid();
        }
        sendBtn = findViewById(R.id.sendMessageIcon);
        messageAdapter = new MessageAdapter(this);
        recyclerView = findViewById(R.id.chatRecycler);
        messageText = findViewById(R.id.messageEdit);

        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbReferenceSender = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom);
        dbReferenceReceiver = FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom);

        dbReferenceSender.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MessageModel> messages = new ArrayList<>();
                for (DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    if (messageModel != null && messageModel.getMessageId() != null) {
                        if (messageModel.getTimestamp() == 0) {
                            messageModel.setTimestamp(System.currentTimeMillis() - (messages.size() * 1000));
                        }
                        messages.add(messageModel);
                    }
                }

                Collections.sort(messages, new Comparator<MessageModel>() {
                    @Override
                    public int compare(MessageModel m1, MessageModel m2) {
                        return Long.compare(m1.getTimestamp(), m2.getTimestamp());
                    }
                });

                messageAdapter.clear();
                for (MessageModel message: messages)
                {
                    messageAdapter.add(message);
                }

                if (!messages.isEmpty()) {
                    recyclerView.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageText.getText().toString();
                if (message.trim().length() > 0)
                {
                    SendMessage(message);
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "Escriba un mensaje", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void SendMessage(String message) {
        if (message.trim().isEmpty()) {
            return;
        }

        String messageId = UUID.randomUUID().toString();
        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        MessageModel messageModel = new MessageModel(messageId, currentUserId, message);
        messageText.setText("");

        dbReferenceSender.child(messageId).setValue(messageModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("ChatActivity", "Mensaje enviado exitosamente");

                        NotificationHelper.sendNotification(
                                receiverId,
                                message,
                                getSupportActionBar().getTitle().toString(),
                                currentUserId
                        );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                    }
                });
        dbReferenceReceiver.child(messageId).setValue(messageModel)
                .addOnFailureListener(e -> {
                    Log.e("ChatActivity", "Error al enviar mensaje al receptor: " + e.getMessage());
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChatActivity.this,SigninActivity.class));
            finish();
            return true;
        }
        return false;
    }
}