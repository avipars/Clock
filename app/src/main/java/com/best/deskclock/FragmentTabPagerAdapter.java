/*
 * Copyright (C) 2016 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.best.deskclock;

import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;

import com.best.deskclock.uidata.UiDataModel;

import java.util.Map;

/**
 * This adapter produces the DeskClockFragments that are the content of the DeskClock tabs. The
 * adapter presents the tabs in LTR and RTL order depending on the text layout direction for the
 * current locale. To prevent issues when switching between LTR and RTL, fragments are registered
 * with the manager using position-independent tags, which is an important departure from
 * FragmentPagerAdapter.
 */
final class FragmentTabPagerAdapter extends PagerAdapter {

    private final DeskClock mDeskClock;

    /**
     * The manager into which fragments are added.
     */
    private final FragmentManager mFragmentManager;

    /**
     * A fragment cache that can be accessed before {@link #instantiateItem} is called.
     */
    private final Map<UiDataModel.Tab, DeskClockFragment> mFragmentCache;

    /**
     * The active fragment transaction if one exists.
     */
    private FragmentTransaction mCurrentTransaction;

    /**
     * The current fragment displayed to the user.
     */
    private Fragment mCurrentPrimaryItem;

    FragmentTabPagerAdapter(DeskClock deskClock) {
        mDeskClock = deskClock;
        mFragmentCache = new ArrayMap<>(getCount());
        mFragmentManager = deskClock.getSupportFragmentManager();
    }

    @Override
    public int getCount() {
        return UiDataModel.getUiDataModel().getTabCount();
    }

    /**
     * @param position the left-to-right index of the fragment to be returned
     * @return the fragment displayed at the given {@code position}
     */
    DeskClockFragment getDeskClockFragment(int position) {
        // Fetch the tab the UiDataModel reports for the position.
        final UiDataModel.Tab tab = UiDataModel.getUiDataModel().getTabAt(position);

        // First check the local cache for the fragment.
        DeskClockFragment fragment = mFragmentCache.get(tab);
        if (fragment != null) {
            return fragment;
        }

        // Next check the fragment manager; relevant when app is rebuilt after locale changes
        // because this adapter will be new and mFragmentCache will be empty, but the fragment
        // manager will retain the Fragments built on original application launch.
        fragment = (DeskClockFragment) mFragmentManager.findFragmentByTag(tab.name());
        if (fragment != null) {
            fragment.setFabContainer(mDeskClock);
            mFragmentCache.put(tab, fragment);
            return fragment;
        }

        // Otherwise, build the fragment from scratch.
        final String fragmentClassName = tab.getFragmentClassName();
        FragmentFactory fragmentFactory = mFragmentManager.getFragmentFactory();
        fragment = (DeskClockFragment) fragmentFactory.instantiate(mDeskClock.getClassLoader(), fragmentClassName);
        fragment.setFabContainer(mDeskClock);
        mFragmentCache.put(tab, fragment);
        return fragment;
    }

    @Override
    public void startUpdate(ViewGroup container) {
        if (container.getId() == View.NO_ID) {
            throw new IllegalStateException("ViewPager with adapter " + this + " has no id");
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction();
        }

        // Use the fragment located in the fragment manager if one exists.
        final UiDataModel.Tab tab = UiDataModel.getUiDataModel().getTabAt(position);
        Fragment fragment = mFragmentManager.findFragmentByTag(tab.name());
        if (fragment != null) {
            mCurrentTransaction.attach(fragment);
        } else {
            fragment = getDeskClockFragment(position);
            mCurrentTransaction.add(container.getId(), fragment, tab.name());
        }

        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
        }

        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction();
        }
        final DeskClockFragment fragment = (DeskClockFragment) object;
        fragment.setFabContainer(null);
        mCurrentTransaction.detach(fragment);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        final Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
            }

            fragment.setMenuVisibility(true);
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        if (mCurrentTransaction != null) {
            mCurrentTransaction.commitAllowingStateLoss();
            mCurrentTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return ((Fragment) object).getView() == view;
    }
} 
