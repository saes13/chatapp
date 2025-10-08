package com.saes.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private static final int VIEW_TYPE_SEND = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private Context context;
    private List<MessageModel> messageModelList;

    public MessageAdapter(Context context) {
        this.context = context;
        this.messageModelList = new ArrayList<>();
    }

    public void add(MessageModel messageModel)
    {
        if (messageModel != null) {
            int index = 0;
            for (int i = 0; i < messageModelList.size(); i++) {
                if (messageModel.getTimestamp() < messageModelList.get(i).getTimestamp()) {
                    index = i;
                    break;
                }
                index = i + 1;
            }
            messageModelList.add(index, messageModel);
            notifyItemInserted(index);
        }
    }

    public void clear()
    {
        int size = messageModelList.size();
        messageModelList.clear();
        if (size > 0) {
            notifyItemRangeRemoved(0, size);
        }
    }

    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SEND)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_send,parent, false);
            return new MyViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_received,parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MyViewHolder holder, int position) {
        MessageModel messageModel = messageModelList.get((position));
        if (messageModel == null) return;

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        if (messageModel.getSenderId().equals(currentUserId))
        {
            if (holder.textViewSendMessage != null) {
                holder.textViewSendMessage.setText(messageModel.getMessage());
            }
            if (holder.textViewReceivedMessage != null) {
                holder.textViewReceivedMessage.setVisibility(View.GONE);
            }
        }else{
            if (holder.textViewReceivedMessage != null) {
                holder.textViewReceivedMessage.setText(messageModel.getMessage());
            }
            if (holder.textViewSendMessage != null) {
                holder.textViewSendMessage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    public List<MessageModel> getUserModelList(){
        return messageModelList;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModelList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid()))
        {
            return VIEW_TYPE_SEND;
        }else{
            return  VIEW_TYPE_RECEIVED;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSendMessage, textViewReceivedMessage;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSendMessage = itemView.findViewById(R.id.textViewSendMessage);
            textViewReceivedMessage = itemView.findViewById(R.id.textViewReceivedMessage);
        }
    }
}