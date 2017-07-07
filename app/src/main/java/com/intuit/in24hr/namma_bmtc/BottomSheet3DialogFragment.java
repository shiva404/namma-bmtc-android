package com.intuit.in24hr.namma_bmtc;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.intuit.in24hr.namma_bmtc.types.LocationPage;
import com.intuit.in24hr.namma_bmtc.types.ThanksCount;

import static com.google.android.gms.internal.zzir.runOnUiThread;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_ERROR;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_FINISHED;
import static com.intuit.in24hr.namma_bmtc.Constants.STATUS_RUNNING;

/**
 * Created by sn1 on 7/7/17.
 */

public class BottomSheet3DialogFragment extends BottomSheetDialogFragment implements FetchThanksResultReceiver.Receiver {
    TextView thanksCountTextView = null;

    @Override
    public void onFetchThanksCountResult(int resultCode, final Bundle resultData) {

        switch (resultCode) {
            case STATUS_RUNNING:
                break;
            case STATUS_FINISHED:
                Thread timer = new Thread(){
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ThanksCount thanksCount = (ThanksCount) resultData.getSerializable("thanksCount");
                                if(thanksCount != null) {
                                    System.out.println("Got thanks count -->" + thanksCount.getThanksCount());
                                    thanksCountTextView.setText(String.valueOf(thanksCount.getThanksCount()));
                                } else {
                                    System.out.println("Got thanks as null.. WTF !! ");
                                }
                            }
                        });
                    }
                };
                timer.start();
                break;

            case STATUS_ERROR:
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public static interface BottomSheetActionsListener {
        void handleStopSharingAction();
    }

    BottomSheetActionsListener bottomSheetActionsListener;

    public BottomSheet3DialogFragment(BottomSheetActionsListener bottomSheetActionsListener) {
        this.bottomSheetActionsListener = bottomSheetActionsListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setPeekHeight(300);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.bottom_page, null);
        FloatingActionButton stopSharing = (FloatingActionButton) contentView.findViewById(R.id.stop_share);
        thanksCountTextView = (TextView) contentView.findViewById(R.id.thanks_count);
        stopSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Stop Been clicked ----");
                bottomSheetActionsListener.handleStopSharingAction();
            }
        });

        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }
}
