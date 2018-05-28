package athi.mc.group11.athi;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.util.ArrayList;



public class WebAppInterface {
    Context mContext;

    /**
     * Instantiate the interface and set the context
     */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public String getData() {
        ArrayList data = DatabaseHandler.getDataToVisualize(mContext);
        return data.toString();
    }
}