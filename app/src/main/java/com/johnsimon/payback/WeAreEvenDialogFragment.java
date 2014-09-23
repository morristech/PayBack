package com.johnsimon.payback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class WeAreEvenDialogFragment extends DialogFragment {

	@Override
	public void onStart() {
		super.onStart();

		if (getDialog() == null) {
			return;
		}
		//Fade in, out to the right
		getDialog().getWindow().setWindowAnimations(R.style.we_are_even_anim);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.we_are_even_dialog, null);

		RobotoMediumTextView evenDialogTitle = (RobotoMediumTextView) rootView.findViewById(R.id.even_dialog_title);
		Animation textAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_from_bottom);

		evenDialogTitle.setAnimation(textAnim);

		builder.setView(rootView);

		final AlertDialog alertDialog = builder.create();

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				alertDialog.cancel();
			}
		}, 1000);


		return alertDialog;
	}

}