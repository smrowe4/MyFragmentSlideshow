package com.example.myfragmentslideshow;



import java.util.List;

import com.example.myfragmentslideshow.SlideshowFragment.SlideshowListener;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

public class SlideshowEditorFragment extends ListFragment {

	// slideshowEditorAdapter to display slideshow in ListView
	   private SlideshowEditorAdapter slideshowEditorAdapter;
	   private SlideshowInfo slideshow; // slideshow data
	   
	   private Button doneButton;
	   private Button addPictureButton;
	   private Button addMusicButton;
	   private Button playButton;
	   
	// Container Activity must implement this interface
	    public interface SlideshowEditorListener {
	        public void removeEditorFragment();
	    }
	    private SlideshowEditorListener slideshowEditorListener;
	    
	    @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        
	        // This makes sure that the container activity has implemented
	        // the callback interface. If not, it throws an exception
	        try {
	            slideshowEditorListener = (SlideshowEditorListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString()
	                    + " must implement SlideshowListener");
	        }
	    }
	    
	   @Override
	   public View onCreateView(LayoutInflater inflater,
	      ViewGroup container, Bundle savedInstanceState) {
	      
	      View rootView = inflater.inflate(R.layout.slideshow_editor, container, false);
	      
	      Button doneButton = (Button) rootView.findViewById(R.id.doneButton);
	      doneButton.setOnClickListener(doneButtonListener);

	      Button addPictureButton =
	         (Button) rootView.findViewById(R.id.addPictureButton);
	      addPictureButton.setOnClickListener(addPictureButtonListener);

	      Button addMusicButton = (Button) rootView.findViewById(R.id.addMusicButton);
	      addMusicButton.setOnClickListener(addMusicButtonListener);

	      Button playButton = (Button) rootView.findViewById(R.id.playButton);
	      playButton.setOnClickListener(playButtonListener);
	      
	    //Inflate the layout for this fragment
	      return rootView;
	   }
	   
	// called when the parent Activity is created
    @Override
    public void onActivityCreated(Bundle savedInstanceStateBundle) 
    {
      super.onActivityCreated(savedInstanceStateBundle);
      String name = getArguments().getString("name");
      slideshow = Slideshow.getSlideshowInfo(name);

      // get ListView and set its adapter for displaying list of images
      slideshowEditorAdapter = 
         new SlideshowEditorAdapter(getActivity(), slideshow.getImageList());
      //getListView().setAdapter(slideshowEditorAdapter);
      setListAdapter(slideshowEditorAdapter);

      //slideshowListView = getListView(); // get the built-in ListView
      // create and set the ListView's adapter
      //slideshowAdapter = new SlideshowAdapter(getActivity(), slideshowList);
      //setListAdapter(slideshowAdapter);
    } // end method onActivityCreated
    
 // called when an Activity launched from this Activity returns
	   @Override
	public
	   final void onActivityResult(int requestCode, int resultCode, 
	      Intent data)
	   {
		   if (resultCode == -1) // if there was no error
		      {
		         Uri selectedUri = data.getData();

		         // if the Activity returns an image
		         if (requestCode == PICTURE_ID )
		         {
		            // add new image path to the slideshow
		            slideshow.addImage(selectedUri.toString());
		            Log.e("SlideShow","Picture_ID");
		            // refresh the ListView 
		            slideshowEditorAdapter.notifyDataSetChanged();
		         } // end if
		         else if (requestCode == MUSIC_ID) // Activity returns music
		            slideshow.setMusicPath(selectedUri.toString());
		      } // end if
	   } // end method onActivityResult
    
 // set IDs for each type of media result
    private static final int PICTURE_ID = 1;
    private static final int MUSIC_ID = 2;
    
 // called when the user touches the "Done" Button
    private OnClickListener doneButtonListener = new OnClickListener()
    {
       // return to the previous Activity
       @Override
       public void onClick(View v)
       {
          slideshowEditorListener.removeEditorFragment();
       } // end method onClick
    }; // end OnClickListener doneButtonListener

    // called when the user touches the "Add Picture" Button
    private OnClickListener addPictureButtonListener = new OnClickListener()
    {
       // launch image choosing activity
       @Override
       public void onClick(View v)
       {
          Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
          intent.setType("image/*");
          startActivityForResult(Intent.createChooser(intent, 
             getResources().getText(R.string.chooser_image)), PICTURE_ID);
       } // end method onClick
    }; // end OnClickListener addPictureButtonListener

    // called when the user touches the "Add Music" Button
    private OnClickListener addMusicButtonListener = new OnClickListener()
    {
       // launch music choosing activity
       @Override
       public void onClick(View v)
       {
          Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
          intent.setType("audio/*");
          startActivityForResult(Intent.createChooser(intent, 
             getResources().getText(R.string.chooser_music)), MUSIC_ID);
       } // end method onClick
    }; // end OnClickListener addMusicButtonListener

    // called when the user touches the "Play" Button
    private OnClickListener playButtonListener = new OnClickListener()
    {
       // plays the current slideshow
       @Override
       public void onClick(View v)
       {
    	   String name = slideshow.getName();
    	   //Log.e("slideshow", "editor fragment: play button listener");
           // open slideshow editor to edit the content of this slideshow
      		Bundle bundle=new Bundle();
      		bundle.putString("SLIDESHOW_NAME", name);
      		SlideshowPlayFragment newSlideshowPlayFragment = new SlideshowPlayFragment();
      		newSlideshowPlayFragment.setArguments(bundle);
      		FragmentManager fm = getFragmentManager();
      		FragmentTransaction fragmentTransaction = fm.beginTransaction();
      		fragmentTransaction.replace(R.id.slideshow_fragment, newSlideshowPlayFragment);
      		fragmentTransaction.addToBackStack(null);
      		   
      		fragmentTransaction.commit();
       } // end method onClick
    }; // end playButtonListener

    // called when the user touches the "Delete" Button next
    // to an ImageView
    private OnClickListener deleteButtonListener = new OnClickListener()
    {
       // removes the image
       @Override
       public void onClick(View v)
       {
          slideshowEditorAdapter.remove((String) v.getTag());
       } // end method onClick
    }; // end OnClickListener deleteButtonListener

    // Class for implementing the "ViewHolder pattern"
    // for better ListView performance
    private static class ViewHolder
    {
       ImageView slideImageView; // refers to ListView item's ImageView
       Button deleteButton; // refers to ListView item's Button
    } // end class ViewHolder
    
    // ArrayAdapter displaying Slideshow images
    private class SlideshowEditorAdapter extends ArrayAdapter<String>
    {
       private List<String> items; // list of image Uris
       private LayoutInflater inflater;
       
       public SlideshowEditorAdapter(Context context, List<String> items)
       {
          super(context, -1, items); // -1 indicates we're customizing view
          this.items = items;
          inflater = (LayoutInflater) 
             context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       } // end SlideshoweditorAdapter constructor

       @Override
       public View getView(int position, View convertView, ViewGroup parent)
       {
          ViewHolder viewHolder; // holds references to current item's GUI
          
          // if convertView is null, inflate GUI and create ViewHolder;
          // otherwise, get existing ViewHolder
          if (convertView == null)
          {
             convertView = 
                inflater.inflate(R.layout.slideshow_edit_item, null);
             
             // set up ViewHolder for this ListView item
             viewHolder = new ViewHolder();
             viewHolder.slideImageView = (ImageView) 
                convertView.findViewById(R.id.slideshowImageView);
             viewHolder.deleteButton = 
                (Button) convertView.findViewById(R.id.deleteButton);
             convertView.setTag(viewHolder); // store as View's tag
          } // end if
          else // get the ViewHolder from the convertView's tag
             viewHolder = (ViewHolder) convertView.getTag();
          
          // get and display a thumbnail Bitmap image 
          String item = items.get(position); // get current image
          Log.i("slideshow", "position= " + item);
          new LoadThumbnailTask().execute(viewHolder.slideImageView, 
             Uri.parse(item));

          // configure the "Delete" Button
          viewHolder.deleteButton.setTag(item);
          viewHolder.deleteButton.setOnClickListener(deleteButtonListener);

          return convertView;
       } // end method getView
    } // end class SlideshowEditorAdapter

    // task to load thumbnails in a separate thread
    private class LoadThumbnailTask extends AsyncTask<Object,Object,Bitmap>
    {
       ImageView imageView; // displays the thumbnail
       
       // load thumbnail: ImageView, MediaType and Uri as args
       @Override
       protected Bitmap doInBackground(Object... params)
       {
          imageView = (ImageView) params[0];
          Uri ui = (Uri) params[1];
          String mystr = ui.getLastPathSegment();
          if (imageView == null) {
         	 Log.i("slideshow", "imageview is null");
          }
          Log.i("slideshow", "doInBackground uri= " + params[1].toString());
          Log.i("slideshow", "getThumb: " + ui.getAuthority());
          
          Log.i("slideshow", "split: " + mystr.split(":")[1]);
          Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_camera);
 
          return Slideshow.getThumbnail((Uri) params[1], 
             getActivity().getContentResolver(), new BitmapFactory.Options());
       } // end method doInBackground

       // set thumbnail on ListView
       @Override
       protected void onPostExecute(Bitmap result)
       {
    	   Log.e("SlideShow","onPostExecute");
          super.onPostExecute(result);
          imageView.setImageBitmap(result);
       } // end method onPostExecute  
    } // end class LoadThumbnailTask
     
}
