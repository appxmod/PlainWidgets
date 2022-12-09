package com.knziha.filepicker.widget;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

import androidx.preference.CMN;

import com.knziha.filepicker.R;
import com.knziha.filepicker.utils.CMNF;

import java.util.ArrayList;

//com.litao.android.CircleCheckBox.CheckBoxSample

/**
 * Created by litao on 16/4/6.
 */
public class CircleCheckBox extends View implements Checkable {

    private final static float BOUNCE_VALUE = 0.2f;

    private ArrayList<Drawable> checkDrawables;

    private Paint bitmapPaint;
    private Paint bitmapEraser;
    private Paint checkEraser;
    private Paint borderPaint;

    private float progress;
    private ObjectAnimator checkAnim;

    private boolean attachedToWindow;
    //public boolean isChecked;
    private int checked;

    private int size = 22;
    private int bitmapColor = 0xFF3F51B5;
    private int borderColor = 0xFFFFFFFF;
    private int mStateCount = 2;
    public boolean drawIconForEmptyState = true;
    /** tint inner drawable as checked */
    public boolean drawInnerForEmptyState = false;
    public boolean noTint = false;
	
	public int mHintLeftPadding,mHintSurrondingPad;
	public float circle_shrinkage;
	private Bitmap drawBitmap;
	private Canvas bitmapCanvas;
	
	public CircleCheckBox(Context context) {
        this(context, null);
    }

    public CircleCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.checkboxStyle);
    }

    public CircleCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }


    @SuppressLint("ResourceType")
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CircleCheckBox, defStyleAttr, 0);
        size = ta.getDimensionPixelSize(R.styleable.CircleCheckBox_size, dp(size));
        bitmapColor = ta.getColor(R.styleable.CircleCheckBox_color_background, bitmapColor);
        borderColor = ta.getColor(R.styleable.CircleCheckBox_color_border, borderColor);
		mHintSurrondingPad = (int) ta.getDimension(R.styleable.CircleCheckBox_dim1, 0);
		Drawable checkDrawable = ta.getDrawable(R.styleable.CircleCheckBox_src0);
		if(checkDrawable==null) checkDrawable = context.getResources().getDrawable(R.drawable.ic_check_black_24dp);
		Drawable checkDrawable2 = ta.getDrawable(R.styleable.CircleCheckBox_src1);

        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setFilterBitmap(true);
        bitmapEraser = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapEraser.setColor(Color.TRANSPARENT);
        bitmapEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        checkEraser = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkEraser.setColor(0);
        checkEraser.setStyle(Paint.Style.STROKE);
        checkEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(1));
        
		checkDrawables = new ArrayList<>(mStateCount-1);
        checkDrawables.add(checkDrawable);
		if(checkDrawable2!=null) checkDrawables.add(checkDrawable2);
        setVisibility(VISIBLE);
        ta.recycle();
    }
	
