package com.nat.mymapproject;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class InfoActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    public static DatabaseReference myRef;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference storageRef1;

    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 768;
    int size;

    public static final int ADD_NEW_INFO = 15;

    private Uri imageUrl;
    private Uri imgForLoad;
    private Uri imgForLoad1;

    private ImageView imageView;

    private List<String> dataImg = new ArrayList<>();
    private InfoImageAdapter mInfoImageAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;

    private String titel;
    private String textDesc;

    String key;
    double latitude;
    double longitude;
    String testStr;
    InputStream stream;

    private int accessCode;
    private String userID;

    Uri downloadUrl;

    CharSequence s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton addPhoto = (FloatingActionButton) findViewById(R.id.addPhoto);
        FloatingActionButton addInfo = (FloatingActionButton) findViewById(R.id.addInfo);
        FloatingActionButton delInfo = (FloatingActionButton) findViewById(R.id.delInfo);
        //imageView = (ImageView) findViewById(R.id.imgTest);
        //imageView.fit

        //TabLayout tabLayout = (TabLayout) findViewById(R.id.tabImg);
        //tabLayout.setupWithViewPager(mViewPager, true);


        database = FirebaseDatabase.getInstance();
        //myRef = database.getReference("map/public");

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("my/");
        storageRef1 = storage.getReference("map/public/");


        final TextView placeTitle = (TextView) findViewById(R.id.placeTitle);
        final TextView placeDesc = (TextView) findViewById(R.id.placeDescription);
        final TextView userName = (TextView) findViewById(R.id.userName);
        final TextView placeAddress = (TextView) findViewById(R.id.placeAddress);
        final TextView access = (TextView) findViewById(R.id.access);

        //TextView text = (TextView) findViewById(R.id.textAny);

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

        size = (int) Math.ceil(Math.sqrt(MAX_WIDTH * MAX_HEIGHT));

        if(accessCode == MapsActivity.PRIVATE){
            myRef = database.getReference("map/private" + "/" + userID);
            access.setText("private");
            delInfo.setVisibility(View.VISIBLE);
        } else {
            myRef = database.getReference("map/public");
            access.setText("public");
            //delInfo.setVisibility(View.INVISIBLE);
        }


        ValueEventListener updateInfo = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot dataInfo = dataSnapshot.child(key);

                for (DataSnapshot child: dataInfo.getChildren()){
                    String mkey = child.getKey().toString();
                    String mval = child.getValue().toString();

                    switch (mkey) {
                        case ("text"):
                            //descText.setText(mval);
                            placeDesc.setText(mval);
                            break;
                        case ("title"):
                            //titleText.setText(mval);
                            placeTitle.setText(mval);
                            break;
                        case ("user_name"):
                            userName.setText(mval);
                            break;
                        case ("address"):
                            placeAddress.setText(mval);
                            break;
                        default:

                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(getApplicationContext(), "Faled to read " + databaseError.toException(), Toast.LENGTH_SHORT).show();
            }




        };

        myRef.addValueEventListener(updateInfo);


        myRef.child(key).child("image").addValueEventListener(new ValueEventListener() {
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

                mInfoImageAdapter = new InfoImageAdapter(getSupportFragmentManager(), dataImg);

                mViewPager = (ViewPager) findViewById(R.id.viewPager);
                tabLayout = (TabLayout) findViewById(R.id.tabImg);
                mViewPager.setAdapter(mInfoImageAdapter);
                mViewPager.setOffscreenPageLimit(dataImg.size());
                tabLayout.setupWithViewPager(mViewPager, true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //LatLng latlng1 = new LatLng(latitude, longitude);
        //myRef.child(key).setValue(latlng1);


        delInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myRef.child(key).removeValue();

                Intent intent = new Intent(InfoActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }

        });

        addInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoActivity.this, WriteInfoActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("accessCode", accessCode);
                intent.putExtra("userID", userID);

                startActivityForResult(intent, ADD_NEW_INFO);
            }
        });

        //myRef.child("public").child(key).child("text").setValue(placeTitle.getText().toString());
        //myRef.child("public").child(key).child("title").setValue(placeDesc.getText().toString());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            //photo
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    //
                    int sizeImgByte;
                    Bitmap bitmap = null;

                    Date d = new Date();
                    s  = DateFormat.format("dd-MM-yyyy hh-mm-ss", d.getTime());
                    String str = (s.toString() + ".jpg");

                    try {
                        bitmap = loadBitmapFromUri(selectedImage, mViewPager.getWidth(), mViewPager.getHeight());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int heigntImg= bitmap.getHeight();
                    int wightImg = bitmap.getWidth();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] dataImg = baos.toByteArray();

                    // display metricks

                    UploadTask uploadTask = storageRef.child(str).putBytes(dataImg);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        @SuppressWarnings("VisibleForTests")
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                            imageUrl = taskSnapshot.getDownloadUrl();
                            myRef.child(key).child("image").child(s.toString()).setValue(imageUrl.toString());
                        }
                    });
                }

                break;

            case RESULT_CANCELED:
                if (resultCode == RESULT_OK) {
                    break;
                }

                break;

            case ADD_NEW_INFO:

                if (resultCode == RESULT_OK) {

                    latitude = data.getDoubleExtra("latitude", 0);
                    longitude = data.getDoubleExtra("longitude", 0);

                    String mm = String.valueOf(longitude);
                    String mm1 = String.valueOf(latitude);
                    mm = mm.replace('.', '-');
                    mm1 = mm1.replace('.', '-');

                    key = mm + mm1;
                }
                break;
        }

    }

    private Bitmap loadBitmapFromUri(Uri uri, int width, int height) throws FileNotFoundException {
        // получение дескриптора того ресурса которй хотим вытянуть
        ParcelFileDescriptor bitmapFd = getContentResolver().openFileDescriptor(uri, "r");
        // создание опций
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        // just to get image size
        //заполнение этих опций, из текущего изображения
        BitmapFactory.decodeFileDescriptor(bitmapFd.getFileDescriptor(), null, opts);
        // модификация опций, зная текущие опции пересчёт в другую ширину и высоту
        opts.inSampleSize = calculateInSampleSize(opts, width, height, true);
        opts.inJustDecodeBounds = false;
        // получение изображения с новыми опциями
        Bitmap img = BitmapFactory.decodeFileDescriptor(bitmapFd.getFileDescriptor(), null, opts);


        try {
            bitmapFd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                            int reqHeight, boolean force) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        if (!!!force && inSampleSize > 3) {
            inSampleSize = 3;
        }
        return inSampleSize;
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private List<String> getData() {

        dataImg.add("1");
        return dataImg;
    }



}

