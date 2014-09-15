package com.example.myfragmentslideshow;


import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class SlideshowPlayFragment extends Fragment {

	private static final String TAG = "SLIDESHOW"; // error logging tag
	   
	   // constants for saving slideshow state when config changes
	   private static final String MEDIA_TIME = "MEDIA_TIME";
	   private static final String IMAGE_INDEX = "IMAGE_INDEX";
	   private static final String SLIDESHOW_NAME = "SLIDESHOW_NAME";
	   
	   private static final int DURATION = 5000; // 5 seconds per slide
	   private ImageView imageView; // displays the current image
	   private String slideshowName; // name of current slideshow
	   private SlideshowInfo slideshow; // slideshow being played
	   private BitmapFactory.Options options; // options for loading images
	   private Handler handler; // used to update the slideshow
	   private int nextItemIndex; // index of the next image to display
	   private int mediaTime; // time in ms from which media should play 
	   private MediaPlayer mediaPlayer; // plays the background music, if any
	// Container Activity must implement this interface
    public interface SlideshowPlayListener {
        public void removePlayFragment();
    }
    
    private SlideshowPlayListener slideshowPlayListener;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            slideshowPlayListener = (SlideshowPlayListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SlideshowPlayListener");
        }
    }
    
   @Override
   public View onCreateView(LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
     
      View rootView = inflater.inflate(R.layout.slideshow_player, container, false);
      
      imageView = (ImageView) rootView.findViewById(R.id.imageView);
  
      int w = container.getLayoutParams().width;
      int h = container.getLayoutParams().height;
      android.view.ViewGroup.LayoutParams lp = container.getLayoutParams();
      lp.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
      lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
    		  
      Log.e("slideshow", "Width is: " + Integer.toString(w) + " Height is: " + Integer.toString(h));
      container.setLayoutParams(lp);
      
      
      
    //Inflate the layout for this fragment
      return rootView;
   }
   
// called when the parent Activity is created
@Override
public void onActivityCreated(Bundle savedInstanceStateBundle) 
{
  super.onActivityCreated(savedInstanceStateBundle);
  
  //if (savedInstanceStateBundle == null) // Activity starting
 // {
     // get slideshow name from Intent's extras
     //slideshowName = getIntent().getStringExtra(Slideshow.NAME_EXTRA);
     String slideshowName = getArguments().getString("SLIDESHOW_NAME");
     Log.e("slideshow", slideshowName.toString() + " PlayFragment");
     mediaTime = 0; // position in media clip
     nextItemIndex = 0; // start from first image
  //} // end if
  /*else // Activity resuming
  {
     // get the play position that was saved when config changed
     mediaTime = savedInstanceStateBundle.getInt(MEDIA_TIME); 
     
     // get index of image that was displayed when config changed 
     nextItemIndex = savedInstanceStateBundle.getInt(IMAGE_INDEX);     
     
     // get name of slideshow that was playing when config changed
     slideshowName = savedInstanceStateBundle.getString(SLIDESHOW_NAME);
  } // end else       
  */
  // get SlideshowInfo for slideshow to play
  slideshow = Slideshow.getSlideshowInfo(slideshowName);  
if (slideshow == null)
{
	Log.e("slideshow","getslideshowInfo returns null");
}
  // configure BitmapFactory.Options for loading images
  options = new BitmapFactory.Options(); 
  options.inSampleSize = 4; // sample at 1/4 original width/height 

  // if there is music to play
  if (slideshow.getMusicPath() != null)
  {
     // try to create a MediaPlayer to play the music
     try
     {
        mediaPlayer = new MediaPlayer(); 
        mediaPlayer.setDataSource(
           getActivity(), Uri.parse(slideshow.getMusicPath()));
        mediaPlayer.prepare(); // prepare the MediaPlayer to play
        mediaPlayer.setLooping(true); // loop the music
        mediaPlayer.seekTo(mediaTime); // seek to mediaTime
     } // end try
     catch (Exception e)
     {
        Log.v(TAG, e.toString());
     } // end catch
  } // end if
  Log.e("slideshow", "player before handler is called");
  handler = new Handler(); // create handler to control slideshow
} // end method onActivityCreated

