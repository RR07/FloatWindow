package com.example.jack8.floatwindow;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 初始化視窗內容
 */
public class initWindow {
    public static void init(Context context,View v, int index, View winform, WindowManager wm, WindowManager.LayoutParams wmlp){
        switch (index){
            case 0:
                initWindow1(context,v,winform,wm,wmlp);
                break;
            case 1:
                initWindow2(context,v,winform,wm,wmlp);
                break;
            case 2:
                initWindow3(context,v,winform,wm,wmlp);
                break;
        }
    }
    public static void initWindow1(Context context,View v,final View winform,final WindowManager wm,final WindowManager.LayoutParams wmlp){
        final EditText path=(EditText)v.findViewById(R.id.webpath);
        path.setText("https://www.google.com.tw/?gws_rd=ssl");
        Button go=(Button)v.findViewById(R.id.go);
        Button goBack=(Button)v.findViewById(R.id.goback);
        final WebView web=(WebView)v.findViewById(R.id.web);
        web.getSettings().setJavaScriptEnabled(true);
        web.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){//當點擊WebView內的連結時處理，參考:https://dotblogs.com.tw/newmonkey48/2013/12/26/136486
                path.setText(url);
                web.loadUrl(url);
                return true;
            }
        });
        web.loadUrl("https://www.google.com.tw/?gws_rd=ssl");
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web.loadUrl(path.getText().toString());
            }
        });
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebBackForwardList WBFL = web.copyBackForwardList();
                if(WBFL.getCurrentIndex()==0)//當目前顯示的是WebView第一個顯示的網址
                    return;
                path.setText(WBFL.getItemAtIndex(WBFL.getCurrentIndex()-1).getUrl());//取得上一頁的網址連結
                web.goBack();

            }
        });
    }
    public static void initWindow2(Context context,View v,final View winform,final WindowManager wm,final WindowManager.LayoutParams wmlp){
        final EditText et=(EditText)v.findViewById(R.id.Temperature);
        View.OnClickListener oc=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et.getText().toString().matches("| "))
                    return;
                switch (v.getId()) {
                    case R.id.toC:
                        et.setText(String.valueOf((Float.parseFloat(et.getText().toString()) - 32) * 5f / 9f));
                        break;
                    case R.id.toF:
                        et.setText(String.valueOf(Float.parseFloat(et.getText().toString())*(9f/5f)+32));
                        break;
                }
            }
        };
        ((Button)v.findViewById(R.id.toC)).setOnClickListener(oc);
        ((Button)v.findViewById(R.id.toF)).setOnClickListener(oc);
    }
    public static void initWindow3(Context context,View v,final View winform,final WindowManager wm,final WindowManager.LayoutParams wmlp){
        final EditText H=(EditText)v.findViewById(R.id.H),W=(EditText)v.findViewById(R.id.W);
        final TextView BMI=(TextView)v.findViewById(R.id.BMI);
        ((Button)v.findViewById(R.id.CH)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(H.getText().toString().matches("| ")||W.getText().toString().matches("| "))
                    return;
                float h=Float.parseFloat(H.getText().toString())/100f;
                BMI.setText(String.valueOf(Float.parseFloat(W.getText().toString())/(h*h)));
            }
        });
    }
}
