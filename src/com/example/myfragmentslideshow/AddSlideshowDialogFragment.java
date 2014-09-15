package com.example.myfragmentslideshow;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddSlideshowDialogFragment extends DialogFragment 
			implements OnClickListener 
{
	 // listens for results from the AddCityDialog
	   public interface DialogFinishedListener 
	   {
	      // called when the AddCityDialog is dismissed
	      void onDialogFinished(String slideshowString);
	   } // end interface DialogFinishedListener  
	   
	EditText addSlideshowEditText; // the DialogFragment's EditText
	
	// initializes a new DialogFragment
	   @Override
	   public void onCreate(Bundle bundle)
	   {
	      super.onCreate(bundle);
	      
	      // allow the user to exit using the back key
	      this.setCancelable(true); 
	   } // end method onCreate
	   
	   // inflates the DialogFragment's layout
	   @Override
	   public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	      Bundle argumentsBundle)
	   {
	      // inflate the layout defined in add_city_dialog.xml 
	      View rootView = inflater.inflate(R.layout.add_slideshow, container, 
	         false);
	      
	      // get the EditText
	      addSlideshowEditText = (EditText) rootView.findViewById(
	         R.id.add_slideshow_edit_text);
	   
	      
	      if (argumentsBundle != null) // if the arguments Bundle isn't empty
	      {
	         addSlideshowEditText.setText(argumentsBundle.getString(
	            getResources().getString(
	               R.string.add_slideshow_dialog_bundle_key)));
	      } // end if
	      
	      // set the DialogFragment's title
	      getDialog().setTitle(R.string.add_slideshow_dialog_title); 
	      
	      // initialize the positive Button
	      Button okButton = (Button) rootView.findViewById(
	         R.id.add_slideshow_button);
	      okButton.setOnClickListener(this);
	      return rootView; // return the Fragment's root View
	   } // end method onCreateView
	   
	// called when the Add City Button is clicked
	   @Override
	   public void onClick(View clickedView) 
	   {
	      if (clickedView.getId() == R.id.add_slideshow_button)
	      {
	         DialogFinishedListener listener = 
	            (DialogFinishedListener) getActivity();
	         listener.onDialogFinished(addSlideshowEditText.getText().toString());
	         dismiss(); // dismiss the DialogFragment
	      } // end if
	   } // end method onClick
}
