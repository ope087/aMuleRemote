/*
 * Copyright (c) 2015. Gianluca Vegetti
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;

import com.iukonline.amule.android.amuleremote.R;

public class TooltipDialogFragment extends AlertDialogFragment {
    
    public final static String BUNDLE_TOOLTIP_ID = "tooltip_id";
    public final static String BUNDLE_IS_CHECKED = "is_checked";

    protected long mTooltipId;
    protected CheckBox mCheckBox;
    
    public TooltipDialogFragment() {}

    public static TooltipDialogFragment newInstance(long tooltipId, int title, int msg) {

        TooltipDialogFragment fragment = new TooltipDialogFragment();
        fragment.setAlertDialogFragmentArguments(title, msg, false);
        Bundle args = fragment.getArguments();
        args.putLong(BUNDLE_TOOLTIP_ID, tooltipId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(BUNDLE_TOOLTIP_ID, mTooltipId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mTooltipId = savedInstanceState.getInt(BUNDLE_TOOLTIP_ID);
        } else {
            Bundle args = getArguments();
            if (args != null) mTooltipId = getArguments().getLong(BUNDLE_TOOLTIP_ID);
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /*
        if (savedInstanceState != null) {
            mTooltipId = savedInstanceState.getInt(BUNDLE_TOOLTIP_ID);
        }
        */

        View checkBoxView = View.inflate(getActivity(), R.layout.dialog_tooltip, null);
        mCheckBox = (CheckBox) checkBoxView.findViewById(R.id.dialog_tooltip_checkbox);
        
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);
        builder.setView(checkBoxView);
        
        builder.setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Bundle b = new Bundle();
                                b.putBoolean(BUNDLE_IS_CHECKED, mCheckBox.isChecked());
                                b.putLong(BUNDLE_TOOLTIP_ID, mTooltipId);
                                sendEventToListener(ALERTDIALOG_EVENT_OK, b);
                            }
                        }
                    );
     
        return builder.create();
    }
    
}
