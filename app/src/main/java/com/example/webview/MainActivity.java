package com.example.webview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.sax.RootElement;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    WebView webview;
    ProgressBar progressBar;
    ProgressDialog progressDialog;
    RelativeLayout relativeLayout;
    Button btn_restry;
    SwipeRefreshLayout swipeRefreshLayout;
    private String WebUrl = "https://baomoi.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.blue)));

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loadView();
        checkConnection();
    }

    private void loadView(){
        webview = findViewById(R.id.webview);

        progressBar = findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Bạn chờ chút...");
        relativeLayout = findViewById(R.id.relativeLayout);
        btn_restry = findViewById(R.id.btn_Retry);
        swipeRefreshLayout = findViewById(R.id.swipeRefeshLayout);
        // tạo màu sắc cho hiệu ứng load khi tại lại trang
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.YELLOW, Color.GREEN, Color.RED);


        webview.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        // cho phép webView hỗ trợ javascript để load ảnh và video trên trang web về
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // cho phép progressBar hoạt động (nó là cái thanh load khi bạn vào mạng)
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                setTitle("Đang tải nè...");
                progressDialog.show();
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);

                if(newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                    setTitle(view.getTitle());
                    progressDialog.dismiss();
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        btn_restry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
            }
        });

        // sử dụng swipeRefreshLayout để load lại webView bừng cử chỉ vuốt
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webview.reload();
            }
        });



    }

    @Override
    public void onBackPressed() {
        if(webview.canGoBack()){
            webview.goBack();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Bạn muốn thoát khỏi ứng dụng ?")
                    .setNegativeButton("Không", null)
                    .setPositiveButton("Đúng", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finishAffinity();
                        }
                    }).show();
        }
    }

    public void checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(wifi.isConnected()){
            webview.loadUrl(WebUrl);
            webview.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
        }else if(mobileNetwork.isConnected()){
            webview.loadUrl(WebUrl);
            webview.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
        }else {
            webview.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int checkId = item.getItemId();

        if(checkId == R.id.nav_previous){
            onBackPressed();
        }else if(checkId == R.id.nav_next){
            if(webview.canGoBack()){
                webview.goForward();
            }
        }else if(checkId == R.id.nav_resert) {
            checkConnection();
        }
        return super.onOptionsItemSelected(item);
    }
}