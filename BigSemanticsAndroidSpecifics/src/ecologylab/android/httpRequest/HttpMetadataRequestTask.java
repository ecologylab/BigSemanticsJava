package ecologylab.android.httpRequest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import android.os.AsyncTask;
import android.util.Log;

/**
 * a async task to obtain metadata for a url through semantic service
 * @author fei
 *
 */
public class HttpMetadataRequestTask extends AsyncTask<MetadataRequestObject, String, MetadataRequestObject>{

	private static final String TAG = "HttpMetadataRequestTask";
	
    @Override
    protected MetadataRequestObject doInBackground(MetadataRequestObject... requestObj) {
    
        // quick and dirty way to check '|'
        String cleanURL = requestObj[0].getUrl().toString();
        int i = cleanURL.indexOf("|");
        if (i >= 0)
        	cleanURL = cleanURL.substring(0, i);
        
        try {
        	cleanURL = URLEncoder.encode(cleanURL, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String requestURL = requestObj[0].getServer_url_prefix() + cleanURL;
        Log.i(TAG, "requestURL: " + requestURL);
        
        URL url;
		try {
			url = new URL(requestURL);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			try{
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				String s = convertStreamToString(in);
				requestObj[0].setSerializedMetadata(s);
			}
			finally {
				urlConnection.disconnect();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        return requestObj[0];
    }

    @Override
    protected void onPostExecute(MetadataRequestObject requestObj) {
        super.onPostExecute(requestObj);
        //Do anything with response..
        requestObj.getRequester().callbackFromHttpRequest(requestObj);
    }
    
    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
