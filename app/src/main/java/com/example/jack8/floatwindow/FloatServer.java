package com.example.jack8.floatwindow;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Scroller;
import android.widget.TextView;


public class FloatServer extends Service {
    WindowManager wm;
    Notification NF;
    final int NotifyId=851262;
    int wm_count=0;//計算FloatServer總共開了多少次
    Handler runUi= new Handler();
    @Override
    public void onCreate() {
        super.onCreate();
        wm=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        NF=new Notification.Builder(getApplicationContext()).
                setSmallIcon(R.drawable.menu_icom).
                setContentTitle("浮動視窗").
                setContentText("浮動視窗已啟用").build();
        startForeground(NotifyId,NF);//將服務升級至前台等級，這樣就不會突然被系統回收
        Log.i("WMStrver","Create");
    }
    /*
    關於onStartCommand的說明
    http://www.cnblogs.com/not-code/archive/2011/05/21/2052713.html
     */
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        final WindowManager.LayoutParams wmlp;
        final View winform;//視窗外框
        final ViewGroup _wincon;
        final View[] wincon;
        final TextView Title;
        final WindowInfo windowInfo;

        wm_count++;

        wmlp = new WindowManager.LayoutParams();
        wmlp.type= WindowManager.LayoutParams.TYPE_PHONE;//類型
        wmlp.format = PixelFormat.RGBA_8888;//背景(透明)
        //wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//設定焦點(不聚交)，否則背後介面將不可操作，因為會有一層透明的圖層
        wmlp.gravity = Gravity.LEFT | Gravity.TOP;//設定重力(初始位置)
        wmlp.x=60;//設定原點座標
        wmlp.y=60;
        wmlp.width = WindowManager.LayoutParams.WRAP_CONTENT;//設定視窗大小
        wmlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        winform= LayoutInflater.from(getApplicationContext()).inflate(R.layout.window,null);
        winform.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect rect = new Rect();
                v.getGlobalVisibleRect(rect);//取得視窗所在的範圍
                if(rect.contains((int)event.getX(),(int)event.getY())){//當Touch的點在視窗範圍內
                    Log.i("Touch","win");
                    wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;//讓視窗聚焦
                }else
                    wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                wm.updateViewLayout(winform, wmlp);
                return false;
            }
        });
        wm.addView(winform,wmlp);
        _wincon=(ViewGroup) winform.findViewById(R.id.wincon);
        windowInfo=new WindowInfo(wmlp,winform,_wincon,wmlp.x,wmlp.y,_wincon.getLayoutParams().width,_wincon.getLayoutParams().height);
        //-------------------------建立視窗內容畫面----------------------------------------------------
        Title=(TextView)winform.findViewById(R.id.title);
        int[] ids=intent.getExtras().getIntArray("Layouts");//頁面id
        String[] titles=intent.getExtras().getStringArray("Titles");//頁面標題
        wincon=new View[ids.length];
        wincon[0]=LayoutInflater.from(getApplicationContext()).inflate(ids[0],(ViewGroup) winform,false);//視窗內容實例
        _wincon.addView(wincon[0]);
        wincon[0].setTag(titles[0]);
        Title.setText(titles[0]);
        for(int i=1;i<wincon.length;i++) {
            wincon[i] = LayoutInflater.from(getApplicationContext()).inflate(ids[i], (ViewGroup) winform, false);
            wincon[i].setTag(titles[i]);
        }
        Title.setOnTouchListener(new MoveWindow(wmlp,windowInfo,winform));
        Title.setOnClickListener(windowInfo);
        /*Log.i("formwidth",winform.getWidth()+"");
        Title.getLayoutParams().width=winform.getWidth()-160;*/
        ((Button)winform.findViewById(R.id.close_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeView(winform);
                if(--wm_count==0) {
                    FloatServer.this.stopForeground(true);
                    stopSelf();
                }
            }
        });
        winform.findViewById(R.id.size).setOnTouchListener(new View.OnTouchListener() {
            float Wlength=-1,Hlength=-1;
            @Override
            public boolean onTouch(View v, MotionEvent event) {//調整視窗大小
                wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    if(Wlength==-1||Hlength==-1){
                        Wlength=_wincon.getWidth()-event.getX();//取得點擊X點到視窗最右側的距離
                        Hlength=(event.getRawY()-wmlp.y)-_wincon.getHeight();//取得視窗標題列高度以及視窗大小調整列被點擊以上的高度，視窗內部框架高度-(點擊座標Y-視窗Top座標)
                        return true;
                    }
                    Log.i("size",(int)(event.getX()+Wlength)+"   "+(int)(event.getRawY()-wmlp.y+Hlength));
                    int temp;
                    _wincon.getLayoutParams().width=windowInfo.width=(temp=(int)(event.getX()+Wlength))>30?temp:30;
                    _wincon.getLayoutParams().height=windowInfo.height=(temp=(int)(event.getRawY()-wmlp.y-Hlength))>=0?temp:0;//Touch的Y減去視窗的Top再減去Hlength就是視窗內容區要調整的高度
                    Log.i("size2",_wincon.getLayoutParams().width+"   "+_wincon.getLayoutParams().height);
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    Wlength=-1;
                    Hlength=-1;
                }
                wm.updateViewLayout(winform, wmlp);
                return true;
            }
        });
        //-----------------------------------------------------------------------------
        //-------------------------建立Menu-------------------------------
        final Button menu=(Button)winform.findViewById(R.id.menu);
        final PopupMenu pm=new PopupMenu(getApplicationContext(),menu);
        Menu m=pm.getMenu();
        for(int i=0;i<wincon.length;i++)
            //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
            m.add(0,i,i,(String)wincon[i].getTag());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            int ViweIndex=0;
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                _wincon.removeView(wincon[ViweIndex]);
                _wincon.addView(wincon[item.getItemId()]);
                Title.setText((String)wincon[item.getItemId()].getTag());
                ViweIndex=item.getItemId();
                return true;
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pm.show();
            }
        });
        Title.getLayoutParams().width=_wincon.getLayoutParams().width-(menu.getLayoutParams().width*3);
        //------------------------------------------------------------------
        //---------------------------縮到最小按鈕---------------------------
        ((Button)winform.findViewById(R.id.mini)).setOnClickListener(windowInfo);
        //------------------------------------------------------------------
        //---------------------------初始化視窗內容-------------------------------
        for(int i=0;i<wincon.length;i++)
            initWindow.init(wincon[i],i,winform,wm,wmlp);
        //---------------------------------------------------------------------------------------------
        return START_REDELIVER_INTENT;
    }
    class WindowInfo implements View.OnClickListener,Runnable{
         int top,left,height,width;
        WindowManager.LayoutParams wmlp;
        View winform;
        ViewGroup wincon;
        boolean isMini=false;
        Scroller topMini=new Scroller(FloatServer.this),heightMini=new Scroller(FloatServer.this);

        public WindowInfo(WindowManager.LayoutParams wmlp,View winform,ViewGroup wincon,int top,int left,int width,int height){
            this.wmlp=wmlp;
            this.winform=winform;
            this.wincon=wincon;
            this.top=top;
            this.left=left;
            this.height=height;
            this.width=width;
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mini:
                    if (!isMini) {
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        topMini.startScroll(left, top, (displayMetrics.widthPixels - 80) - left, -top, 1000);
                        heightMini.startScroll(width, height, 80 - width, -height, 1000);
                        wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                        ((Button) winform.findViewById(R.id.menu)).setVisibility(View.GONE);
                        ((Button) winform.findViewById(R.id.close_button)).setVisibility(View.GONE);
                        ((Button) winform.findViewById(R.id.mini)).setVisibility(View.GONE);
                        ((LinearLayout) winform.findViewById(R.id.size)).setVisibility(View.GONE);
                        runUi.post(this);
                        isMini = true;
                    }
                    break;
                case R.id.title:
                    if(isMini){
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        topMini.startScroll(wmlp.x,wmlp.y,left-wmlp.x,top-wmlp.y,1000);
                        heightMini.startScroll(wincon.getLayoutParams().width, wincon.getLayoutParams().height
                                ,  width-wincon.getLayoutParams().width, height-wincon.getLayoutParams().height, 1000);
                        wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                        ((Button) winform.findViewById(R.id.menu)).setVisibility(View.VISIBLE);
                        ((Button) winform.findViewById(R.id.close_button)).setVisibility(View.VISIBLE);
                        ((Button) winform.findViewById(R.id.mini)).setVisibility(View.VISIBLE);
                        ((LinearLayout) winform.findViewById(R.id.size)).setVisibility(View.VISIBLE);
                        runUi.post(this);
                        isMini = false;
                    }
            }
        }

        @Override
        public void run() {
            if(!topMini.isFinished()||!heightMini.isFinished()) {
                if (topMini.computeScrollOffset()) {
                    wmlp.x = topMini.getCurrX();
                    wmlp.y = topMini.getCurrY();
                }
                if (heightMini.computeScrollOffset()) {
                    wincon.getLayoutParams().width = heightMini.getCurrX();
                    wincon.getLayoutParams().height = heightMini.getCurrY();
                }
                wm.updateViewLayout(winform, wmlp);
                runUi.post(this);
            }
        }
    }
    class MoveWindow implements View.OnTouchListener{
        WindowManager.LayoutParams wmlp;
        WindowInfo windowInfo;
        View winform;
        float H=-1,W=-1;
        public MoveWindow(WindowManager.LayoutParams wmlp,WindowInfo windowInfo,View winform){
            this.wmlp=wmlp;
            this.windowInfo=windowInfo;
            this.winform=winform;
        }
        /*
        移動視窗
        關於getX,getY等等的含數用法:
        http://blog.csdn.net/u013872857/article/details/53750682
         */
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(!windowInfo.isMini)
                wmlp.flags = WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;
            if(event.getAction() == MotionEvent.ACTION_MOVE) {
                if(H==-1||W==-1){
                    H=event.getX();//取得點擊的X座標到視窗頂點的距離
                    W=event.getY();//取得點擊的Y座標到視窗頂點的距離
                    return true;
                }
                wmlp.x = windowInfo.left = (int) (event.getRawX()-H-80);
                wmlp.y = windowInfo.top = (int) (event.getRawY()-W-60);//60為狀態列高度
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                H=-1;
                W=-1;
            }
            wm.updateViewLayout(winform, wmlp);
            return false;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

