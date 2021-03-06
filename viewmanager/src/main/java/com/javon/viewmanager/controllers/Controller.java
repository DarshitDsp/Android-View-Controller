package com.javon.viewmanager.controllers;

import android.content.Context;
import android.view.View;

import com.javon.viewmanager.animators.ControllerAnimator;
import com.javon.viewmanager.animators.LeftFlipAnimator;
import com.javon.viewmanager.animators.RightFlipAnimator;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author Javon Davis
 *         Created by Javon Davis on 21/02/16.
 */
public class Controller {

    private ControllerAnimator mDefaultForwardAnimation;
    private ControllerAnimator mDefaultBackwardAnimation;

    private ArrayList<View> mViews;
    private ListIterator<View> iterator;
    private boolean mLoop;
    private ViewListener listener;
    private boolean mUseDefaultListener;

    private ControllerListener mListener; // Considered using a WeakReference to it but nah, if I
                                         // decided to accommodate more than one Listeners a WeakHashmap might be appropriate

    /**
     * This constructor will use the default listener and also not loop around
     * @param context - application Context
     * @param views - list of views to be used in the correct order
     */
    public Controller(Context context, ArrayList<View> views)
    {
        this(context,views,true,false);
    }

    /**
     * @param context - application Context
     * @param views - list of views to be used in the correct order
     * @param useDefaultListeners - if true the default onClick listener will be used which is an onClick listener on the entire view
     * @param loop - if true when the next view after the last view in the list will be the first view
     */
    public Controller(Context context, ArrayList<View> views, boolean useDefaultListeners,boolean loop)
    {
        this(context,views,useDefaultListeners,loop,null,null);
    }

    /**
     * This constructor will use the default listener and also not loop around
     * @param context - application Context
     * @param views - list of views to be used in the correct order
     * @param defaultForwardAnimation - animation to be used whenever going to the next view in the list, if null the default is used
     * @param defaultBackwardAnimation - animation to be used whenever going back to the previous view in the list, if null the default is used
     */
    public Controller(Context context, ArrayList<View> views, ControllerAnimator defaultForwardAnimation, ControllerAnimator defaultBackwardAnimation)
    {
        this(context,views,true,false,defaultForwardAnimation,defaultBackwardAnimation);
    }

    /**
     *
     * @param context - application Context
     * @param views - list of views to be used in the correct order
     * @param useDefaultListeners - if true, the default onClick listener will be used which is an onClick listener on the entire view
     * @param loop - if true it will loop around the list of views and start from the beginning
     * @param defaultForwardAnimation - animation to be used whenever going to the next view in the list, if null the default is used
     * @param defaultBackwardAnimation - animation to be used whenever going back to the previous view in the list, if null the default is used
     */
    public Controller(Context context, ArrayList<View> views, boolean useDefaultListeners, boolean loop, ControllerAnimator defaultForwardAnimation, ControllerAnimator defaultBackwardAnimation) {
        this.mViews = views;
        this.iterator = views.listIterator();

        listener = new ViewListener();

        this.mUseDefaultListener = useDefaultListeners;

        if(useDefaultListeners)
            views.get(0).setOnClickListener(listener);

        this.mLoop = loop;

        if((defaultForwardAnimation == null || defaultBackwardAnimation == null) && context == null)
            throw new NullPointerException("null Context value cannot be used with default animator");

        if(defaultForwardAnimation == null)
            this.mDefaultForwardAnimation = new RightFlipAnimator(context);
        else
            this.mDefaultForwardAnimation = defaultForwardAnimation;

        if(defaultBackwardAnimation == null)
            this.mDefaultBackwardAnimation = new LeftFlipAnimator(context);
        else
            this.mDefaultBackwardAnimation = defaultBackwardAnimation;
    }

