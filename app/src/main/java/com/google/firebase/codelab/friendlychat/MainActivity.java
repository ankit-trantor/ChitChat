/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>
            mFirebaseAdapter;

    private static final String TAG = "MainActivity";
    public  String MESSAGES_CHILD = "messages";
    String FRIENDLY_MSG_LENGTH = "friendly_msg_length";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 20;
    public static final String ANONYMOUS = "anonymous";
    private String mUsername;
    private String mPhotoUrl;
    private String mText;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;

    // Firebase instance variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;
        SharedPreferences settings;

        settings = getApplicationContext().getSharedPreferences("name", Context.MODE_PRIVATE); //1
        mText= settings.getString("key", "messages"); //2
        MESSAGES_CHILD= mText;



        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
    .addApi(Auth.GOOGLE_SIGN_IN_API)
    .build();

    // Initialize ProgressBar and RecyclerView.
    mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
    mLinearLayoutManager = new LinearLayoutManager(this);
    mLinearLayoutManager.setStackFromEnd(true);
    mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage,
            MessageViewHolder>(
    FriendlyMessage.class,
    R.layout.item_message,
    MessageViewHolder.class,
            mFirebaseDatabaseReference.child(MESSAGES_CHILD)) {

        @Override
        protected void populateViewHolder(MessageViewHolder viewHolder,
                FriendlyMessage friendlyMessage, int position) {
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            viewHolder.messageTextView.setText(friendlyMessage.getText());
            viewHolder.messengerTextView.setText(friendlyMessage.getName());
            if (friendlyMessage.getPhotoUrl() == null) {
                viewHolder.messengerImageView
                        .setImageDrawable(ContextCompat
                                .getDrawable(MainActivity.this,
                                        R.drawable.ic_account_circle_black_36dp));
            } else {
                Glide.with(MainActivity.this)
                        .load(friendlyMessage.getPhotoUrl())
                        .into(viewHolder.messengerImageView);

            }
        }
    };

    mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            int friendlyMessageCount = mFirebaseAdapter.getItemCount();
            int lastVisiblePosition =
                    mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

            if (lastVisiblePosition == -1 ||
                    (positionStart >= (friendlyMessageCount - 1) &&
                            lastVisiblePosition == (positionStart - 1))) {
                mMessageRecyclerView.scrollToPosition(positionStart);
            }
        }
    });

    mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
    mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    mMessageEditText = (EditText) findViewById(R.id.messageEditText);
    mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
            .getInt(FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
    mMessageEditText.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString().trim().length() > 0) {
                mSendButton.setEnabled(true);
            } else {
                mSendButton.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    });

    mSendButton = (Button) findViewById(R.id.sendButton);
    mSendButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Send messages on click.
            FriendlyMessage friendlyMessage = new
                    FriendlyMessage(mMessageEditText.getText().toString(),
                    mUsername,
                    mPhotoUrl);
            mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                    .push().setValue(friendlyMessage);
            mMessageEditText.setText("");
            if(!(mFirebaseUser.getDisplayName().equalsIgnoreCase(mText)))
            new feedTask().execute();
        }
    });
}

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
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
                        .add("UserId", mText)
                        .build();

                Request request = new Request.Builder()
                        .url("http://192.168.1.104/chat/push_notification.php")
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
