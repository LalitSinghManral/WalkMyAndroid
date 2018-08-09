package com.example.android.walkmyandroid;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FetchAddressTask extends AsyncTask<Location,Void,String> {

    private final String TAG = FetchAddressTask.class.getSimpleName();
    private Context mContext;

    private OnTaskCompleted mListener;

    public FetchAddressTask(Context context,OnTaskCompleted listener){
        mContext = context;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Location... locations) {

        Geocoder geocoder = new Geocoder(mContext);

        Location location = locations[0];

        List<Address> addresses = null;
        String resultMessage="";

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(),1);

        }
        catch (Exception e){
            resultMessage = mContext.getString(R.string.exception);
            Log.e(TAG, resultMessage +" : "+e.getMessage());
        }


        if (addresses == null || addresses.size() == 0){
            if (resultMessage.isEmpty()){
                resultMessage = mContext.getString(R.string.no_address_found);
                Log.i(TAG, resultMessage);
            }
        }
        else {
            Address address = addresses.get(0);
            ArrayList<String> addressParts = new ArrayList<>();

            for (int i=0; i <= address.getMaxAddressLineIndex(); i++){
                addressParts.add(address.getAddressLine(i));
            }

            resultMessage = TextUtils.join("\n",addressParts);
            Log.e(TAG,resultMessage);
        }

        return resultMessage;
    }

    @Override
    protected void onPostExecute(String s) {
        mListener.onTaskCompletion(s);
        super.onPostExecute(s);
    }

    interface OnTaskCompleted{
        void onTaskCompletion(String result);
    }
}
