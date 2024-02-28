package template.software.a3snet.webview_lksdi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoaderActivity extends AppCompatActivity {
    WebView test;


    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_CHOOSE = 2;
    public static String URL_STRING = "";
    private static String file_type = "image/*";    // типы разрешенных файлов
    private boolean multiple_files = true;         // множество файлов пермисс
    private String cam_file_data = null;        // хран файлов камеры
    private ValueCallback<Uri> file_data;       // дата полученная после выбора
    private ValueCallback<Uri[]> file_path;     // лока полученных файлов
    private final static int file_req_code = 1;



    private ValueCallback<Uri[]> mFilePathCallback;





    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);
        test = findViewById(R.id.mybrowser);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Log.d("  TEST", "TEST77");

        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(test, true);
        } else CookieManager.getInstance().setAcceptCookie(true);

        test.setWebChromeClient(new WebChromeClient() {

            /*-- handling input[type="file"] API 21+ --*/
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {

                // proverka dvoynih callback
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePath;

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*"); // Allow all file types

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");

                startActivityForResult(chooserIntent, REQUEST_CHOOSE);

                return true;
            }


            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                callback.onCustomViewHidden();
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
            }
        });
        test.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.d(" TEST", String.valueOf(errorCode));
                // startActivity(new Intent(LoaderActivity.this, MainActivity.class));
                // LoaderActivity.this.finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                    return true;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                    startActivity(intent);
                }catch (Exception e) {

                }

                return true;
            }





            @Override
            public void onPageFinished(WebView view, String url) {
                CookieSyncManager.getInstance().sync();
                SharedPreferences.Editor editor = getSharedPreferences("saveInfo", MODE_PRIVATE).edit();
                editor.putBoolean("appState", false);
                editor.putString("appLink", url);
                //  Log.d(" SAVED", "SAVED");
                editor.apply();
                test.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }
        });
        test.getSettings().setSupportMultipleWindows(true);
        test.getSettings().setJavaScriptEnabled(true);
        test.getSettings().setAllowContentAccess(true);
        test.getSettings().setDomStorageEnabled(true);
        test.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        test.getSettings().setBuiltInZoomControls(true);
        test.getSettings().setUseWideViewPort(true);
        //test.getSettings().setAppCacheEnabled(true);
        //---------------------изменено---------------------------------
        test.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //--------------------------------------------------------------
        test.getSettings().setAllowFileAccess(true);
        test.getSettings().setPluginState(WebSettings.PluginState.ON);

        test.requestFocus(View.FOCUS_DOWN | View.FOCUS_UP);
        test.getSettings().setLightTouchEnabled(true);
        test.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }
                    break;
            }
            return false;
        });


        if (savedInstanceState != null) {
            test.restoreState(savedInstanceState);
            test.setVisibility(View.VISIBLE);
        } else test.loadUrl(getIntent().getStringExtra("url"));
    }


    @Override
    public void onBackPressed() {
        if (test.canGoBack()) test.goBack();
        else super.onBackPressed();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        test.saveState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;

            /*-- если отклонено - выходим, чтобы можно было пытаться --*/
            if (resultCode == Activity.RESULT_CANCELED) {
                if (requestCode == file_req_code) {
                    file_path.onReceiveValue(null);
                    return;
                }
            }

            /*-- продолжение, если ответ полож --*/
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == file_req_code) {
                    if (null == file_path) {
                        return;
                    }

                    ClipData clipData;
                    String stringData;
                    try {
                        clipData = intent.getClipData();
                        stringData = intent.getDataString();
                    } catch (Exception e) {
                        clipData = null;
                        stringData = null;
                    }

                    if (clipData == null && stringData == null && cam_file_data != null) {
                        results = new Uri[]{Uri.parse(cam_file_data)};
                    } else {
                        if (clipData != null) { // проверка нескольких файлов
                            final int numSelectedFiles = clipData.getItemCount();
                            results = new Uri[numSelectedFiles];
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                results[i] = clipData.getItemAt(i).getUri();
                            }
                        } else {
                            results = new Uri[]{Uri.parse(stringData)};
                        }
                    }
                }
            }
            file_path.onReceiveValue(results);
            file_path = null;
        } else {
            if (requestCode == file_req_code) {
                if (null == file_data) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                file_data.onReceiveValue(result);
                file_data = null;
            }
        }
    }

    public boolean file_permission(){
        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(LoaderActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, 1);
            return false;
        }else{
            return true;
        }
    }


    /*-- новая пикча --*/
    private File create_image() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /*-- новое видео --*/
    private File create_video() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name = "file_" + file_name + "_";
        File sd_directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".3gp", sd_directory);
    }


}