/*
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");

        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
*/



/*
    @Override
    protected void onResume() {
        super.onResume();
/*
        final TextView placeTitle = (TextView) findViewById(R.id.descTitle);
        final TextView placeDesc = (TextView) findViewById(R.id.descText);
        final TextView userName = (TextView) findViewById(R.id.userName);
        final TextView placeAddress = (TextView) findViewById(R.id.placeAddress);
        final TextView access = (TextView) findViewById(R.id.access);

        ValueEventListener updateInfo = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot dataInfo = dataSnapshot.child(key);

                for (DataSnapshot child: dataInfo.getChildren()){
                    String mkey = child.getKey().toString();
                    String mval = child.getValue().toString();

                    switch (mkey) {
                        case ("text"):
                            //descText.setText(mval);
                            placeDesc.setText(mval);
                            break;
                        case ("title"):
                            //titleText.setText(mval);
                            placeTitle.setText(mval);
                            break;
                        case ("user_name"):
                            userName.setText(mval);
                            break;
                        case ("address"):
                            placeAddress.setText(mval);
                            break;
                        default:

                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Faled to read " + databaseError.toException(), Toast.LENGTH_SHORT).show();
            }
        };

        myRef.addValueEventListener(updateInfo);

    }
*/








                    /*myRef.child(key).addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot child: dataSnapshot.getChildren()){
                                String mkey = child.getKey();
                                String mval = child.getValue().toString();

                                switch (mkey) {
                                    case ("text"):
                                        //descText.setText(mval);
                                        placeTitle.setText(mval);

                                        break;
                                    case ("title"):
                                        //titleText.setText(mval);
                                        placeDesc.setText(mval);
                                        break;
                                    case ("image"):

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
                    });
                    */











 /* Snackbar mSnackbar = Snackbar.make(v, "I want add information marker", Snackbar.LENGTH_INDEFINITE)
                        .setAction("anything", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        text.setText("Anyting else");
                                    }
                                });*/

    //Snackbar mSnackbar = Snackbar.make(v, "I want add information marker", Snackbar.LENGTH_SHORT)
    //       .setAction("anything", null);

    //mSnackbar.show();



                /*Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                */








/*

        myRef.child(key).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    String mkey = child.getKey();
                    String mval = child.getValue().toString();

                    switch (mkey) {
                        case ("text"):
                            //descText.setText(mval);
                            placeDesc.setText(mval);

                            break;
                        case ("title"):
                            //titleText.setText(mval);
                            placeTitle.setText(mval);
                            break;
                        case ("user_name"):
                            userName.setText(mval);

                        default:

                            break;
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
*/





