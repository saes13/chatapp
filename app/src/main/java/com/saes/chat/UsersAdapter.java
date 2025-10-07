package com.saes.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {

    private Context context;
    private List<UserModel> userModelList;

    public UsersAdapter(Context context) {
        this.context = context;
        this.userModelList = new ArrayList<>();
    }

    public void add(UserModel userModel) {
        userModelList.add(userModel);
        notifyItemInserted(userModelList.size() - 1);
    }

    public void clear() {
        int size = userModelList.size();
        userModelList.clear();
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserModel userModel = userModelList.get(position);
        if (userModel == null) return;

        holder.name.setText(userModel.getUserName());
        holder.email.setText(userModel.getUserEmail());

        final UserModel selectedUser = userModel;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedUser.getUserID() == null) {
                    android.util.Log.e("AdapterDebug", "ERROR: userID null – no se lanza Chat");
                    return;  // Evita Intent con null
                }

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("id", selectedUser.getUserID());  // Ahora usa selectedUser (final, correcto)
                intent.putExtra("name", selectedUser.getUserName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public List<UserModel> getUserModelList() {
        return new ArrayList<>(userModelList);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name, email;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.useremail);
        }
    }
}