//	public CircleCheckBox CloneView(int id) {
//		CircleCheckBox new_view = new CircleCheckBox(getContext());
//		new_view.size=size;
//		new_view.bitmapColor=bitmapColor;
//		new_view.borderColor=borderColor;
//
//		new_view.drawIconForEmptyState = drawIconForEmptyState;
//		new_view.drawInnerForEmptyState = drawInnerForEmptyState;
//		new_view.noTint = noTint;
//
//		new_view.mHintLeftPadding=mHintLeftPadding;
//		new_view.circle_shrinkage=circle_shrinkage;
//
//		new_view.setId(id);
//
//		return new_view;
//	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = (int) size;
        int h = (int) size;
        final int pleft = getPaddingLeft();
        final int pright = getPaddingRight();
        final int ptop = getPaddingTop();
        final int pbottom = getPaddingBottom();

        w += pleft + pright;
        h += ptop + pbottom;
        w = Math.max(w, getSuggestedMinimumWidth());
        h = Math.max(h, getSuggestedMinimumHeight());

        int widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        int heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);

        setMeasuredDimension(widthSize, heightSize);
    }
	
	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if (drawBitmap == null && visibility == VISIBLE) {
			int dmm = dp((float) (size*2));
			drawBitmap = Bitmap.createBitmap(dmm, dmm, Bitmap.Config.ARGB_8888);
			bitmapCanvas = new Canvas(drawBitmap);
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		onDrawBox(canvas);
		super.draw(canvas);
	}
	
	PaintFlagsDrawFilter mSetfil = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
    protected void onDrawBox(Canvas canvas) {
		//todo optimise
        if (getVisibility() != VISIBLE) {
            return;
        }
		//讨厌的锯齿消不掉
		canvas.setDrawFilter( mSetfil );
		
        checkEraser.setStrokeWidth(size);

        float rad = size / 2;

        float bitmapProgress = progress >= 0.5f ? 1.0f : progress / 0.5f;
        float checkProgress = progress < 0.5f ? 0.0f : (progress - 0.5f) / 0.5f;

        float p = isChecked() ? progress : (1.0f - progress);

        if (p < BOUNCE_VALUE) {
            rad -= dp(2) * p ;
        } else if (p < BOUNCE_VALUE * 2) {
            rad -= dp(2) - dp(2) * p;
        }
        int pLeft=getPaddingLeft();
        int pTop=getPaddingTop();
        int MeasuredWidth = getMeasuredWidth() - pLeft - getPaddingRight();
        int MeasuredHeight = getMeasuredHeight() - pTop - getPaddingBottom();
		int MeasuredWidthH = MeasuredWidth/2;
		int MeasuredHeightH = MeasuredHeight/2;

        borderPaint.setColor(borderColor);
        canvas.drawCircle(MeasuredWidthH+pLeft, MeasuredHeightH+pTop, rad - dp(1), borderPaint);

        bitmapPaint.setColor(bitmapColor);

        //if(drawInnerForEmptyState)
        //    canvas.drawCircle(MeasuredWidth / 2+pLeft, MeasuredHeight / 2+pTop, rad, bitmapPaint);
		
		//canvas.drawCircle(MeasuredWidth / 2+pLeft, MeasuredHeight / 2+pTop, rad, bitmapPaint);
		//canvas.drawCircle(MeasuredWidth / 2+pLeft, MeasuredHeight / 2+pTop, (rad-circle_shrinkage) * (1 - bitmapProgress), bitmapEraser);
		int bitmapW = drawBitmap.getWidth();
		int bitmapWH = bitmapW / 2;
		bitmapCanvas.drawCircle(bitmapWH, bitmapWH, rad, bitmapPaint);
		bitmapCanvas.drawCircle(bitmapWH, bitmapWH, (rad-circle_shrinkage) * (1 - bitmapProgress), bitmapEraser);
		canvas.drawBitmap(drawBitmap, MeasuredWidthH-bitmapWH+pLeft, MeasuredHeightH-bitmapWH+pTop, bitmapPaint);
		
        int w = size-mHintSurrondingPad;//checkDrawable.getIntrinsicWidth();
        int h = size-mHintSurrondingPad;//checkDrawable.getIntrinsicHeight();
        int x = MeasuredWidthH - w / 2+pLeft;
        int y = MeasuredHeightH - h / 2+pTop;

        Drawable checkDrawable = drawInnerForEmptyState||checked==0?(drawIconForEmptyState ?checkDrawables.get(0):null):checkDrawables.get(checked-1);
        if(checkDrawable!=null) {
            if(!noTint && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            if (isChecked()||drawInnerForEmptyState)
                checkDrawable.setTint(Color.WHITE);
            else
                checkDrawable.setTint(CMNF.ShallowHeaderBlue);
			
			checkDrawable.setBounds(x, y, x + w, y + h);
            //checkDrawable.draw(checkCanvas);
            //checkCanvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, rad * (1 - checkProgress), checkEraser);
			checkDrawable.draw(canvas);
            //canvas.drawBitmap(checkBitmap, 0, 0, null);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }


    public void setProgress(float value) {
        if (progress == value) {
            return;
        }
        progress = value;
        invalidate();
    }

    public void setSize(int size) {
        this.size = size;
    }

    public float getProgress() {
        return progress;
    }

    public void setCheckedColor(int value) {
        bitmapColor = value;
    }

    public int getCheckedColor() {
        return bitmapColor;
    }

    public void setBorderColor(int value) {
        borderColor = value;
        borderPaint.setColor(borderColor);
    }

    private void cancelAnim() {
        if (checkAnim != null) {
            checkAnim.cancel();
        }
    }

    public void addAnim(boolean isChecked) {
        if(checkAnim!=null)
            checkAnim.cancel();
        checkAnim = ObjectAnimator.ofFloat(this, "progress", isChecked ? 1.0f : 0.0f);
        checkAnim.setDuration(300);
        checkAnim.start();
    }

    public void setChecked(int val, boolean animated) {
        val = val%getStateCount();
        if (checked == val) {
            return;
        }
        checked = val;

        if (attachedToWindow && animated) {
            addAnim(isChecked());
        } else {
            cancelAnim();
            setProgress(isChecked() ? 1.0f : 0.0f);
        }
    }

    @Override
    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean animate) {
		setChecked(isChecked()?0:1, animate);
    }

    public void iterate() {
        checked=(checked+1)%getStateCount();
        if (attachedToWindow) {
            addAnim(checked!=0);
        } else {
            cancelAnim();
            setProgress(checked!=0 ? 1.0f : 0.0f);
        }
    }

    public void addStateWithDrawable(Drawable drawable) {
        checkDrawables.add(drawable);
        mStateCount++;
    }

    public void setDrawable(int idx, Drawable drawable) {
        checkDrawables.set(idx, drawable);
    }

    private int getStateCount() {
        return mStateCount;
    }


    @Override
    public void setChecked(boolean b) {
        setChecked(b?1:0, true);
    }

    public void setChecked(boolean b, boolean animated) {
        setChecked(b?1:0, animated);
    }

    public void setChecked(int val) {
        setChecked(val, true);
    }

    @Override
    public boolean isChecked() {
        return  checked > 0;
    }

    public int getChecked() {
        return  checked;
    }

    public int dp(float value) {
        if (value == 0) {
            return 0;
        }
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) Math.ceil(density * value);
    }
}