package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeListener implements View.OnTouchListener{
    private final GestureDetector gestureDetector;

    public OnSwipeListener( Context context) {
        this.gestureDetector = new GestureDetector( context , new SwipeGestureListener() );
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent( event );
    }

    public void OnSwipeBottom() { }
    public void OnSwipeLeft()   { }
    public void OnSwipeRight()  { }
    public void OnSwipeTop()    { }

    private final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener{
        private final static int minVelocity = 150;
        private final static int minDistance = 100;
        private final static double minRatio = 1.0 / 2.0;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean isHandled = false;
            if(e1 == null) return false;
            float deltaX = e2.getX() - e1.getX();
            float deltaY = e2.getY() - e1.getY();
            float distanceX = Math.abs(deltaX);
            float distanceY = Math.abs(deltaY);
            if( distanceX * minRatio > distanceY && distanceX >= minDistance){
                if( Math.abs( velocityX ) >= minVelocity ){
                    if( deltaX > 0){
                        OnSwipeRight();
                    }
                    else{
                        OnSwipeLeft();
                    }
                    isHandled = true;
                }
            }
            else if (distanceY * minRatio > distanceX && distanceY >= minDistance){
                if( Math.abs( velocityY ) >= minVelocity ){
                    if( deltaY > 0){
                        OnSwipeBottom();
                    }
                    else{
                        OnSwipeTop();
                    }

                }
            }
            return  isHandled;
        }
    }
}
