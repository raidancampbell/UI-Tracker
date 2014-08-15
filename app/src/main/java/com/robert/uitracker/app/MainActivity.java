package com.robert.uitracker.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends ActionBarActivity {

  private boolean bottomImageIsShown = true;
  private boolean topImageIsShown = true;
  private Menu myMenu;
  Drawable blank;//solid gray. Not white, so that size of image is shown
  Drawable nothing;//clear.  Used as an overlay over the bottomImage
  Drawable bottomImage;//bottom layer
  Drawable topImage;//top layer
  Drawable[] bottomTop = new Drawable[2];
  LayerDrawable sum;
  int x_mm = 400;//arbitrarily set to be a decently visible size
  int xpix = 400;
  int y_mm = 400;
  int ypix = 400;
  String x_or_y = "X";
  boolean usePixel =false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    nothing = new ColorDrawable(Color.LTGRAY);
    blank = new ColorDrawable(Color.LTGRAY);
    blank.setAlpha(0);
    nothing.setAlpha(20);
    bottomImage = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(),R.drawable.sample_ui));
    topImage = blank;
    bottomTop[0] = bottomImage;
    bottomTop[1] = topImage;
    sum = new LayerDrawable(bottomTop);
    recalcCompletedImage();
  }


  public void recalcCompletedImage(){
    if(!bottomImageIsShown) bottomTop[0] = nothing;
    else bottomTop[0] = bottomImage;
    if(!topImageIsShown)bottomTop[1] = blank;
    else bottomTop[1] = topImage;
    ((ImageView) findViewById(R.id.image_here)).setBackground(new LayerDrawable(bottomTop));
    Log.d("Err","background just set");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    myMenu = menu;
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if(id == R.id.remove_bottom_image_settings) {//whitewash the current image
      if(bottomImageIsShown) {
        (myMenu.findItem(R.id.remove_bottom_image_settings)).setTitle("Show Bottom Image");//change text
        bottomImageIsShown = false;
        recalcCompletedImage();
      } else {
        (myMenu.findItem(R.id.remove_bottom_image_settings)).setTitle("Hide Bottom Image");//change text
        bottomImageIsShown = true;
        recalcCompletedImage();
      }
      return true;
    }//end of hide/show bottom image
    if(id == R.id.remove_top_image_settings) {//clear the current image
      if(topImageIsShown) {
        (myMenu.findItem(R.id.remove_top_image_settings)).setTitle("Show Top Image");//change text
        topImageIsShown = false;
        recalcCompletedImage();
      }else{
        (myMenu.findItem(R.id.remove_top_image_settings)).setTitle("Hide Top Image");//change text
        topImageIsShown = true;
        recalcCompletedImage();
      }
      return true;
    }//end of hide/show top image
    if(id == R.id.clear_touches_settings) {
      ((MySurfaceView) findViewById(R.id.image_here)).clearTouches();
    }
    if(id == R.id.load_bottom_image_settings) {//load a bottom image
      Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      startActivityForResult(i, 1);
    }
    if(id == R.id.load_top_image_settings) {//load a top image
      Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      startActivityForResult(i, 2);
    }
    if(id == R.id.size_mm_settings) {
      //very very duct taped.  Hacked together using internet frankencode.
      //this code is not poetry.  It is oh-noetry.
      usePixel = false;
      x_or_y = "Y";
      query("Y");
      x_or_y = "X";
      query("X");
    }
    if(id==R.id.size_px_settings){
      usePixel = true;
      x_or_y = "Y";
      query("Y");
      x_or_y = "X";
      query("X");
    }
    if(id==R.id.hide_touches_settings){
      MySurfaceView holder = ((MySurfaceView) findViewById(R.id.image_here));
      if(!holder.hidePoints) {//was showing, now hiding
        myMenu.findItem(R.id.hide_touches_settings).setTitle("Show Touches");
      } else {
        myMenu.findItem(R.id.hide_touches_settings).setTitle("Hide Touches");
      }
      holder.hidePoints = !holder.hidePoints;
      holder.invalidateme();
    }
    return super.onOptionsItemSelected(item);
  }

  public void resizeImage(int x, int y){
    MySurfaceView parent = ((MySurfaceView) findViewById(R.id.image_here));
    parent.getLayoutParams().height = y;
    if(usePixel) parent.getLayoutParams().height = ypix;
    parent.getLayoutParams().width = x;
    if(usePixel) parent.getLayoutParams().width = xpix;
    parent.setScaleType(ImageView.ScaleType.FIT_XY);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == 1 && resultCode == RESULT_OK && null != data) {//selecting bottom image
      Uri selectedImage = data.getData();
      String[] filePathColumn = {MediaStore.Images.Media.DATA};
      Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
      cursor.moveToFirst();
      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      String picturePath = cursor.getString(columnIndex);
      cursor.close();
      Bitmap bump = BitmapFactory.decodeFile(picturePath);
      bottomImage =  new BitmapDrawable(getResources(), bump);
      bottomImageIsShown = true;
      recalcCompletedImage();
      ((MySurfaceView) findViewById(R.id.image_here)).invalidateme();
    }//end of selecting bottom image
    if(requestCode == 2 && resultCode == RESULT_OK && null != data) {//selecting top image
      Uri selectedImage = data.getData();
      String[] filePathColumn = {MediaStore.Images.Media.DATA};
      Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
      cursor.moveToFirst();
      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      String picturePath = cursor.getString(columnIndex);
      cursor.close();
      Bitmap bump = BitmapFactory.decodeFile(picturePath);
      topImage = new BitmapDrawable(getResources(), bump);
      topImageIsShown = true;
      recalcCompletedImage();
      ((MySurfaceView) findViewById(R.id.image_here)).invalidateme();
    }//end of selecting top image
  }//end of onActivityResult

  private int query(String xy) {
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Enter "+xy+" length");
    alert.setMessage("Enter "+xy+" dimension");
    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    alert.setView(input);

    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        String value = input.getText().toString();
        if(x_or_y.equals("X")){
          x_mm = Integer.parseInt(value);
          xpix = x_mm;
          x_mm = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, (x_mm), getResources().getDisplayMetrics());
          resizeImage(x_mm, y_mm);
          ((MySurfaceView) findViewById(R.id.image_here)).invalidateme();
          x_or_y = "Y";
        }else{ y_mm = Integer.parseInt(value);
          ypix = y_mm;
          y_mm = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, (y_mm), getResources().getDisplayMetrics());
          resizeImage(x_mm, y_mm);
          ((MySurfaceView) findViewById(R.id.image_here)).invalidateme();
          x_or_y = "X";
        }
      }
    });

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {}
    });
    alert.show();
    return 0;
  }//end of query
}//end of class