    /**
     * Go to the next view
     */
    public void next()
    {
        if(iterator.hasNext()) {
            View currentView = iterator.next();
            if(isUsingDefaultListener())
                currentView.setOnClickListener(null);

            if (iterator.nextIndex() < mViews.size()) {
                View nextView = mViews.get(iterator.nextIndex());

                if(isUsingDefaultListener())
                    nextView.setOnClickListener(listener);

                ControllerAnimator animator = getDefaultForwardAnimation();

                animator.setOldView(currentView);
                animator.setNewView(nextView);

                currentView.startAnimation(animator);

                if(iterator.nextIndex() == mViews.size() - 1)
                    if(hasControllerListener())
                        mListener.onEndReached();

            }
            else
            {
                if(mLoop)
                {
                    iterator = mViews.listIterator();
                    View nextView = mViews.get(0);
                    if(mUseDefaultListener)
                        nextView.setOnClickListener(listener);

                    ControllerAnimator animator = getDefaultForwardAnimation();

                    animator.setOldView(currentView);
                    animator.setNewView(nextView);

                    currentView.startAnimation(animator);
                }
            }
        }
    }

    /**
     * Go to the next view using the animator passed in
     * @param animator - the animator to use for the next view change
     */
    public void next(ControllerAnimator animator){
        if(animator == null)
            throw new NullPointerException("Animator cannot be null");

        if(iterator.hasNext()) {
            View currentView = iterator.next();
            if(isUsingDefaultListener())
                currentView.setOnClickListener(null);

            if (iterator.nextIndex() < mViews.size()) {
                View nextView = mViews.get(iterator.nextIndex());
                if(isUsingDefaultListener())
                    nextView.setOnClickListener(listener);
                animator.setOldView(currentView);
                animator.setNewView(nextView);

                currentView.startAnimation(animator);

                if(iterator.nextIndex() == mViews.size() - 1)
                    if(hasControllerListener())
                        mListener.onEndReached();
            } else {
                if (mLoop) {
                    iterator = mViews.listIterator();
                    View nextView = mViews.get(0);
                    if(mUseDefaultListener)
                        nextView.setOnClickListener(listener);

                    animator.setOldView(currentView);
                    animator.setNewView(nextView);

                    currentView.startAnimation(animator);
                }
            }
        }
    }

    /**
     * Go back to the previous view
     */
    public void previous()
    {
        if(iterator.hasPrevious())
        {
            View currentView = mViews.get(iterator.nextIndex());

            if(iterator.nextIndex() > 0) {
                View previousView = iterator.previous();

                if(isUsingDefaultListener())
                    previousView.setOnClickListener(listener);

                ControllerAnimator animator = getDefaultBackwardAnimation();

                animator.setOldView(currentView);
                animator.setNewView(previousView);
                currentView.startAnimation(animator);
            }

        }
        else
        {
            if(mLoop)
            {
                iterator = mViews.listIterator();
                for(int i = 0; i< mViews.size()-1;i++)
                {
                    iterator.next();
                }

                View currentView = mViews.get(0);
                View previousView = mViews.get(mViews.size()-1);
                if(mUseDefaultListener)
                    previousView.setOnClickListener(listener);

                ControllerAnimator animator = getDefaultBackwardAnimation();

                animator.setOldView(currentView);
                animator.setNewView(previousView);
                currentView.startAnimation(animator);
            }
        }
    }

    /**
     * Go back to the previous view
     * @param animator - the animator to be used for the transition
     */
    public void previous(ControllerAnimator animator)
    {
        if(animator == null)
            throw new NullPointerException("Animator cannot be null");

        if(iterator.hasPrevious())
        {

            View currentView = mViews.get(iterator.nextIndex());

            if(iterator.nextIndex() > 0) {
                View previousView = iterator.previous();

                if(isUsingDefaultListener())
                    previousView.setOnClickListener(listener);

                animator.setOldView(currentView);
                animator.setNewView(previousView);
                currentView.startAnimation(animator);
            }

        }
        else
        {
            if(mLoop)
            {
                iterator = mViews.listIterator();
                for(int i = 0; i< mViews.size()-1;i++)
                {
                    iterator.next();
                }

                View currentView = mViews.get(0);
                View previousView = mViews.get(mViews.size()-1);

                if(mUseDefaultListener)
                    previousView.setOnClickListener(listener);

                animator.setOldView(currentView);
                animator.setNewView(previousView);
                currentView.startAnimation(animator);
            }
        }
    }

