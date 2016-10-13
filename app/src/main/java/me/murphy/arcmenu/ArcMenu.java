package me.murphy.arcmenu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.NavUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by murphy on 16/10/11.
 * 自定义ArcMenu练手
 */

public class ArcMenu extends ViewGroup implements View.OnClickListener {
    /**
     * Menu的缺省位置
     */
    private Position mPosition = Position.Right_Bottom;
    /**
     * Menu的缺省状态
     */
    private State mCurrentState = State.CLOSE;
    /**
     * 主Button
     */
    private View mCButton;
    /**
     * 卫星button距离主button的距离
     */
    private int mRadius;

    /**
     * 点击子菜单的回调事件
     */
    private MenuClickListener menuClickListener;

    public enum Position {
        Left_Top, Left_Bottom, Right_top, Right_Bottom
    }

    public enum State {
        CLOSE, OPEN
    }

    public ArcMenu(Context context) {
        this(context, null);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float dp100 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                100, getResources().getDisplayMetrics());
        mRadius = (int) dp100;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ArcMenu, defStyleAttr, 0);
        int pos = array.getInt(R.styleable.ArcMenu_currentPosition, 3);
        int state = array.getInt(R.styleable.ArcMenu_currentState, 0);
        mRadius = (int) array.getDimension(R.styleable.ArcMenu_radius, dp100);
        switch (pos) {
            case 0:
                mPosition = Position.Left_Top;
                break;
            case 1:
                mPosition = Position.Left_Bottom;
                break;
            case 2:
                mPosition = Position.Right_top;
                break;
            case 3:
                mPosition = Position.Right_Bottom;
                break;

        }
        switch (state) {
            case 0:
                mCurrentState = State.CLOSE;
                break;
            case 1:
                mCurrentState = State.OPEN;
                break;
        }
        array.recycle();
    }

    /**
     * 对每个子View进行测量
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        if (count < 4) {
            throw new RuntimeException("ChildView must be at least 4 view");
        }
        for (int i = 0; i < count; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            layoutCButton();
            layoutMenuButton();
        }
    }

    /**
     * 对主button进行定位
     */
    private void layoutCButton() {
        mCButton = getChildAt(0);
        mCButton.setOnClickListener(this);
        int left = 0;
        int top = 0;
        int width = mCButton.getMeasuredWidth();
        int height = mCButton.getMeasuredHeight();
        switch (mPosition) {
            case Left_Top:
                left = 0;
                top = 0;
                break;
            case Left_Bottom:
                left = 0;
                top = getMeasuredHeight() - height;
                break;
            case Right_top:
                left = getMeasuredWidth() - width;
                top = 0;
                break;
            case Right_Bottom:
                left = getMeasuredWidth() - width;
                top = getMeasuredHeight() - height;
                break;
        }
        mCButton.layout(left, top, left + width, top + height);
    }

    /**
     * 对子菜单进行定位
     */
    private void layoutMenuButton() {

        /**
         * 子按钮的数量是所有childView的数量-主按钮的数量
         */
        int count = getChildCount() - 1;
        /**
         *对所有的子按钮进行遍历布局
         */
        for (int i = 0; i < count; i++) {
            View childButton = getChildAt(i + 1);
            childButton.setOnClickListener(this);
            childButton.setAlpha(0);
            childButton.setClickable(false);
            childButton.setFocusable(false);
            int childWidth = childButton.getMeasuredWidth();
            int childHeight = childButton.getMeasuredHeight();
            int childLeft = mCButton.getLeft() + (mCButton.getMeasuredWidth() - childWidth) / 2;
            int childTop = mCButton.getTop() + (mCButton.getMeasuredHeight() - childHeight) / 2;
            childButton.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }
    }

    /**
     * 打开卫星菜单执行动画
     */
    private void openMenu() {
        AnimatorSet animatorSet = new AnimatorSet();
        int count = getChildCount() - 1;
        for (int i = 0; i < count; i++) {
            View childButton = getChildAt(i + 1);
            int destinationX = (int) (Math.sin(Math.PI / 2 / (count - 1) * i) * mRadius);
            int destinationY = (int) (Math.cos(Math.PI / 2 / (count - 1) * i) * mRadius);
            switch (mPosition) {
                case Left_Bottom:
                    destinationY = -destinationY;
                    break;
                case Right_top:
                    destinationX = -destinationX;
                    break;
                case Right_Bottom:
                    destinationY = -destinationY;
                    destinationX = -destinationX;
                    break;
            }
            ObjectAnimator animator = ObjectAnimator.ofFloat(childButton, "translationX", 0, destinationX);
            ObjectAnimator translationYanimator = ObjectAnimator.ofFloat(childButton, "translationY", 0, destinationY);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(childButton, "alpha", 0f, 1f);
            animator.setStartDelay(i * 50);
            translationYanimator.setStartDelay(i * 50);
            alpha.setStartDelay(i * 50);
            animatorSet.play(animator).with(translationYanimator).with(alpha);
        }
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    view.setFocusable(false);
                    view.setClickable(false);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    view.setFocusable(true);
                    view.setClickable(true);
                }
                mCurrentState = State.OPEN;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    view.setFocusable(true);
                    view.setClickable(true);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    /**
     * 关闭卫星菜单时执行动画
     */
    private void closeMenu() {
        AnimatorSet animatorSet = new AnimatorSet();
        int count = getChildCount() - 1;
        for (int i = 0; i < count; i++) {
            View childButton = getChildAt(i + 1);
            int destinationX = (int) (Math.sin(Math.PI / 2 / (count - 1) * i) * mRadius);
            int destinationY = (int) (Math.cos(Math.PI / 2 / (count - 1) * i) * mRadius);
            switch (mPosition) {
                case Left_Bottom:
                    destinationY = -destinationY;
                    break;
                case Right_top:
                    destinationX = -destinationX;
                    break;
                case Right_Bottom:
                    destinationY = -destinationY;
                    destinationX = -destinationX;
                    break;
            }
            ObjectAnimator animator = ObjectAnimator.ofFloat(childButton, "translationX", destinationX, 0);
            ObjectAnimator translationYanimator = ObjectAnimator.ofFloat(childButton, "translationY", destinationY, 0);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(childButton, "alpha", 1f, 0f);
            animator.setStartDelay(i * 50);
            translationYanimator.setStartDelay(i * 50);
            alpha.setStartDelay(i * 50);
            animatorSet.play(animator).with(translationYanimator).with(alpha);
        }
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    view.setFocusable(false);
                    view.setClickable(false);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCButton.setFocusable(true);
                mCButton.setClickable(true);
                mCurrentState = State.CLOSE;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCButton.setFocusable(true);
                mCButton.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();

    }

    public void setOnMenuClickListener(MenuClickListener menuClickListener) {
        this.menuClickListener = menuClickListener;
    }

    @Override
    public void onClick(View v) {
        if (v == mCButton) {
            if (mCurrentState.equals(State.CLOSE)) {
                openMenu();
                return;
            }
            if (mCurrentState.equals(State.OPEN)) {
                closeMenu();
                return;
            }
        }
        for (int i = 0; i < getChildCount() - 1; i++) {
            if (v == getChildAt(i + 1)) {
                if (menuClickListener == null)
                    return;
                menuClickListener.onMenuClick(i);
                closeMenu();
            }
        }

    }


    public interface MenuClickListener {
        void onMenuClick(int pos);
    }
}
