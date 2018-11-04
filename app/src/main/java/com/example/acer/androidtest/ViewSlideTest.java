package com.example.acer.androidtest;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class ViewSlideTest extends View {


    private int lastX;
    private int lastY;

    public ViewSlideTest(Context context) {
        super(context);
    }

    public ViewSlideTest(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewSlideTest(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x= (int) event.getX();
        int y= (int) event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                lastX=x;
                lastY=y;
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                int offsetX=x-lastX;
                int offsetY=y-lastY;
                /**
                 * ....方法一......重新 设置layout

                layout(getLeft()+offsetX,getTop()+offsetY,
                        getRight()+offsetX,getBottom()+offsetY);
                 */
                /**
                 * .....方法二.....offsetLeftAndRight......
                 *
                 *     offsetLeftAndRight(offsetX);
                       offsetTopAndBottom(offsetY);
                 */
                /**
                 * .....方法三：....LayoutParams..修改view的布局参数
                 *
                LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) getLayoutParams();
                layoutParams.leftMargin=getLeft()+offsetX;
                layoutParams.TMargin=getTop()+offsetY;
                setLayoutParams(layoutParams);
                 */
                /**
                 * ......方法四：....View动画 ，注意的是只能改变他在父布局中的位置，  某个位置的点击 的事件是不变的
                 */
                /**
                 * ......方法五：....scrollTo和scrollBy...
                 *             注意向右下移动是  -的offset，
                 *             ((View)getParent()).scrollBy(-offsetX,-offsetY);
                 */


                break;
            }
        }
        return true;
    }
}
