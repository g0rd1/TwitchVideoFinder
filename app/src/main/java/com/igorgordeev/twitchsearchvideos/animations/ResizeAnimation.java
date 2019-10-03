package com.igorgordeev.twitchsearchvideos.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation {

	private final int targetHeight;
	private final View view;
	private final int startHeight;

	public ResizeAnimation(View view, int targetHeight, int startHeight) {
		this.view = view;
		this.targetHeight = targetHeight;
		this.startHeight = startHeight;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
	}

	@Override
	public boolean willChangeBounds() {
		return true;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		view.getLayoutParams().height = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
		view.requestLayout();
	}


}
