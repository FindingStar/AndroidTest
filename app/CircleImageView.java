/**
 *

package com.example.acer.androidtest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.widget.ImageViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
 */
/**
 * 自定义圆形头像：核心思想：利用bitmapshader印章
 *
 * 几个重要参数：  BorderPaint,
 *
 * 调用顺序  ：首先地 会根据xml的设置src 调用setImageDrawable  ，将bitmap参数调入代码中，   并且调用setup 进行paint 等的初始化赋值
 *            然后 ，， 进行初始化完毕后， 他就知道了wrapcontent  应该多大？？ 使用invalidate（）争取重绘，
 *            然后   ，，重绘：调用二参的的构造函数，
 *            ondraw

public class CircleImageView extends ImageView {

    private static final String TAG = "CircleImageView";

    private static final ImageView.ScaleType SCALE_TYPE = ImageView.ScaleType.CENTER_CROP;
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;
    private static final int COLORDRAWABLE_DIMENSION = 1;
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    private boolean mReady;
    private boolean mSetupPending;
    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mDrawableRadius;
    private float mBorderRadius;

    //    java代码会取用这个
    public CircleImageView(Context context) {

        super(context);
        Log.d(TAG, "CircleImageView: 一个参数的构造调用");
    }
//   xml 文件会用这个
    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);

        Log.d(TAG, "CircleImageView: 2个参数的构造调用");
    }

//      属性值获取的优先级从高到低依次是set, defStyleAttr, defStyleRes. defStyleAttr是一个reference, 它指向当前Theme中的一个style, style其实就是各种属性的集合，
//    如果defStyleAttr为0或者在Theme中没有找到相应的style, 则 才会尝试从defStyleRes获取属性值，defStyleRes表示的是一个style的id,
    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        以原图填满ImageView为目的，如果原图size大于ImageView的size，则与center_inside一样，按比例缩小，居中显示在ImageView上。
//           如果原图size小于ImageView的size，则按比例拉升原图的宽和高，填充ImageView居中显示。
        super.setScaleType(SCALE_TYPE);
//  参数 为：   调用的级别次序  看上面说明
        TypedArray array=context.obtainStyledAttributes(attrs,R.styleable.CircleImageView,defStyleAttr,0);
        mBorderWidth=array.getDimensionPixelSize(R.styleable.CircleImageView_border_width,DEFAULT_BORDER_WIDTH);
        mBorderColor=array.getColor(R.styleable.CircleImageView_border_color,DEFAULT_BORDER_COLOR);
        array.recycle();

        Log.d(TAG, "CircleImageView: mBorderWidth 和  mBorderColor"+mBorderWidth+"//"+mBorderColor);
        
        mReady=true;

        if (mSetupPending){
            setUp();
            mSetupPending=false;
            Log.d(TAG, "CircleImageView: 从SetupPending调用setup");
        }
    }

    private void setUp(){
        if (!mReady){
            mSetupPending=true;
            return;
        }

        if (mBitmap==null){
            return;
        }

//        用边缘色彩 填充剩余进空间
        mBitmapShader=new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);
//           空心
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mBitmapHeight=mBitmap.getHeight();
        mBitmapWidth=mBitmap.getWidth();

        Log.d(TAG, "setUp: Bitmap的宽和高："+mBitmapWidth+"//"+mBitmapHeight);
        
        mBorderRect.set(0,0,getWidth(),getHeight());

        Log.d(TAG, "setUp: 获得的控件区域的宽和高："+getWidth()+"//"+getHeight());
//        怎么只是减去一个  borderwidth       ？？？？？？？？？？？？   因为  borderpaint  画笔的宽度是从两边扩展的，所以一个宽度相当于两个......
        mBorderRadius=Math.min((mBorderRect.height() - mBorderWidth) / 2, (mBorderRect.width() - mBorderWidth) / 2);
        Log.d(TAG, "setUp: BorderRadius和BorderWidth"+mBorderRadius+"//"+mBorderWidth);
        mDrawableRect.set(mBorderWidth, mBorderWidth, mBorderRect.width() - mBorderWidth, mBorderRect.height() - mBorderWidth);
        mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2);

        updateShaderMatrix();
        invalidate();     //触发 ondraw
    }

    private void updateShaderMatrix(){
        float scale=1;
        float dx=0;
        float dy=0;

        mShaderMatrix.set(null);
//         保证充满划定区域
        if (mBitmapWidth*mDrawableRect.height()>mDrawableRect.width()*mBitmapHeight){
            scale=mDrawableRect.height()/(float)mBitmapHeight;
            dx=(mDrawableRect.width()-mBitmapWidth*scale)*0.5f;
        }else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale,scale);
        mShaderMatrix.postTranslate((int)(dx+0.5f)+mBorderWidth,(int)(dy+0.5f)+mBorderWidth);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }
//       未知使用
    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

//    尺寸改变时调用，，重要的一批！！！
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setUp();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable()==null){
            return;
        }

        canvas.drawCircle(getWidth()/2,getHeight()/2,mDrawableRadius,mBitmapPaint);
//        这个  半径有疑问！！！！！！
        canvas.drawCircle(getWidth()/2,getHeight()/2,mBorderRadius,mBorderPaint);
        Log.d(TAG, "onDraw: 调用");
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int borderColor) {
        if (borderColor == mBorderColor) {
            return;
        }

        mBorderColor = borderColor;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == mBorderWidth) {
            return;
        }

        mBorderWidth = borderWidth;
        setUp();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        Log.d(TAG, "setImageBitmap: 调用");
        mBitmap = bm;
        setUp();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        Log.d(TAG,"setImageDrawable调用");
        mBitmap = getBitmapFromDrawable(drawable);
        setUp();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        Log.d(TAG, "setImageResource: 被调用");
        mBitmap = getBitmapFromDrawable(getDrawable());
        setUp();
    }

    //    从drawable 获取bitmap
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
//                如果是 colordrawable 的话，， 直接创建一个宽高均为1，黑色的bitmap
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
                Log.d(TAG, "getBitmapFromDrawable: ");
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            Log.d(TAG, "getBitmapFromDrawable: 画布的宽高"+canvas.getWidth()+"//"+canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }finally {
            Log.d(TAG, "getBitmapFromDrawable: 被用");
        }
        
    }


}
 */