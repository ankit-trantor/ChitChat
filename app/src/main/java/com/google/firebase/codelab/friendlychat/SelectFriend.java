package com.google.firebase.codelab.friendlychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelectFriend extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabaseReference;
    private ArrayList<String> listOfSomething;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private String mUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mUsername=mFirebaseUser.getDisplayName();


        Button bt = (Button)findViewById(R.id.button);
        assert bt != null;
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {


                EditText edit = (EditText)findViewById(R.id.editText);
                String text = edit.getText().toString();
Boolean found  = false;
                for(String d : listOfSomething){
                    if(d != null && d.contains(text)) {
                        Toast.makeText(getApplicationContext(), "User Found", Toast.LENGTH_LONG).show();
                        found=true;
                        //something here
                    }

                }
                if(!found)
                    Toast.makeText(getApplicationContext(), "User Not Found", Toast.LENGTH_LONG).show();
                    else
                {   SharedPreferences settings;
                    SharedPreferences.Editor editor;
                    settings = getApplicationContext().getSharedPreferences("name", Context.MODE_PRIVATE); //1
                    editor = settings.edit(); //2

                    editor.putString("key", text); //3
                    editor.commit(); //4

                    // Start NewActivity.class
                    Intent myIntent = new Intent(SelectFriend.this,
                            MainActivity.class);
                    startActivity(myIntent);
                }







            }
        });


        FirebaseMessaging.getInstance().subscribeToTopic("test");


        new feedTask().execute();

        readUsers();
    }

    private void readUsers(){

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        listOfSomething = new ArrayList<String>();
        mFirebaseDatabaseReference.child("users").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            // Get user value
                            Users user = child.getValue(Users.class);
                            listOfSomething.add(user.getName());

                        }
                        // ...

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //  Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });


    }



    public class feedTask extends AsyncTask<String,Void,String> {


        public feedTask() {
            super();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                OkHttpClient client = new OkHttpClient();

                RequestBody body = new FormBody.Builder()
                        .add("Token", FirebaseInstanceId.getInstance().getToken())
                        .add("Name", mUsername)
                        .build();

                Request request = new Request.Builder()
                        .url("http://192.168.1.104/chat/register.php")
                        .post(body)
                        .build();


                Response response =  client.newCall(request).execute();
                String result = response.body().string();

                return result;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);



        }


    }




}
