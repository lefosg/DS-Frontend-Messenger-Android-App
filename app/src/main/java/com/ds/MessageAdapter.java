package com.ds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static  final int MSG_TYPE_LEFT = 0;
    public static  final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Value> mChat;
    private String username;


    public MessageAdapter(Context mContext, List<Value> mChat, String username){
        this.mContext = mContext;
        this.mChat = mChat;
        this.username = username;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Value chat = mChat.get(position);
        if (chat.getMessage() != null){
            holder.show_message.setText(chat.getMessage());
            holder.txt_seen.setText(chat.getSender());
        } else if (chat.getMultiMediaFile() != null) {
            holder.show_message.setVisibility(View.INVISIBLE);
            MultimediaFile media = chat.getMultiMediaFile();
            Bitmap bitmap = BitmapFactory.decodeByteArray(media.getMultimediaFileChunk(), 0, (int)media.getLength());
            holder.mmf_image.setImageBitmap(bitmap);
            holder.mmf_image.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public ImageView profile_image, mmf_image;
        public TextView txt_seen;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            mmf_image = itemView.findViewById(R.id.chat_img);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mChat.get(position).getSender().equals(username)){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}