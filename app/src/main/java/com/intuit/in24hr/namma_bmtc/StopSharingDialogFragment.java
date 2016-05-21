package com.intuit.in24hr.namma_bmtc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by sn1 on 5/19/16.
 */
public class StopSharingDialogFragment  extends DialogFragment {
    public static StopSharingDialogFragment newInstance() {
        StopSharingDialogFragment frag = new StopSharingDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    public static interface StopSharingDialogListener {
        public void onStopShareDialogPositiveClick(StopSharingDialogFragment dialog);
    }

    public StopSharingDialogFragment(){
    }

    StopSharingDialogListener listener;

    @Override
    public void onAttach(Activity activity){
        System.out.println("On create view attach ############");
        super.onAttach(activity);
        try{
            listener = (StopSharingDialogListener) activity;
        }catch(ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement StartPayperiodDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View inflate = inflater.inflate(R.layout.stop_sharing, null);
        alertDialogBuilder.setView(inflate);
        alertDialogBuilder.setPositiveButton("Stop Sharing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onStopShareDialogPositiveClick(StopSharingDialogFragment.this);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return alertDialogBuilder.create();
    }
}
