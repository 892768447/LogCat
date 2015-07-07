package zce.log.cat.widget;

import zce.log.cat.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AutoCompleteTextView extends android.widget.AutoCompleteTextView
		implements android.view.View.OnFocusChangeListener, TextWatcher {

	/**
	 * 删除按钮的引用
	 */
	private Drawable clearDrawable;

	/**
	 * 控件是否有焦点
	 */
	private boolean hasFoucs;

	public AutoCompleteTextView(Context context) {
		this(context, null);
		// super(context);
		// TODO Auto-generated constructor stub
	}

	public AutoCompleteTextView(Context context, AttributeSet attrs) {
		// 这里构造方法也很重要，不加这个很多属性不能再XML里面定义
		this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
		// super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AutoCompleteTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
		// TODO Auto-generated constructor stub
	}

	private void init() {
		// 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
		if (clearDrawable == null) {
			clearDrawable = getResources().getDrawable(R.drawable.edit_clear);
		}
		clearDrawable.setBounds(0, 0, clearDrawable.getIntrinsicWidth(),
				clearDrawable.getIntrinsicHeight());
		// 默认隐藏图片
		setClearIconVisible(false);
		// 设置焦点改变的监听
		setOnFocusChangeListener(this);
		// 设置输入框里面内容发生改变的监听
		addTextChangedListener(this);
	}

	/**
	 * 因为我们不能直接给AutoCompleteTextView设置点击事件，所以我们用记住我们按下的位置来模拟点击事件 当我们按下的位置 在
	 * AutoCompleteTextView的宽度 - 图标到控件右边的间距 - 图标的宽度 和 AutoCompleteTextView的宽度 -
	 * 图标到控件右边的间距之间我们就算点击了图标，竖直方向就没有考虑
	 */

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (clearDrawable != null && event.getAction() == MotionEvent.ACTION_UP) {
			if (getCompoundDrawables()[2] != null) {// 右边的图片
				boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
						&& (event.getX() < ((getWidth() - getPaddingRight())));
				if (touchable) {
					this.setText("");
				}
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 当EditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		this.setHasFoucs(hasFocus);
		if (hasFocus) {
			setClearIconVisible(getText().length() > 0);
		} else {
			setClearIconVisible(false);
		}
	}

	/**
	 * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
	 * 
	 * @param visible
	 */
	protected void setClearIconVisible(boolean visible) {
		Drawable right = visible ? clearDrawable : null;
		setCompoundDrawables(getCompoundDrawables()[0],
				getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
	}

	/**
	 * 当输入框里面内容发生变化的时候回调的方法
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int count, int after) {
		setClearIconVisible(s.length() > 0);
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	public boolean getHasFoucs() {
		return hasFoucs;
	}

	public void setHasFoucs(boolean hasFoucs) {
		this.hasFoucs = hasFoucs;
	}

}
