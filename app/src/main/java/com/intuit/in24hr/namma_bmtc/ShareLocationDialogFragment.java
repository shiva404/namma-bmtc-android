package com.intuit.in24hr.namma_bmtc;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.intuit.in24hr.namma_bmtc.types.Location;

/**
 *
 */
public class ShareLocationDialogFragment extends DialogFragment {

    public static interface ShareLocationDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }


    ShareLocationDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the StartPayperiodDialogListener
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try{
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (ShareLocationDialogListener) activity;
        }catch(ClassCastException e){
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement StartPayperiodDialogListener");
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Location location = (Location) getArguments().getSerializable("location");

        final LocationUpdateResultReceiver resultReceiver = getArguments().getParcelable("receiver");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.location_share, null))
                .setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(ShareLocationDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ShareLocationDialogFragment.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();

    }

}
