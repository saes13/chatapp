// NotificationHelper.java
package com.saes.chat;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "BCPiaGvctE9Q6sEAv64tEf5wJmUMP7YMEt6hGZ-XJ1G7NYxlMjAyYq-YIbhejGLXv3cxEeV4-gUVafysYiH0Ybs";

    public static void sendNotification(String receiverUserId, String message, String senderName, String senderId) {
        // Obtener el token FCM del receptor
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(receiverUserId)
                .child("fcmToken");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String receiverToken = snapshot.getValue(String.class);
                if (receiverToken != null && !receiverToken.isEmpty()) {
                    sendFCMNotification(receiverToken, message, senderName, senderId);
                } else {
                    Log.d(TAG, "Token FCM no encontrado para el usuario: " + receiverUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error obteniendo token FCM: " + error.getMessage());
            }
        });
    }

    private static void sendFCMNotification(String receiverToken, String message, String senderName, String senderId) {
        try {
            OkHttpClient client = new OkHttpClient();

            Map<String, Object> notificationMap = new HashMap<>();
            notificationMap.put("title", senderName);
            notificationMap.put("body", message);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("title", senderName);
            dataMap.put("message", message);
            dataMap.put("senderId", senderId);
            dataMap.put("senderName", senderName);
            dataMap.put("chatId", senderId);

            Map<String, Object> rootMap = new HashMap<>();
            rootMap.put("to", receiverToken);
            rootMap.put("notification", notificationMap);
            rootMap.put("data", dataMap);
            rootMap.put("priority", "high");

            String json = new com.google.gson.Gson().toJson(rootMap);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    json
            );

            Request request = new Request.Builder()
                    .url(FCM_URL)
                    .post(body)
                    .addHeader("Authorization", "key=" + SERVER_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error enviando notificación FCM: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Notificación enviada exitosamente");
                    } else {
                        Log.e(TAG, "Error en respuesta FCM: " + response.body().string());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error enviando notificación: " + e.getMessage());
        }
    }
}