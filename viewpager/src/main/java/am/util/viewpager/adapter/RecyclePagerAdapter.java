/*
 * Copyright (C) 2015 AlexMofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package am.util.viewpager.adapter;

import android.content.res.Configuration;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ConfigurationHelper;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * RecyclePagerAdapter
 * Created by Alex on 2016/3/16.
 */
@SuppressWarnings("all")
public abstract class RecyclePagerAdapter<VH extends RecyclePagerAdapter.PagerViewHolder>
        extends PagerAdapter {

    private ArrayList<VH> holderList = new ArrayList<>();
    private SparseArray<ArrayList<VH>> holderSparse = new SparseArray<>();

    @Override
    public final int getCount() {
        return getItemCount();
    }

    @Override
    public final boolean isViewFromObject(View view, Object object) {
        return view == ((VH) object).itemView;
    }

    public final Object instantiateItem(ViewGroup container, int position) {
        VH holder;
        int viewType = getItemViewType(position);
        ArrayList<VH> recycleHolders = holderSparse.get(viewType);
        if (recycleHolders != null && recycleHolders.size() > 0) {
            holder = recycleHolders.remove(0);
            holder.mPosition = POSITION_UNCHANGED;
        } else {
            holder = createViewHolder(container, viewType);
        }
        bindViewHolder(holder, position);
        container.addView(holder.itemView, 0);
        return holder;
    }

    public final void destroyItem(ViewGroup container, int position, Object object) {
        VH holder = (VH) object;
        container.removeView(holder.itemView);
        holder.isRecycled = true;
        holder.mPosition = POSITION_NONE;
        int viewType = getItemViewType(position);
        ArrayList<VH> recycleHolders = holderSparse.get(viewType, new ArrayList<VH>());
        recycleHolders.add(holder);
        holderSparse.put(viewType, recycleHolders);
        onViewRecycled(holder);
    }

    @Override
    @Deprecated
    public final Object instantiateItem(View container, int position) {
        return instantiateItem((ViewPager) container, position);
    }

    @Override
    @Deprecated
    public final void destroyItem(View container, int position, Object object) {
        destroyItem((ViewPager) container, position, object);
    }

    @Override
    public final int getItemPosition(Object object) {
        int position = POSITION_UNCHANGED;
        if (object != null) {
            VH holder = (VH) object;
            if (holderList.contains(holder)) {
                position = holder.mPosition;
                position = position >= getItemCount() ? POSITION_NONE : position;
            }
        }
        return position;
    }

    public abstract int getItemCount();

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindViewHolder(VH holder, int position);

    public final VH createViewHolder(ViewGroup parent, int viewType) {
        final VH holder = onCreateViewHolder(parent, viewType);
        holderList.add(holder);
        return holder;
    }

    public final void bindViewHolder(VH holder, int position) {
        holder.mPosition = position;
        holder.isRecycled = false;
        onBindViewHolder(holder, position);
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public void onViewRecycled(VH holder) {
    }

    @Override
    public final void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for (VH holder : holderList) {
            if (!holder.isRecycled && holder.mPosition < getItemCount()) {
                onBindViewHolder(holder, holder.mPosition);
            }
        }
    }

    public final void notifyItemChanged(int position) {
        for (VH holder : holderList) {
            if (!holder.isRecycled && holder.mPosition == position) {
                onBindViewHolder(holder, holder.mPosition);
                break;
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        final int count = holderSparse.size();
        for (int i = 0; i < count; i++) {
            final ArrayList<VH> holders = holderSparse.valueAt(i);
            if (holders == null || holders.isEmpty())
                continue;
            for (VH holder : holders) {
                if (holder == null || holder.itemView == null)
                    continue;
                ConfigurationHelper.onConfigurationChanged(holder.itemView, newConfig);
            }
        }
    }

    public static abstract class PagerViewHolder {
        public final View itemView;
        int mPosition = POSITION_UNCHANGED;
        boolean isRecycled = false;

        public PagerViewHolder(View itemView) {
            this.itemView = itemView;
        }
    }
}
