package ru.vasilkov.geoslideshow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;


public class MainActivity extends Activity {

    NumberPicker npCountPhoto;
    NumberPicker npPhotoTimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        npCountPhoto = (NumberPicker)findViewById(R.id.numberPicker);
        npPhotoTimeView = (NumberPicker)findViewById(R.id.numberPicker2);

        npCountPhoto.setMinValue(0);
        npCountPhoto.setMaxValue(10);
        npCountPhoto.setValue(4);
        npCountPhoto.setWrapSelectorWheel(false);

        npPhotoTimeView.setMinValue(0);
        npPhotoTimeView.setMaxValue(5);
        npPhotoTimeView.setValue(0);
        npPhotoTimeView.setWrapSelectorWheel(false);

        npPhotoTimeView.setDisplayedValues(new String[]{"0.5","1","1.5","2","2.5","3"});

    }

    private int getTime(){
        return ((npPhotoTimeView.getValue() + 1) * 1000) / 2;
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void btnNextClick(View view) {
        if (hasInternetConnection()) {
            Intent slideShowView = new Intent(MainActivity.this, SlideShowActivity.class);
            slideShowView.putExtra("count_photo", npCountPhoto.getValue());
            slideShowView.putExtra("time_display", getTime());
            startActivity(slideShowView);
        } else {
            Toast.makeText(this, "Проверьте соединение с интернетом", Toast.LENGTH_LONG).show();
        }
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        for (NetworkInfo ni : netInfo)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) {
                    return true;
                }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {
                    return true;
                }
        }
        return false;
    }
}
