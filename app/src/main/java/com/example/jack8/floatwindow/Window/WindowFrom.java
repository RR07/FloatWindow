package com.example.jack8.floatwindow.Window;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.example.jack8.floatwindow.R;

public class WindowFrom extends LinearLayout {
    final WindowManager wm;
    WindowManager.LayoutParams wmlp=null;
    WindowStruct WS;
    public boolean isStart=true;
    public WindowFrom(Context context) {
        super(context);
        wColor=new WindowColor(context);
        wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }
    public WindowFrom(Context context, AttributeSet attrs) {
        super(context, attrs);
        wColor=new WindowColor(context);
        wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }
    public void setLayoutParams(WindowManager.LayoutParams wmlp,WindowStruct WS){
        this.wmlp=wmlp;
        this.WS=WS;
    }
    View titleBar,sizeBar,microMaxButtonBackground,closeButtonBackground;
    WindowColor wColor;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
        titleBar=this.findViewById(R.id.title_bar);
        sizeBar=this.findViewById(R.id.size);
        microMaxButtonBackground=this.findViewById(R.id.micro_max_button_background);
        closeButtonBackground=this.findViewById(R.id.close_button_background);
        findViewById(R.id.window).setBackgroundColor(wColor.getWindowBackground());

        if(isStart){
            isStart=false;
            titleBar.setBackgroundColor(wColor.getTitleBar());
            sizeBar.setBackgroundColor(wColor.getSizeBar());
            microMaxButtonBackground.setBackgroundColor(wColor.getMicroMaxButtonBackground());
            closeButtonBackground.setBackgroundColor(wColor.getCloseButtonBackground());
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(wmlp!=null) {
            if(WindowStruct.NOW_FOCUS_NUMBER!=WS.Number){//如果被觸碰的視窗編號不是現在焦點視窗編號
                if(WindowStruct.windowList.containsKey(WindowStruct.NOW_FOCUS_NUMBER)){//如果現在焦點視窗編號在有視窗清單裡
                    WindowStruct WS=WindowStruct.windowList.get(WindowStruct.NOW_FOCUS_NUMBER);
                    if(!WS.isMini)
                        WS.getWindowFrom().unFocusWindow();
                }
                WindowStruct.NOW_FOCUS_NUMBER=WS.Number;
                wm.removeView(this);
                wm.addView(this,wmlp);
                focusWindow();
                return true;//防止點擊事件因為至視窗至頂切換變成長按事件
            }
        }
        return super.onInterceptTouchEvent(event);
    }
    public void focusWindow(){
        titleBar.setBackgroundColor(wColor.getTitleBar());
        sizeBar.setBackgroundColor(wColor.getSizeBar());
        microMaxButtonBackground.setBackgroundColor(wColor.getMicroMaxButtonBackground());
        closeButtonBackground.setBackgroundColor(wColor.getCloseButtonBackground());
    }
    public void unFocusWindow(){
        titleBar.setBackgroundColor(wColor.getWindowNotFoucs());
        sizeBar.setBackgroundColor(wColor.getWindowNotFoucs());
        microMaxButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
        closeButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
    }
}
