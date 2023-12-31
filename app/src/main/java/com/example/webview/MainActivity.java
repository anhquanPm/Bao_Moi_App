package com.example.webview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Contacts;
import android.sax.RootElement;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
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
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Circle;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
    WebView webview;
    ProgressBar progressBar;
    AlertDialog progressDialog;

    RelativeLayout relativeLayout;
    Button btn_restry;
    SwipeRefreshLayout swipeRefreshLayout;
    private String WebUrl = "https://baomoi.com";

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
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Bạn chờ chút...");
        progressDialog = new SpotsDialog(this, R.style.Custom);

        relativeLayout = findViewById(R.id.relativeLayout);
        btn_restry = findViewById(R.id.btn_Retry);
        swipeRefreshLayout = findViewById(R.id.swipeRefeshLayout);
        // tạo màu sắc cho hiệu ứng load khi tải lại trang
        swipeRefreshLayout.setColorSchemeColors(Color.GREEN, Color.YELLOW, Color.RED, Color.RED);


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

        // nút restry khi app bị mất kết nối mạng
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


        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                Dexter.withActivity(MainActivity.this)
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s));
                                request.setMimeType(s3);
                                String cookies = CookieManager.getInstance().getCookie(s1);
                                request.addRequestHeader("cookie", cookies);
                                request.addRequestHeader("User_Agent", s1);
                                request.setDescription("Đang tải file...");
                                request.setTitle(URLUtil.guessFileName(s,s2,s3));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                                s,s2,s3));
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);
                                Toast.makeText(MainActivity.this, "Đang tải file...", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
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