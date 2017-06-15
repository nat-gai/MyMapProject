package com.nat.mymapproject;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ItemImageInfo extends Fragment{
    public static final String COUNT = "count";
    public static final String IMG_URL = "URL";
    private static final int MAX_WIDTH = 1080;
    private static final int MAX_HEIGHT = 960;
    private Uri imgForLoad;
    int size;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mainViewItem = inflater.inflate(R.layout.item_pager_img, container, false);

        Bundle args = getArguments();

        size = (int) Math.ceil(Math.sqrt(MAX_WIDTH * MAX_HEIGHT));

        //TextView textView = (TextView) mainViewItem.findViewById(R.id.textImg);
        //textView.setText(args.getString(COUNT));

        ImageView imageViewImg = (ImageView) mainViewItem.findViewById(R.id.imgImg);

        imgForLoad = Uri.parse(args.getString(IMG_URL));


        //imageViewImg.setImageResource(R.drawable.test_image);
        Picasso.with(imageViewImg.getContext())
                                    .load(imgForLoad)
                                    //.load("https://firebasestorage.googleapis.com/v0/b/mymapproject-d81bb.appspot.com/o/crane.jpg?alt=media&token=259879c3-a348-42c3-9416-9cc8cc61b7cd")
                                    //.transform(new BitmapTransform(2064, 1548))
                                    .skipMemoryCache()
                                    //.resize(size, size) //не размер а качество
                                    //.centerCrop()
                                    .into(imageViewImg);
        //imageViewImg.setScaleType(ImageView.ScaleType.FIT_XY);

        /*Picasso.with(getContext())
                .load("https://firebasestorage.googleapis.com/v0/b/mymapproject-d81bb.appspot.com/o/my%2Fmy.jpg?alt=media&token=1a9a9567-2e56-4a06-9e64-db6308c83810")
                //.load("https://firebasestorage.googleapis.com/v0/b/mymapproject-d81bb.appspot.com/o/crane.jpg?alt=media&token=259879c3-a348-42c3-9416-9cc8cc61b7cd")
                //.transform(new BitmapTransform(MAX_WIDTH, MAX_HEIGHT))
                .skipMemoryCache()
                //.resize(imageViewImg.getWidth(), imageViewImg.getHeight())
                //.centerInside()
                .into(imageViewImg);
                */



        return mainViewItem;
    }
}
 /*
 Здесь метод onCreateView() будет строить нам отображение отдельной страницы,
  получая данные из объекта Bundle, которые мы будем передавать из класса-адаптера
  (создадим на следующем шаге).
  Аргументы достаются так:
  Bundle args = getArguments();
  Аргументы достаются по ключам, имя которых помещено в константы ARG_TEXT, ARG_POSITION и ARG_COUNT.
   Достать тот или иной аргумент можно с помощью методов:
   args.getInt(ARG_POSITION)
  */