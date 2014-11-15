package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.johnsimon.payback.R;
import com.johnsimon.payback.util.FontCache;
import com.johnsimon.payback.util.Resource;

import java.util.ArrayList;

public class FromWhoDialogFragment extends DialogFragment {

	public FromWhoSelected completeCallback = null;
	private AlertDialog alertDialog;

	public final static String KEY_NAME = "FROM_WHO_LEY_NAME";

	private AutoCompleteTextView actv;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.from_who_dialog, null);

		final Button confirmButton = (Button) rootView.findViewById(R.id.from_who_dialog_confirm);
		confirmButton.setTypeface(FontCache.get(getActivity(), FontCache.RobotoMedium));
		Button cancelButton = (Button) rootView.findViewById(R.id.from_who_dialog_cancel);
		cancelButton.setTypeface(FontCache.get(getActivity(), FontCache.RobotoMedium));

		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});

		actv = (AutoCompleteTextView) rootView.findViewById(R.id.from_who_actv);
		actv.setTextColor(getResources().getColor(R.color.gray_text_dark));

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getActivity(),
				R.layout.autocomplete_list_item,
				R.id.autocomplete_list_item_title,
				Resource.getAllNames());

		actv.setAdapter(adapter);

		Bundle args = getArguments();
		if (args != null) {
			String sentName = args.getString(KEY_NAME, "");
			if (!TextUtils.isEmpty(sentName)) {

				actv.setText(sentName);

				actv.setSelection(0, actv.getText().length());
				actv.setAdapter(null);
				actv.requestFocus();

				Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						((AutoCompleteTextView) msg.obj).setAdapter(adapter);
					}
				};

				Message msg = handler.obtainMessage();
				handler.sendMessageDelayed(msg, 200);

				actv.setSelection(actv.getText().length());
				enableButton(confirmButton);
			}
		}

		if (actv.getText().toString().equals("")) {
			disableButton(confirmButton);
		}

			actv.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {

				String name = s.toString();

				if (TextUtils.isEmpty(name)) {
					disableButton(confirmButton);
				} else {
					enableButton(confirmButton);
				}
			}
		});

		builder.setView(rootView);

		alertDialog = builder.create();
		return alertDialog;
	}

	private void disableButton(Button btn) {
		btn.setTextColor(getResources().getColor(R.color.green_disabled));
		btn.setOnClickListener(null);
		btn.setClickable(false);
		btn.setEnabled(false);
	}

	private void enableButton(Button btn) {
		btn.setTextColor(getResources().getColor(R.color.green_strong));
		btn.setOnClickListener(clickListener);
		btn.setClickable(true);
		btn.setEnabled(true);
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			completeCallback.onSelected(actv.getText().toString());
			alertDialog.cancel();
		}
	};

	public interface FromWhoSelected {
		public void onSelected(String name);
	}
}