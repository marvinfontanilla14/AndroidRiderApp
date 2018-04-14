package com.example.khean07.androidriderapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Khean07 on 4/8/2018.
 */

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {

    String mTag;

    public static BottomSheetRiderFragment newInstance(String tag) {

        Bundle args = new Bundle();

        BottomSheetRiderFragment fragment = new BottomSheetRiderFragment();
        args.putString("TAG",tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString("TAG");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_rider,container,false);
        return view;
    }
}