/*
        //String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard, "test_image.jpg");


        InputStream stream = null;

        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        UploadTask uploadTask = storageRef.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                imageUrl = taskSnapshot.getDownloadUrl();
            }
        });

*/





/*
                    if (mkey.equals("text"))
                    {
                        descText.setText(mval);
                    }

                    if (mkey.equals("title"))
                    {
                        titleText.setText(mval);
                    }
                    if (mkey.equals("image"))
                    {
                        imgForLoad = Uri.parse(mval);
                        Picasso.with(InfoActivity.this)
                                .load(imgForLoad)
                                //.load("https://firebasestorage.googleapis.com/v0/b/mymapproject-d81bb.appspot.com/o/crane.jpg?alt=media&token=259879c3-a348-42c3-9416-9cc8cc61b7cd")
                                .into(imageView);
*/



   /* private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    */



                       /* URL url = null;
                        try {
                            url = new URL("https://firebasestorage.googleapis.com/v0/b/mymapproject-d81bb.appspot.com/o/my%2Fmy.jpg?alt=media&token=1a9a9567-2e56-4a06-9e64-db6308c83810");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        Bitmap bmp = null;
                        try {
                            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        imageView.setImageBitmap(bmp);*/

//new DownloadImageTask((ImageView) findViewById(R.id.imgTest)).execute("https://firebasestorage.googleapis.com/v0/b/mymapproject-d81bb.appspot.com/o/my%2Fmy.jpg?alt=media&token=1a9a9567-2e56-4a06-9e64-db6308c83810");



//StorageReference httpsReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/mymapproject-d81bb.appspot.com/o/my%2Fmy.jpg?alt=media&token=1a9a9567-2e56-4a06-9e64-db6308c83810");
                        /*Glide.with(getApplicationContext())
                                .using(new FirebaseImageLoader())
                                .load(storageRef)
                                .into(imageView);
                        //imageView.setImageURI(imgForLoad);
                        */

/*
        imageView = (ImageView) findViewById(R.id.imgTest);

        // пока не понятно что это, но вроде это для того, чтобы узнать размеры, ещё что то об имедж
        // для того что взять картинку
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();

        // вернёт картинку из имеджа
        Bitmap bitmap = imageView.getDrawingCache();


        // Создали массив из потока байтов
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // узазываем формат и качество (от 0 до 100 лучшее качество) PNG игнорирует настройки качества
        // baos это в какой поток запишутся данные
        // compress это бул которые вернёт тру, вроде как если всё ок
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] data = baos.toByteArray();

        // putBytes сохраняет все содержимое даты байтов в память
        //UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //  что то не загрузилось нифига, тут нужно обрабатывать
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata () содержит метаданные файлов,
                // такие как размер, тип содержимого и URL-адрес загрузки.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

            }
        });
*/

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


//https://stackoverflow.com/questions/23740307/load-large-images-with-picasso-and-custom-transform-object
                            /*Picasso.with(imageView.getContext())
                                    .load(imgForLoad)
                                    //.load("https://firebasestorage.googleapis.com/v0/b/mymapproject-d81bb.appspot.com/o/crane.jpg?alt=media&token=259879c3-a348-42c3-9416-9cc8cc61b7cd")
                                    .transform(new BitmapTransform(MAX_WIDTH, MAX_HEIGHT))
                                    .skipMemoryCache()
                                    .resize(size, size)
                                    .centerInside()
                                    .into(imageView);
                                    */




                            /*Picasso.with(InfoActivity.this)
                                    //.load(imgForLoad)
                                    .load("https://firebasestorage.googleapis.com/v0/b/mymapproject-d81bb.appspot.com/o/crane.jpg?alt=media&token=259879c3-a348-42c3-9416-9cc8cc61b7cd")
                                    //.centerCrop()
                                    //.resize(imageView.getWidth(), imageView.getHeight())
                                    .into(imageView);*/





/*
                LatLng latlng1 = new LatLng(latitude, longitude);
                myRef.child("public").child(key).setValue(latlng1);

                myRef.child("public").child(key).child("text").setValue(descText.getText().toString());
                myRef.child("public").child(key).child("title").setValue(titleText.getText().toString());
                myRef.child("public").child(key).child("image").setValue(imageUrl.toString());
*/




//String mmm = child.child("image").getKey();
//String mmn = child.child("image").getValue().toString();
//testStr = mval;
//testStr.toString();
//imgForLoad = Uri.parse(testStr);
//imgForLoad.toString();

//imgForLoad = Uri.parse(mval);
//картинка из метода
//imgForLoad1 = upImage();
// break;