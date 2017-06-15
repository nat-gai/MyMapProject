package com.nat.mymapproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WriteInfoActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    String key;
    double latitude;
    double longitude;

    private EditText titleText;
    private EditText descText;
    private EditText addressText;

    private RadioButton rbtnPrivate;
    private RadioButton rbtnPublic;
    private RadioGroup groupAccess;

    private int accessCode;
    private int writeDataCode;
    private String userID;
    private String userName;
    private String address;

    private List<String> dataImg = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = FirebaseDatabase.getInstance();
        //myRef = database.getReference("map/public");

        titleText = (EditText)findViewById(R.id.descTitle);
        descText = (EditText)findViewById(R.id.descText);
        addressText = (EditText)findViewById(R.id.address);

        rbtnPrivate = (RadioButton)findViewById(R.id.rbtnPrivate);
        rbtnPublic = (RadioButton)findViewById(R.id.rbtnPublic);
        groupAccess = (RadioGroup)findViewById(R.id.groupAccess);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        accessCode = intent.getIntExtra("accessCode", 0);
        userID = intent.getStringExtra("userID");

        String mm = String.valueOf(longitude);
        String mm1 = String.valueOf(latitude);
        mm = mm.replace('.', '-');
        mm1 = mm1.replace('.', '-');

        key = mm + mm1;

        if(accessCode == MapsActivity.PRIVATE){
            myRef = database.getReference("map/private" + "/" + userID);
            //access.setText("private");
            groupAccess.setVisibility(View.VISIBLE);
            rbtnPrivate.setChecked(true);
            writeDataCode = 1;
        } else {
            myRef = database.getReference("map/public");
            //if ()
            groupAccess.setVisibility(View.INVISIBLE);

            //access.setText("public");
        }

        groupAccess.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case (R.id.rbtnPrivate):
                        //Toast.makeText(getApplicationContext(), "private", Toast.LENGTH_SHORT).show();
                        writeDataCode = MapsActivity.PRIVATE;
                        break;
                    case (R.id.rbtnPublic):
                        //Toast.makeText(getApplicationContext(), "public", Toast.LENGTH_SHORT).show();
                        writeDataCode = MapsActivity.PUBLIC;
                        break;
                    default:
                        //Toast.makeText(getApplicationContext(), "Default switch int group ration button", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final ValueEventListener addNewInfo = new ValueEventListener() {

        //myRef.child(key).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DataSnapshot dataInfo = dataSnapshot.child(key);

                for (DataSnapshot child: dataInfo.getChildren()){
                    String mkey = child.getKey();
                    String mval = child.getValue().toString();

                    switch (mkey) {
                        case ("text"):
                            descText.setText(mval);
                            //placeTitle.setText(mval);

                            break;
                        case ("title"):
                            titleText.setText(mval);
                            //placeDesc.setText(mval);
                            break;

                        case ("user_name"):
                            userName = mval;
                            break;
                        case ("address"):
                            addressText.setText(mval);
                            break;

                        default:

                            break;
                    }

                    //mkey = mkey + "1";
                    //Log.e("Key", key);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        myRef.addValueEventListener(addNewInfo);
        myRef.child(key).child("image").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String mkey = child.getKey();
                    String mval = child.getValue().toString();

                    //dataImg.add(mval);

                    if(dataImg.size() != 0){
                        boolean flag = true;

                        for(String strImg : dataImg){
                            if (strImg == mval){
                                flag = false;
                                break;
                            }
                        }

                        if (flag){
                            dataImg.add(mval);
                        }


                    } else {
                        dataImg.add(mval);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //myRef = database.getReference("map/public");

                if(writeDataCode == MapsActivity.PRIVATE){
                    myRef = database.getReference("map/private" + "/" + userID);

                    DatabaseReference upvotesRef = myRef;

                    upvotesRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {

                            myRef.child(key).child("text").setValue(descText.getText().toString());
                            myRef.child(key).child("title").setValue(titleText.getText().toString());

                            return Transaction.success(mutableData);

                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                            System.out.println("Transaction completed");
                        }
                    });


                } else if (writeDataCode == MapsActivity.PUBLIC){

                    myRef = database.getReference("map/public");
                    //InfoActivity.myRef = myRef;

                    DatabaseReference upvotesRef = myRef;

                    upvotesRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {

                            int i = 0;
                            myRef.child(key).child("latitude").setValue(latitude);
                            myRef.child(key).child("longitude").setValue(longitude);
                            myRef.child(key).child("text").setValue(descText.getText().toString());
                            myRef.child(key).child("title").setValue(titleText.getText().toString());
                            myRef.child(key).child("user_name").setValue(userName);
                            myRef.child(key).child("address").setValue(addressText.getText().toString());
                            myRef.child(key).child("image").setValue(dataImg);


                            myRef = database.getReference("map/private" + "/" + userID);
                            myRef.child(key).removeValue();
                            return Transaction.success(mutableData);

                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                            System.out.println("Transaction completed");
                        }
                    });

                    //myRef.addValueEventListener(addNewInfo);

                    //access.setText("public");
                } else {

                    /*myRef = database.getReference("map/public");

                    myRef.child(key).child("latitude").setValue(latitude);
                    myRef.child(key).child("longitude").setValue(longitude);
                    myRef.child(key).child("text").setValue(descText.getText().toString());
                    myRef.child(key).child("title").setValue(titleText.getText().toString());*/

                }

                Intent intent = new Intent();
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                //intent.putExtra("writeCode", writeDataCode);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }



}
