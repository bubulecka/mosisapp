package com.demo.mosisapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = "MainActivity";
    private static final int RC_MAIN = 124;

    //firebase authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 123; //request code

    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //maybe extend Application, add this there
        //if (!FirebaseApp.getApps(this).isEmpty())
        //    FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //initalize Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mAuthStateListener==null) {
            Log.d(TAG, "onAuthStateChanged: create");
            mAuthStateListener = new FirebaseAuth.AuthStateListener()
            {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    Log.d("MainActivity", "onAuthStateChanged");
                    //check if user is logged in...
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null)
                    {
                        Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
                        MosisApp.getInstance().logoutFlag = false;
                        //Intent go = new Intent(MainActivity.this, MapsActivity.class);
                        Intent go = new Intent(MainActivity.this, BluetoothFriendActivity.class);
                        go.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivityForResult(go, RC_MAIN);
                        //startActivity(go);
                    }
                    else
                    {
                        // user is signed out, show logInFlow
                        Log.d(TAG, "onAuthStateChanged: signed out");
                        Intent login = new Intent(MainActivity.this, LoginActivity.class);
                        login.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivityForResult(login, RC_SIGN_IN);
                    }
                }
            };
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Log.d(TAG, "onActivityResult: RC_SIGN_IN");
            if (resultCode== Activity.RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){ //if user cancels the sign in
                Toast.makeText(this, "Sign in canceled!", Toast.LENGTH_SHORT).show();
                finish(); //so that you can actually leave
            }else Toast.makeText(this, "Well, we returned", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == RC_MAIN) {
            Log.d(TAG, "onActivityResult: RC_MAIN");
            if (MosisApp.getInstance().logoutFlag) {
                FirebaseAuth.getInstance().signOut();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sign_out_menu) {
            //sign out
            mFirebaseAuth.signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override //onPause or onStop?
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override //onResume or onStart?
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
