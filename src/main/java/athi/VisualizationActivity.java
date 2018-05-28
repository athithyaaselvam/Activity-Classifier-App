package athi.mc.group11.athi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class VisualizationActivity extends AppCompatActivity {
    //Creates and initializes visualization activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(athi.athi.group11.athi.R.layout.activity_visualization);
        initializeVisualization();
    }
    //Calls the WebApp Interface to display the 3D-Graph
    private void initializeVisualization() {
        WebView myweb_View = (WebView) findViewById(athi.athi.group11.athi.R.id.webview);
        myweb_View.addJavascriptInterface(new WebAppInterface(this), "Android");
        WebSettings web_Settings = myweb_View.getSettings();
        web_Settings.setJavaScriptEnabled(true);
        myweb_View.loadUrl("file:///android_asset/index.html");
    }

}
