package com.example.myfragmentslideshow;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myfragmentslideshow.AddSlideshowDialogFragment;
import com.example.myfragmentslideshow.AddSlideshowDialogFragment.DialogFinishedListener;
import com.example.myfragmentslideshow.SlideshowEditorFragment.SlideshowEditorListener;
import com.example.myfragmentslideshow.SlideshowPlayFragment.SlideshowPlayListener;

public class Slideshow extends Activity 
			implements SlideshowFragment.SlideshowListener, 
			DialogFinishedListener, SlideshowEditorListener,
			SlideshowPlayListener {

	static List<SlideshowInfo> slideshowList; // List of slideshows
	SlideshowFragment ssFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		slideshowList = new ArrayList<SlideshowInfo>();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	      inflater.inflate(R.menu.slideshow_menu, menu);
		return true;
	}

	 // handle choice from options menu
	   @Override
	   public boolean onOptionsItemSelected(MenuItem item) 
	   {
	      // get a reference to the LayoutInflater service
	      LayoutInflater inflater = (LayoutInflater) getSystemService(
	         Context.LAYOUT_INFLATER_SERVICE);

	      // inflate slideshow_name_edittext.xml to create an EditText
	      View view = inflater.inflate(R.layout.slideshow_name_edittext, null);
	      final EditText nameEditText = 
	         (EditText) view.findViewById(R.id.nameEditText);
	         
	      // create an input dialog to get slideshow name from user
	   // create a new AddCityDialogFragment 
	      AddSlideshowDialogFragment newAddSlideshowDialogFragment = 
	         new AddSlideshowDialogFragment();
	      
	      // get instance of the FragmentManager
	      FragmentManager thisFragmentManager = getFragmentManager();
	      
	      // begin a FragmentTransaction
	      FragmentTransaction addSlideshowFragmentTransition = 
	         thisFragmentManager.beginTransaction();
	      
	      // show the DialogFragment
	      newAddSlideshowDialogFragment.show(addSlideshowFragmentTransition, "");
	      
	      return super.onOptionsItemSelected(item); // call super's method
	   } // end method onOptionsItemSelected
	
	// called when an Activity launched from this Activity returns
	   @Override
	   protected final void onActivityResult(int requestCode, int resultCode, 
	      Intent data)
	   {
		   Log.e("SlideShow","RESULT_OK");
	      if (resultCode == RESULT_OK) // if there was no error
	      {
	      //   Uri selectedUri = data.getData(); 
	      } // end if
	   } // end method onActivityResult
	   
	// utility method to get a thumbnail image Bitmap
	   public static Bitmap getThumbnail(Uri uri, ContentResolver cr, 
	      BitmapFactory.Options options)
	   {
		   //int id = Integer.parseInt(uri.getLastPathSegment());
	      String myStr = uri.getLastPathSegment();
	      long id = Long.parseLong(myStr.split(":")[1]);
		  // int id = 1117;

	      Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, id, 
	         MediaStore.Images.Thumbnails.MICRO_KIND, options);         
	      
	      return bitmap;
	   } // end method getThumbnail
	   
	// utility method to locate SlideshowInfo object by slideshow name
	   public static SlideshowInfo getSlideshowInfo(String name)
	   {
	      // locate and return slideshow with specified name
	      for (SlideshowInfo slideshowInfo : slideshowList)
	         if (slideshowInfo.getName().equals(name))
	            return slideshowInfo;
	      
	      return null; // no matching object
	   } // end method getSlideshowInfo
	   
	// listens for changes to the CitiesFragment
	     // called to get a copy of the list
	public List<SlideshowInfo> getSlideshowList()
	{
		return slideshowList;
	}
	
	// called when the FragmentDialog is dismissed 
	   public void onDialogFinished(String slideshowString) 
	   {
		   slideshowList.add(new SlideshowInfo(slideshowString));
		   
		   // open slideshow editor to add content to this slideshow
		   Bundle bundle=new Bundle();
		   bundle.putString("name", slideshowString.toString());
		   SlideshowEditorFragment newSlideshowEditorFragment = new SlideshowEditorFragment();
		   newSlideshowEditorFragment.setArguments(bundle);
		   FragmentManager fm = getFragmentManager();
		   FragmentTransaction fragmentTransaction = fm.beginTransaction();
		   //Log.e("SlideShow", slideshowString.toString());
		   fragmentTransaction.replace(R.id.slideshow_fragment, newSlideshowEditorFragment);
		   fragmentTransaction.addToBackStack(null);
		   
		   fragmentTransaction.commit();
	   } // end method onDialogFinished

	@Override
	public void removeEditorFragment() {
		Log.e("slideshow","removeEditorFragment");
		FragmentManager fm = getFragmentManager();
		fm.popBackStack();
	}

	@Override
	public void removePlayFragment() {
		Log.e("slideshow", "removePlayFragment");
		FragmentManager fm = getFragmentManager();
		fm.popBackStack();
	}
}