    /**
     *
     * @return the default animation for next
     */
    public ControllerAnimator getDefaultForwardAnimation() {
        return mDefaultForwardAnimation;
    }

    /**
     *
     * @param defaultForwardAnimation the default animation for next
     */
    public void setDefaultForwardAnimation(Context context, ControllerAnimator defaultForwardAnimation) {
        if(defaultForwardAnimation == null && context == null)
            throw new NullPointerException("null Context value cannot be used with default animator");

        if(defaultForwardAnimation == null)
            this.mDefaultForwardAnimation = new RightFlipAnimator(context);
        else
            this.mDefaultForwardAnimation = defaultForwardAnimation;
    }


    /**
     * Set the duration for the default forward animation
     * @param duration - duration in milliseconds
     */
    public void setForwardAnimationDuration(long duration)
    {
        getDefaultForwardAnimation().setDuration(duration);
    }

    /**
     * Set the duration for the default backward animation
     * @param duration - duration in milliseconds
     */
    public void setBackwardAnimationDuration(long duration)
    {
        getDefaultForwardAnimation().setDuration(duration);
    }

    /**
     *
     * @return the next view
     */
    public View getNextView()
    {
        return mViews.get(iterator.nextIndex());
    }

    /**
     *
     * @return the current view
     */
    public View getCurrentView()
    {
        return mViews.get(iterator.previousIndex()+1);
    }

    /**
     *
     * @return the default animation for previous
     */
    public ControllerAnimator getDefaultBackwardAnimation() {
        return mDefaultBackwardAnimation;
    }

    /**
     *
     * @param defaultBackwardAnimation the default animation for previous
     */
    public void setDefaultBackwardAnimation(Context context,ControllerAnimator defaultBackwardAnimation) {
        if( defaultBackwardAnimation == null && context == null)
            throw new NullPointerException("null Context value cannot be used with default animator");

        if(defaultBackwardAnimation == null)
            this.mDefaultBackwardAnimation = new LeftFlipAnimator(context);
        else
            this.mDefaultBackwardAnimation = defaultBackwardAnimation;
    }

    /**
     *
     * @return - all the views
     */
    public ArrayList<View> getViews() {
        return mViews;
    }

    /**
     *
     * @param views - the views to be used
     */
    public void setViews(ArrayList<View> views) {
        this.mViews = views;
    }

    public void addToViews(View view)
    {
        if(view == null)
            throw new NullPointerException("Cannot add null view to list");

        if(mViews == null)
            throw new NullPointerException("No List for views has been set");

        this.mViews.add(view);
    }

    /**
     *
     * @return - the iterator being used on the views
     */
    public ListIterator<View> getIterator() {
        return iterator;
    }

    /**
     *
     * @param iterator - the iterator to be used on the views
     */
    public void setIterator(ListIterator<View> iterator) {
        this.iterator = iterator;
    }

    /**
     * Similar to getting the iterator and calling hasNext on it
     * @return if the iterator has a next view
     */
    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    /**
     * Similar to getting the iterator and calling hasPrevious on it
     * @return if the iterator has a previous view
     */
    public boolean hasPrevious()
    {
        return iterator.hasPrevious();
    }

    public boolean isUsingDefaultListener() {
        return mUseDefaultListener;
    }

    public void useDefaultListener(boolean mUseDefaultListener) {
        this.mUseDefaultListener = mUseDefaultListener;
    }

    public boolean hasControllerListener()
    {
        return mListener != null;
    }

    public void setControllerListener(ControllerListener listener)
    {
        this.mListener = listener;
    }

    /**
     * Default view listener
     */
    private class ViewListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            next();
        }
    }

    public interface ControllerListener
    {
        void onEndReached();
    }
}
