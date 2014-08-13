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
import android.media.Image;
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

import java.util.LinkedList;

public class MainActivity extends ActionBarActivity {

  private boolean imageIsShown = true;
  private Menu myMenu;
  Drawable image;
  Drawable blank;
  double x_mm;
  int xpix = 100;
  double y_mm;
  int ypix = 100;
  String x_or_y = "X";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    blank = new ColorDrawable(Color.LTGRAY);
    image = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(),R.drawable.ppoverlay));
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
    if(id == R.id.remove_settings) {//whitewash the current image
      if(imageIsShown) {
        ((MenuItem) myMenu.findItem(R.id.remove_settings)).setTitle("Show Image");//change text
        (findViewById(R.id.image_here)).setBackgroundDrawable(blank);//remove image
      }
      if(!imageIsShown) {
        ((MenuItem) myMenu.findItem(R.id.remove_settings)).setTitle("Hide Image");//change text
        (findViewById(R.id.image_here)).setBackgroundDrawable(image);//remove image
      }
      imageIsShown = !imageIsShown;
      return true;
    }

    if(id == R.id.clear_settings) {
      ((MySurfaceView) findViewById(R.id.image_here)).clearTouches();
    }
    if(id == R.id.load_settings) {
      Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      startActivityForResult(i, 1);
    }
    if(id == R.id.size_settings) {
      //very very duct taped.  Hacked together using internet frankencode.
      //this code is not poetry.  It is oh-noetry.
      Log.d("DEBUG","Size settings just called.  XXXXXXXXXXXXXXXXXXXXX");
      Log.d("DEBUG", "x_or_y just set to Y");
      x_or_y = "Y";
      query("Y");
      Log.d("DEBUG", "x_or_y just set to X");
      x_or_y = "X";
      query("X");
      int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, (int)(x_mm), getResources().getDisplayMetrics());
      int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, (int) (y_mm), getResources().getDisplayMetrics());
      Log.d("DEBUG","Custom View dimension set, in pixels, to: "+width+" x "+height);
    }
    if(id==R.id.hide_settings){
      MySurfaceView holder = ((MySurfaceView) findViewById(R.id.image_here));
      if(!holder.hidePoints) {//was showing, now hiding
        myMenu.findItem(R.id.hide_settings).setTitle("Show Touches");
      }
      if(holder.hidePoints) {
        myMenu.findItem(R.id.hide_settings).setTitle("Hide Touches");
      }
      holder.hidePoints = !holder.hidePoints;
      holder.invalidateme();
    }
    return super.onOptionsItemSelected(item);
  }

  public void resizeImage(int xpx, int ypx){
    MySurfaceView parent = ((MySurfaceView) findViewById(R.id.image_here));
    parent.getLayoutParams().height = ypx;
    parent.getLayoutParams().width = xpx;
    parent.setScaleType(ImageView.ScaleType.FIT_XY);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == 1 && resultCode == RESULT_OK && null != data) {
      Uri selectedImage = data.getData();
      String[] filePathColumn = {MediaStore.Images.Media.DATA};
      Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
      cursor.moveToFirst();
      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      String picturePath = cursor.getString(columnIndex);
      cursor.close();
      MySurfaceView parent = ((MySurfaceView) findViewById(R.id.image_here));
      Bitmap bump = BitmapFactory.decodeFile(picturePath);
      Drawable draw = new BitmapDrawable(getResources(), bump);
      parent.setBackgroundDrawable(draw);
      image = draw;
      ((MySurfaceView) findViewById(R.id.image_here)).invalidateme();
      imageIsShown = true;
    }
  }




  private int query(String xy) {
    Log.d("DEBUG","Query just called. x_or_y set to " + x_or_y);
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Enter "+xy+" length");
    alert.setMessage("Enter "+xy+" dimension in millimeters");
    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    alert.setView(input);

    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        String value = input.getText().toString();
        if(x_or_y.equals("X")){ x_mm = Double.parseDouble(value);
          Log.d("DEBUG", "X dimension set to" + x_mm);
          xpix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, (int)(x_mm), getResources().getDisplayMetrics());
          resizeImage(xpix, ypix);
          ((MySurfaceView) findViewById(R.id.image_here)).invalidateme();
          x_or_y = "Y";
        }else{ y_mm = Double.parseDouble(value);
          Log.d("DEBUG", "Y dimension set to" + y_mm);
          ypix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, (int) (y_mm), getResources().getDisplayMetrics());
          resizeImage(xpix, ypix);
          ((MySurfaceView) findViewById(R.id.image_here)).invalidateme();
          x_or_y = "X";
        }
        return;
        // Do something with value!
      }
    });

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {

        return;}
    });
    alert.show();
    return 0;
  }
}
