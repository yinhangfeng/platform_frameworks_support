/*
 * Copyright 2018 The Android Open Source Project
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

package com.example.android.supportv7.widget;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.supportv7.R;
import com.example.android.supportv7.widget.util.ConfigToggle;

public class SimpleRecyclerViewActivity extends BaseLayoutManagerActivity<LinearLayoutManager> {

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_simple_recycler_view, parent, false);

                ViewHolder viewHolder = new ViewHolder(view);
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((ViewHolder) holder).mTextView.setText("position:" + position);
            }

            @Override
            public int getItemCount() {
                return 20;
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.text1);
        }
    }

    @Override
    protected void onRecyclerViewInit(RecyclerView recyclerView) {

    }

    @Override
    protected ConfigToggle[] createConfigToggles() {
        return new ConfigToggle[]{
                new ConfigToggle(this, R.string.checkbox_orientation) {
                    @Override
                    public boolean isChecked() {
                        return mLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL;
                    }

                    @Override
                    public void onChange(boolean newValue) {
                        mLayoutManager.setOrientation(newValue ? LinearLayoutManager.HORIZONTAL
                                : LinearLayoutManager.VERTICAL);

                    }
                },
                new ConfigToggle(this, R.string.checkbox_reverse) {
                    @Override
                    public boolean isChecked() {
                        return mLayoutManager.getReverseLayout();
                    }

                    @Override
                    public void onChange(boolean newValue) {
                        mLayoutManager.setReverseLayout(newValue);
                    }
                },
                new ConfigToggle(this, R.string.checkbox_layout_dir) {
                    @Override
                    public boolean isChecked() {
                        return ViewCompat.getLayoutDirection(mRecyclerView) ==
                                ViewCompat.LAYOUT_DIRECTION_RTL;
                    }

                    @Override
                    public void onChange(boolean newValue) {
                        ViewCompat.setLayoutDirection(mRecyclerView, newValue ?
                                ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
                    }
                },
                new ConfigToggle(this, R.string.checkbox_stack_from_end) {
                    @Override
                    public boolean isChecked() {
                        return mLayoutManager.getStackFromEnd();
                    }

                    @Override
                    public void onChange(boolean newValue) {
                        mLayoutManager.setStackFromEnd(newValue);
                    }
                }
        };
    }
}
