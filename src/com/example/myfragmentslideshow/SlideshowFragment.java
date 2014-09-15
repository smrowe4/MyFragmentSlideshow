package com.example.myfragmentslideshow;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.TextView;

public class SlideshowFragment extends ListFragment {

	//static Slideshow slideAct;
    public List<SlideshowInfo> slideshowList; // gets a copy of slideshowList from Activity
    private ListView slideshowListView; // this ListActivity's ListView
    private SlideshowAdapter slideshowAdapter; // adapter for the ListView

 // Container Activity must implement this interface
    public interface SlideshowListener {
        public List<SlideshowInfo> getSlideshowList();
    }
    private SlideshowListener slideshowListener;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            slideshowListener = (SlideshowListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SlideshowListener");
        }
    }
    
 // called when the parent Activity is created
    @Override
    public void onActivityCreated(Bundle savedInstanceStateBundle) 
    {
      super.onActivityCreated(savedInstanceStateBundle);
       //////////////////////////////////////////////
      //slideshowList = new ArrayList<SlideshowInfo>();

      slideshowListView = getListView(); // get the built-in ListView
      //slideAct = (Slideshow) getActivity();
      slideshowList = slideshowListener.getSlideshowList();
      // create and set the ListView's adapter
      slideshowAdapter = new SlideshowAdapter(getActivity(), slideshowList);
      setListAdapter(slideshowAdapter);
    } // end method onActivityCreated
    
    private static class ViewHolder
    {
       TextView nameTextView; // refers to ListView item's TextView
       ImageView imageView; // refers to ListView item's ImageView
       Button playButton; // refers to ListView item's Play Button
       Button editButton; // refers to ListView item's Edit Button
       Button deleteButton; // refers to ListView item's Delete Button
    } // end class ViewHolder
    
 // ArrayAdapter subclass that displays a slideshow's name, first image
    // and "Play", "Edit" and "Delete" Buttons
    private class SlideshowAdapter extends ArrayAdapter<SlideshowInfo>
    {
       private List<SlideshowInfo> items;
       private LayoutInflater inflater;
       private Context context; // this Fragment's Activity's Context

       // public constructor for SlideshowAdapter
       public SlideshowAdapter(Context context, List<SlideshowInfo> items)
       {
          // call super constructor
          super(context, -1, items);
          this.items = items;
          this.context = context;
          inflater = (LayoutInflater) 
             context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       } // end SlideshowAdapter constructor

       // returns the View to display at the given position
       @Override
       public View getView(int position, View convertView, 
          ViewGroup parent)
       {
          ViewHolder viewHolder; // holds references to current item's GUI

          // if convertView is null, inflate GUI and create ViewHolder;
          // otherwise, get existing ViewHolder
          if (convertView == null) 
          {
             convertView = 
                inflater.inflate(R.layout.slideshow_list_item, null);

             // set up ViewHolder for this ListView item
             viewHolder = new ViewHolder();
             viewHolder.nameTextView = (TextView) 
                convertView.findViewById(R.id.nameTextView);
             viewHolder.imageView = (ImageView) 
                convertView.findViewById(R.id.slideshowImageView);
             viewHolder.playButton = 
                (Button) convertView.findViewById(R.id.playButton);
             viewHolder.editButton = 
                (Button) convertView.findViewById(R.id.editButton);
             viewHolder.deleteButton = 
                (Button) convertView.findViewById(R.id.deleteButton);
             convertView.setTag(viewHolder); // store as View's tag
          } // end if
          else // get the ViewHolder from the convertView's tag
             viewHolder = (ViewHolder) convertView.getTag();

          // get the slideshow the display its name in nameTextView
          SlideshowInfo slideshowInfo = items.get(position);
          viewHolder.nameTextView.setText(slideshowInfo.getName());

          // if there is at least one image in this slideshow
          if (slideshowInfo.size() > 0)
          {
             // create a bitmap using the slideshow's first image or video
             String firstItem = slideshowInfo.getImageAt(0);
             new LoadThumbnailTask().execute(viewHolder.imageView, 
                Uri.parse(firstItem));
          } // end if

          // set tag and OnClickListener for the "Play" Button
          viewHolder.playButton.setTag(slideshowInfo);
          viewHolder.playButton.setOnClickListener(playButtonListener);

          // set tag and OnClickListener for the "Edit" Button
          viewHolder.editButton.setTag(slideshowInfo);
          viewHolder.editButton.setOnClickListener(editButtonListener);

          // set and tag OnClickListener for the "Delete" Button
          viewHolder.deleteButton.setTag(slideshowInfo);
          viewHolder.deleteButton.setOnClickListener(deleteButtonListener);
          
          return convertView; // return the View for this position
       } // end getView
    } // end class SlideshowAdapter 
    
 // task to load thumbnails in a separate thread
    private class LoadThumbnailTask extends AsyncTask<Object,Object,Bitmap>
    {
       ImageView imageView; // displays the thumbnail
       
       // load thumbnail: ImageView and Uri as args
       @Override
       protected Bitmap doInBackground(Object... params)
       {
          imageView = (ImageView) params[0];
          Uri ui = (Uri) params[1];
          String mystr = ui.getLastPathSegment();
          
          Log.i("slideshow", "doInBackground uri= " + params[1].toString());
          Log.i("slideshow", "getThumb: " + ui.getAuthority());
          
          Log.i("slideshow", "split: " + mystr.split(":")[1]);
          
          return Slideshow.getThumbnail((Uri) params[1], 
             getActivity().getContentResolver(), new BitmapFactory.Options());
       } // end method doInBackground

       // set thumbnail on ListView
       @Override
       protected void onPostExecute(Bitmap result)
       {
          super.onPostExecute(result);
          imageView.setImageBitmap(result);
       } // end method onPostExecute  
    } // end class LoadThumbnailTask 
    
    // respond to events generated by the "Play" Button
    OnClickListener playButtonListener = new OnClickListener()
    {
       @Override
       public void onClick(View v)
       {
    	   String name = ((SlideshowInfo) v.getTag()).getName();
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

    // respond to events generated by the "Edit" Button
    private OnClickListener editButtonListener = new OnClickListener()
    {
       @Override
       public void onClick(View v)
       {
          String name = ((SlideshowInfo) v.getTag()).getName();
    	// open slideshow editor to edit the content of this slideshow
		   Bundle bundle=new Bundle();
		   bundle.putString("name", name);
		   SlideshowEditorFragment newSlideshowEditorFragment = new SlideshowEditorFragment();
		   newSlideshowEditorFragment.setArguments(bundle);
		   FragmentManager fm = getFragmentManager();
		   FragmentTransaction fragmentTransaction = fm.beginTransaction();
		   fragmentTransaction.replace(R.id.slideshow_fragment, newSlideshowEditorFragment);
		   fragmentTransaction.addToBackStack(null);
		   
		   fragmentTransaction.commit();
       } // end method onClick
    }; // end editButtonListener

    // respond to events generated by the "Delete" Button
    private OnClickListener deleteButtonListener = new OnClickListener()
    {
       @Override
       public void onClick(final View v)
       {
          // create a new AlertDialog Builder
          AlertDialog.Builder builder = 
             new AlertDialog.Builder(getActivity());
          builder.setTitle(R.string.dialog_confirm_delete); 
          builder.setMessage(R.string.dialog_confirm_delete_message);
          builder.setPositiveButton(R.string.button_ok, 
             new DialogInterface.OnClickListener()
             {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                   slideshowList.remove(
                      (SlideshowInfo) v.getTag());
                   slideshowAdapter.notifyDataSetChanged(); // refresh 
                } // end method onClick
             } // end anonymous inner class
          ); // end call to setPositiveButton 
          builder.setNegativeButton(R.string.button_cancel, null);
          builder.show();
       } // end method onClick
    }; // end deleteButtonListener
}