//called after onCreate and sometimes onStop
@Override
public void onStart()
{
   super.onStart();
   handler.post(updateSlideshow); // post updateSlideshow to execute
} // end method onStart

//called when the Activity is paused
@Override
public void onPause()
{
   super.onPause();
   
   if (mediaPlayer != null) 
      mediaPlayer.pause(); // pause playback
} // end method onPause

// called after onStart or onPause
@Override
public void onResume()
{
   super.onResume();

   if (mediaPlayer != null) 
      mediaPlayer.start(); // resume playback
} // end method onResume

// called when the Activity stops
@Override
public void onStop()
{
   super.onStop();
   
   // prevent slideshow from operating when in background
   handler.removeCallbacks(updateSlideshow); 
} // end method onStop

// called when the Activity is destroyed
@Override
public void onDestroy()
{
   super.onDestroy();
   
   if (mediaPlayer != null) 
      mediaPlayer.release(); // release MediaPlayer resources
} // end method onDestroy

// save slideshow state so it can be restored in onCreate
@Override
public void onSaveInstanceState(Bundle outState)
{
   super.onSaveInstanceState(outState);
   
   // if there is a mediaPlayer, store media's current position 
   if (mediaPlayer != null)
      outState.putInt(MEDIA_TIME, mediaPlayer.getCurrentPosition());
      
   // save nextItemIndex and slideshowName
   outState.putInt(IMAGE_INDEX, nextItemIndex - 1); 
   outState.putString(SLIDESHOW_NAME, slideshowName); 
} // end method onSaveInstanceState

// anonymous inner class that implements Runnable to control slideshow
private Runnable updateSlideshow = new Runnable()
{
   @Override
   public void run()
   {
      if (nextItemIndex >= slideshow.size())
      {
         // if there is music playing
         if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.reset(); // slideshow done, reset mediaPlayer 
         Log.e("slideshow","runnable quit");
         slideshowPlayListener.removePlayFragment(); // return to launching Activity
      } // end if
      else
      {     
         String item = slideshow.getImageAt(nextItemIndex);
         new LoadImageTask().execute(Uri.parse(item));
         Log.e("slideshow","runnable display");
         ++nextItemIndex; 
      } // end else
   } // end method run
   
   // task to load thumbnails in a separate thread
   class LoadImageTask extends AsyncTask<Uri, Object, Bitmap>
   {
      // load iamges
      @Override
      protected Bitmap doInBackground(Uri... params)
      {
         return getBitmap(params[0], getActivity().getContentResolver(), options);
      } // end method doInBackground

      // set thumbnail on ListView
      @Override
      protected void onPostExecute(Bitmap result)
      {
         super.onPostExecute(result);
         BitmapDrawable next = new BitmapDrawable(result);
         next.setGravity(android.view.Gravity.CENTER);
         Drawable previous = imageView.getDrawable();
         
         // if previous is a TransitionDrawable, 
         // get its second Drawable item
         if (previous instanceof TransitionDrawable)
            previous = ((TransitionDrawable) previous).getDrawable(1);
         
         if (previous == null)
            imageView.setImageDrawable(next);
         else
         {
            Drawable[] drawables = { previous, next };
            TransitionDrawable transition =
               new TransitionDrawable(drawables);
            imageView.setImageDrawable(transition);
            transition.startTransition(1000);
         } // end else

         handler.postDelayed(updateSlideshow, DURATION);
      } // end method onPostExecute  
   } // end class LoadImageTask 

   // utility method to get a Bitmap from a Uri
   public Bitmap getBitmap(Uri uri, ContentResolver cr, 
      BitmapFactory.Options options)
   {
      Bitmap bitmap = null;
      
      // get the image
      try
      {
         InputStream input = cr.openInputStream(uri);
         bitmap = BitmapFactory.decodeStream(input, null, options);            
      } // end try
      catch (FileNotFoundException e) 
      {
         Log.v(TAG, e.toString());
      } // end catch
      
      return bitmap;
   } // end method getBitmap
}; // end Runnable updateSlideshow
} // end slideshowPlayFragment
