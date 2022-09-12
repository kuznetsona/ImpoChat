package com.example.improchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ListView messageListView;
    private AwesomeMessageAdapter adapter;
    private ProgressBar progressBar;
    private ImageButton sendImageButton;
    private Button sendMessageButton;
    private EditText messageEditText;

    // создаем константу, чтобы при выборе изображения по этой константе могли вернуться
    private static final int PC_IMAGE_PICKER = 123;

    private FirebaseAuth auth;

    private String userName;
    private String recipientUserId;

    private FirebaseDatabase database;
    private DatabaseReference messagesDataBaseReference;
    private ChildEventListener messageChildEventListener;
    private DatabaseReference usersDataBaseReference;
    private ChildEventListener usersChildEventListener;

    //private DatabaseReference timeDataBaseReference;
    //private ChildEventListener timeChildEventListener;

    private FirebaseStorage storage;
    private StorageReference chatImagesStorageReference;

    private String recipientUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();

        // Принимаем интент, которые делает чат только для двоих. Устанавливаем айди получателя
        Intent intent = getIntent();
        if (intent!= null){
            userName = intent.getStringExtra("userName");
            recipientUserId = intent.getStringExtra("recipientUserId");
            recipientUserName = intent.getStringExtra("recipientUserName");
        }

        setTitle("" + recipientUserName);

        database = FirebaseDatabase.getInstance();
        messagesDataBaseReference = database.getReference().child("message");
        usersDataBaseReference = database.getReference().child("users");
        //timeDataBaseReference = database.getReference().child("time");

        storage = FirebaseStorage.getInstance();
        chatImagesStorageReference = storage.getReference().child("chatImages");

        // инициализировали
        progressBar = findViewById(R.id.progressBar);
        sendImageButton = findViewById(R.id.sendPhotoButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);




        messageListView = findViewById(R.id.messageListView);
        List<AwesomeMessage> awesomeMessages = new ArrayList<>();
        adapter = new AwesomeMessageAdapter(this, R.layout.message_item, awesomeMessages);
        // установили невидимым прогресс бар

        messageListView.setAdapter(adapter);

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sendMessageButton.setEnabled(charSequence.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // сообщения не больше 500 символов
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});

        // создали он клик лиссениры для наших кнопок. Чтобы имплементировать поведение
        // для этого нужно понимать как именно работает поведение каждой вьюшки!!
        sendMessageButton.setOnClickListener((v) -> {
            AwesomeMessage message = new AwesomeMessage();
            message.setText(messageEditText.getText().toString());
            message.setName(userName);
            message.setTime(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()));

            message.setSender(auth.getCurrentUser().getUid());
            message.setRecipient(recipientUserId);

            message.setImageUrl(null);



            // Отправляем нашу запись в файерБэйз
            messagesDataBaseReference.push().setValue(message);
            messageEditText.setText("");


        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // создаем интент для получения контента
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*"); // указываем тип контента (jpeg - изображение)
                // указываем что хотим получить изображение с локальнго хранилища
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                // создали активити, которая выбирает изображение и возвращается по PC_IMAGE_PICKER ?
                startActivityForResult(Intent.createChooser(intent, "Choose an image"),
                        PC_IMAGE_PICKER);
                // мы получаем какой-то резалт, и этот резалт
                // мы должны переопределить в методе онАктивитиРезалт (ниже)


            }
        });

        usersChildEventListener = new ChildEventListener() {
            @Override
            // получаем доступ ко всем пользователям
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);
                //если есть совпадение по id(?) пользователя, то это именно тот пользователь, который нам нужен
                if(user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    userName = user.getName();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        usersDataBaseReference.addChildEventListener(usersChildEventListener);

        messageChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                AwesomeMessage message = snapshot.getValue(AwesomeMessage.class);

                if(message.getSender().equals(auth.getCurrentUser().getUid())
                        && message.getRecipient().equals(recipientUserId)){
                    message.setMine(true);
                    adapter.add(message);
                } else if(message.getRecipient().equals(auth.getCurrentUser().getUid())
                        && message.getSender().equals(recipientUserId) ){
                    message.setMine(false);
                    adapter.add(message);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        messagesDataBaseReference.addChildEventListener(messageChildEventListener);
    }


    // Определяем меню (точки сверху)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatActivity.this, SignInActivity.class));
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // если выбор изображения и адрес прошли успешно, то создаем Uri изображения
        if(requestCode == PC_IMAGE_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            // получаем последний сегмент Uri с помощью метода getLastPathSegment()
            final StorageReference imageReference = chatImagesStorageReference
                    .child(selectedImageUri.getLastPathSegment());

            UploadTask uploadTask = imageReference.putFile(selectedImageUri);

            // Get a download URL
            uploadTask = imageReference.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        // ??????
                        AwesomeMessage message = new AwesomeMessage();
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        message.setSender(auth.getCurrentUser().getUid());
                        message.setRecipient(recipientUserId);
                        messagesDataBaseReference.push().setValue(message);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }
}