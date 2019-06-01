package com.mnevent.hz.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;


public class GoodsScrollView extends ScrollView implements SlidingDetailsLayout.TopBottomListener{
	 private boolean top,bottom;

	private OnScrollChangeListener mOnScrollChangeListener;

	/**
	 * 设置滚动接口
     * @param
     * @param onScrollChangeListener
     */

	public void setOnScrollChangeListener(OnScrollChangeListener onScrollChangeListener) {
		mOnScrollChangeListener = onScrollChangeListener;
	}

	public GoodsScrollView(Context context) {
		super(context);
	}





	/**
	 *
	 *定义一个滚动接口
	 * */

	public interface OnScrollChangeListener{
		void onScrollChanged(GoodsScrollView scrollView, int l, int t, int oldl, int oldt);
	}



	    public GoodsScrollView(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        top = false;
	        bottom = false;
	    }

	    @Override
	    public boolean isScrollTop() {
	        return top;
	    }

	    @Override
	    public boolean isScrollBottom() {
	        return bottom;
	    }

	    @Override
	    public void scrollToTop() {
	        fullScroll(View.FOCUS_UP);
	    }

	    @Override
	    public void scrollToBottom() {
	        fullScroll(View.FOCUS_DOWN);
	    }

	    @Override
	    protected void onLayout(boolean changed, int l, int t, int r, int b) {
	        super.onLayout(changed,l,t,r,b);

	        if(getChildAt(0).getHeight()<getHeight()){
	            top = true;
	            bottom = true;
	        }else{
	            top = getScrollY() == 0;

	            int diff = (getChildAt(0).getBottom()-(getHeight()+getScrollY()));
	            bottom = diff == 0;
	        }

	    }

	    @Override
	    public boolean dispatchTouchEvent(MotionEvent ev) {
	        //Log.e("MyScrollView","dispatchTouchEvent ==> "+ev);
	        return super.dispatchTouchEvent(ev);
	    }

	    @Override
	    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
	        super.onScrollChanged(l, t, oldl, oldt);
	        top = getScrollY() == 0;

	        int diff = (getChildAt(0).getBottom()-(getHeight()+getScrollY()));
	        bottom = diff == 0;
            /**
             * 当scrollView滑动时系统会调用该方法,并将该回调放过中的参数传递到自定义接口的回调方法中,
             * 达到scrollView滑动监听的效果
             *
             * */
            if(mOnScrollChangeListener!=null){
                mOnScrollChangeListener.onScrollChanged(this,l,t,oldl,oldt);

            }

	    }


}
