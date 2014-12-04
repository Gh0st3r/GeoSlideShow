package ru.vasilkov.geoslideshow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SlideShowActivity extends Activity {

    Context context = this;

    private int countPhoto;
    private int timeDisplay;
    private int ind = 0;
    private int realPhotoCount;
    private int Err = 0;

    private String getPhotoFromPanoramio;
    private LocationManager locationManager;

    private List<PhotoAttr> listPhoto = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);

        countPhoto = getIntent().getIntExtra("count_photo", 1);
        timeDisplay = getIntent().getIntExtra("time_display", 1000);

        refresh();

    }

    private void refresh(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
    }

    class PhotoAttr {
        String upload;
        String currLat;
        String currLng;
        Bitmap bitmap;
        String[] date;
        Integer keySort;
        PhotoAttr(String up, String cLat, String cLng, Bitmap bmp) {
            upload = up; currLat = cLat; currLng = cLng; bitmap = bmp;
            date =  upload.trim().split("\\s+");
            keySort = Integer.valueOf(date[2] + getMonthNumb(date[1]) + date[0]);
        }
    }

    private String getMonthNumb(String month){
        switch (month){
            case "January": {
                return "01";
            }
            case "February": {
                return "02";
            }
            case "March": {
                return "03";
            }
            case "April": {
                return "04";
            }
            case "May": {
                return "05";
            }
            case "June": {
                return "06";
            }
            case "July": {
                return "07";
            }
            case "August": {
                return "08";
            }
            case "September": {
                return "09";
            }
            case "October": {
                return "10";
            }
            case "November": {
                return "11";
            }
            case "December": {
                return "12";
            }
        }
        return "00";
    }

    class GetPanoramioPhotoAsyncTask extends AsyncTask<Void, Void, Void> {

        public ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setMessage("Поиск...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            String imageUrls, uploadDate, currLat, currLng;
            Bitmap image;

            try {
                URL url = new URL(getPhotoFromPanoramio);
                InputStream inputStream = url.openConnection().getInputStream();

                String response = streamToString(inputStream);

                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();

                JSONArray jsonArray1 = jsonObject.getJSONArray("photos");
                realPhotoCount = jsonArray1.length();

                if(realPhotoCount < countPhoto) countPhoto = realPhotoCount;

                for (int index = 0; index < countPhoto; index ++) {
                    JSONObject mainImageJsonObject = jsonArray1.getJSONObject(index);

                    imageUrls = mainImageJsonObject.getString("photo_file_url");
                    uploadDate = mainImageJsonObject.getString("upload_date");
                    currLat = mainImageJsonObject.getString("latitude");
                    currLng = mainImageJsonObject.getString("longitude");
                    image = BitmapFactory.decodeStream((InputStream) new URL(imageUrls).getContent());
                    listPhoto.add(new PhotoAttr(uploadDate, currLat, currLng, image));
                }

                Collections.sort(listPhoto, new Comparator<PhotoAttr>() {
                    public int compare(PhotoAttr o1, PhotoAttr o2) {
                        return o1.keySort.compareTo(o2.keySort);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (hasInternetConnection()) {

                final Handler handler = new Handler();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imgSlideShow = (ImageView) findViewById(R.id.imgSlideShow);
                        TextView text1 = (TextView) findViewById(R.id.textView3);
                        TextView text2 = (TextView) findViewById(R.id.textView4);
                        TextView text3 = (TextView) findViewById(R.id.textView5);
                        TextView text4 = (TextView) findViewById(R.id.textView6);
                        TextView text5 = (TextView) findViewById(R.id.textView7);

                        imgSlideShow.setImageBitmap(listPhoto.get(ind).bitmap);

                        text1.setText(getString(R.string.real_photo_count) + "\n" + Integer.toString(realPhotoCount));
                        text2.setText(getString(R.string.displayed_photo) + "\n" + Integer.toString(countPhoto));
                        text3.setText(getString(R.string.curr_foto_location) + "\n" + "lat: " + listPhoto.get(ind).currLat + " long: " + listPhoto.get(ind).currLng);
                        text4.setText(getString(R.string.upload_date) + "\n" + listPhoto.get(ind).upload);
                        text5.setText(getString(R.string.curr_photo_numb) + "\n" + Integer.toString(ind + 1));

                        ind++;
                        handler.postDelayed(this, timeDisplay);
                        if (ind == countPhoto) {
                            ind = 0;
                        }
                    }
                });
            }else {

                ImageView imgSlideShow = (ImageView) findViewById(R.id.imgSlideShow);
                Drawable drawable = context.getResources().getDrawable(R.drawable.err_not_found);
                imgSlideShow.setImageDrawable(drawable);
            }
        }
    }

    public String streamToString(InputStream is) throws IOException {
        String string = "";

        if (is != null) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            string = stringBuilder.toString();
        }

        return string;
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

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            showSlideShow(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

            showSlideShow(locationManager.getLastKnownLocation(provider));
        }

    };

    private void showSlideShow(Location location){

        if (location == null) return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)){
            getPhoto(location);
        }
        else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
            getPhoto(location);
        }
    }

    private void getPhoto(Location location){
        double lat = location.getLatitude();

        String latmin = Double.toString(lat - 0.002);
        String latmax = Double.toString(lat + 0.002);

        float lng = (float)location.getLongitude();

        String lngmin = Double.toString(lng - 0.002);
        String lngmax = Double.toString(lng + 0.002);

        getPhotoFromPanoramio = "http://www.panoramio.com/map/get_panoramas.php?set=public&from=0&to=20&minx=" + lngmin + "&miny=" + latmin + "&maxx=" + lngmax + "&maxy=" + latmax + "&size=medium&mapfilter=true";

        GetPanoramioPhotoAsyncTask getPanPhoto = new GetPanoramioPhotoAsyncTask();
        getPanPhoto.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slide_show, menu);
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

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(locationListener);

    }
}
