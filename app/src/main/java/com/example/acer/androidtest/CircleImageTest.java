package com.example.acer.androidtest;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;

public class CircleImageTest extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_image_test);

        /**
         * view滑动普通
         */
        ViewSlideTest viewSlide=findViewById(R.id.view_slide);
//        viewSlide.setAnimation(AnimationUtils.loadAnimation(this,R.anim.translate));

        /**
         *ObjectAnimator
         */
        MyView myView=new MyView(viewSlide);
        ObjectAnimator.ofInt(myView,"width",500).setDuration(500).start();




    }

    /**
     * ObjectAnimator   利用的是反射机制
     *       对于要操作的属性必须有get和set
     */
    public static class MyView{
        private View target;
        private MyView(View target){
            this.target=target;
        }

        public int getWidth(){
            return target.getLayoutParams().width;
        }
        public void setWidth(int width){
            target.getLayoutParams().width=width;
            target.requestLayout();
        }

    }
}
