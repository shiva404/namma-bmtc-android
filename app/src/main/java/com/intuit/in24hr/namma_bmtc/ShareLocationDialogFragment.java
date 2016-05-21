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
import android.widget.EditText;
import android.widget.RadioGroup;

import com.intuit.in24hr.namma_bmtc.types.Location;

/**
 *
 */
public class ShareLocationDialogFragment extends DialogFragment {

    public static interface ShareLocationDialogListener {
        public void onDialogPositiveClick(ShareLocationDialogFragment dialog);
        public void onDialogNegativeClick(ShareLocationDialogFragment dialog);
    }

    ShareLocationDialogListener listener;
    EditText routeNumberValue;
    RadioGroup busCrowdedButtonGroup;
    Location location;

    public ShareLocationDialogFragment(){
    }

    public static ShareLocationDialogFragment newInstance() {
        ShareLocationDialogFragment frag = new ShareLocationDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    // Override the Fragment.onAttach() method to instantiate the StartPayperiodDialogListener
    @Override
    public void onAttach(Activity activity){
        System.out.println("On create view attach ############");
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
        System.out.println("On onCreateDialog ############");
        final Location location = (Location) getArguments().getSerializable("location");

        this.location = location;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View inflate = inflater.inflate(R.layout.location_share, null);
        routeNumberValue = (EditText) inflate.findViewById(R.id.routeNumberText);
        busCrowdedButtonGroup = (RadioGroup) inflate.findViewById(R.id.radioBusCrowd);
        builder.setView(inflate)
                .setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(ShareLocationDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

}
