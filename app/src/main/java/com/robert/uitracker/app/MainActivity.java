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

  private boolean imageIsShown = true;
  private Menu myMenu;
  Drawable image;
  Drawable blank;
  int x_mm;
  int xpix = 400;
  int y_mm;
  int ypix = 400;
  String x_or_y = "X";
  boolean usePixel =false;

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
        (myMenu.findItem(R.id.remove_settings)).setTitle("Show Image");//change text
        (findViewById(R.id.image_here)).setBackgroundDrawable(blank);//remove image
      }
      if(!imageIsShown) {
        (myMenu.findItem(R.id.remove_settings)).setTitle("Hide Image");//change text
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
      usePixel = false;
      x_or_y = "Y";
      query("Y");
      x_or_y = "X";
      query("X");
    }
    if(id==R.id.sizepx_settings){
      usePixel = true;
      x_or_y = "Y";
      query("Y");
      x_or_y = "X";
      query("X");
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
  }
}
