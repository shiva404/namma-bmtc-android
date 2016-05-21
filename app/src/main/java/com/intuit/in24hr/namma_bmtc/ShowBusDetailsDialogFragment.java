package com.intuit.in24hr.namma_bmtc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import junit.framework.Test;

import org.w3c.dom.Text;

/**
 * Created by sn1 on 5/18/16.
 */
public class ShowBusDetailsDialogFragment extends DialogFragment {

    public ShowBusDetailsDialogFragment(){
    }

    private static String busDetails;
    private static String routeNo;
    private static String from;
    private static String to;

    public static ShowBusDetailsDialogFragment newInstance(String routeNo, String from, String to, String details){
        ShowBusDetailsDialogFragment frag = new ShowBusDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString("routeNo", routeNo);
        ShowBusDetailsDialogFragment.routeNo = routeNo;
        args.putString("from", from);
        ShowBusDetailsDialogFragment.from = from;
        args.putString("to", to);
        ShowBusDetailsDialogFragment.to = to;
        args.putString("details", details);
        ShowBusDetailsDialogFragment.busDetails = details;
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View inflate = inflater.inflate(R.layout.bus_details, null);
        TextView busRouteNo = (TextView) inflate.findViewById(R.id.route_no);
        TextView fromTextView = (TextView) inflate.findViewById(R.id.route_from);
        TextView toTextView = (TextView) inflate.findViewById(R.id.route_to);
        TextView detailsTextView = (TextView) inflate.findViewById(R.id.route_details);

        busRouteNo.setText(routeNo);
        fromTextView.setText(from);
        toTextView.setText(to);
        detailsTextView.setText(busDetails);

        alertDialogBuilder.setView(inflate);
        alertDialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return alertDialogBuilder.create();
    }
}
