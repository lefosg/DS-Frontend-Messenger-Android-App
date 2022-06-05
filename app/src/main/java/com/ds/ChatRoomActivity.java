package com.ds;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity {

    String topicName;
    ImageButton sendMsgBtn, galleryBtn;
    MultimediaFile topicIconMMF, MMFToSend;
    CircularImageView activityChatIcon;
    EditText sendMsgTxt;
    Client client;

    MessageAdapter messageAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Intent intent = getIntent();
        topicName = intent.getStringExtra("topicName");

        Toolbar toolbar = findViewById(R.id.chat_room_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(topicName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endChatSession();
            }
        });

        recyclerView = findViewById(R.id.chat_room_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        sendMsgBtn = findViewById(R.id.chat_room_msgBtn);
        sendMsgTxt = findViewById(R.id.chat_room_msgTxt);
        galleryBtn = findViewById(R.id.chat_room_mediaBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new InitChatTask().execute(topicName);

        client = ((MyApp)getApplication()).getClient();

        activityChatIcon = findViewById(R.id.chat_room_title_image);
        topicIconMMF = client.getProfile().getSubbedTopicsImages().get(client.getSubbedTopics().indexOf(topicName));
        Icon icon = Icon.createWithData(topicIconMMF.getMultimediaFileChunk(), 0, (int)topicIconMMF.getLength());
        activityChatIcon.setImageIcon(icon);

        messageAdapter = new MessageAdapter(getApplicationContext(), client.getChatMessages(), client.getUsername());
        recyclerView.setAdapter(messageAdapter);

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = sendMsgTxt.getText().toString();

                if (!message.equals("") && !message.trim().equals("")){
                    new SendMessageTask().execute(new Value(message));
                }
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
                bitmap.copyPixelsToBuffer(byteBuffer);
                MultimediaFile imageToSend = new MultimediaFile(byteBuffer.array(), getFileName(selectedImage) ,client.getUsername());
                MMFToSend = imageToSend;
                System.out.println(MMFToSend);
                System.out.println(MMFToSend);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        endChatSession();
    }

    //Async Tasks
    private class InitChatTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            client.push(new Value("MESSAGE"));
            client.push(new Value(strings[0]));  //push topicName
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            messageAdapter.notifyDataSetChanged();
        }
    }

    private class QuitTopicTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Value v = new Value("quit_topic");
            v.setCommand(true);
            client.push(v);
            return null;
        }
    }

    private class SendMessageTask extends AsyncTask<Value, Void, Void> {
        @Override
        protected Void doInBackground(Value... values) {
            Value msg = values[0];
            client.push(msg);
            client.addMsgToChat(msg);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            sendMsgTxt.setText("");
            messageAdapter.notifyDataSetChanged();
        }
    }

    //methods
    private void endChatSession() {
        System.out.println(client.getChatMessages());
        client.setChatMessages(new ArrayList<>());
        new QuitTopicTask().execute();
        finish();
    }


}