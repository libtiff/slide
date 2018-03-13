package trikita.slide.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;

import trikita.jedux.Action;
import trikita.promote.Promote;
import trikita.slide.ActionType;
import trikita.slide.App;
import trikita.slide.middleware.StorageController;

public class MainActivity extends Activity {
    String path = "";
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(new MainLayout(this));
        App.getWindowController().setWindow(getWindow());
        //Fixing the keyboard Issue #10
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //Fix the keyboard Issue #2
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (App.getState().presentationMode()) {
            App.dispatch(new Action<>(ActionType.OPEN_PRESENTATION));
        }
        Promote.after(3).times().every(10).times().rate(this);
        Promote.after(5).times().every(5).times().share(this,
                Promote.FACEBOOK_TWITTER,
                "https://github.com/trikita/slide",
                "Slide: Minimal presentation maker");
    }

    @Override
    protected void onDestroy() {
        App.getWindowController().setWindow(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (App.getState().presentationMode()) {
            App.dispatch(new Action<>(ActionType.CLOSE_PRESENTATION));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        if (requestCode == StorageController.EXPORT_PDF_REQUEST_CODE) {
            Uri uri = data.getData();
            App.dispatch(new Action<>(ActionType.EXPORT_PDF, uri));
        } else if (requestCode == StorageController.PICK_IMAGE_REQUEST_CODE) {
            Uri uri = data.getData();
            App.dispatch(new Action<>(ActionType.INSERT_IMAGE, uri));
        } else if (requestCode == StorageController.OPEN_DOCUMENT_REQUEST_CODE) {
            Uri uri = data.getData();
            App.dispatch(new Action<>(ActionType.LOAD_DOCUMENT, uri));
        }   //Adding the save function
        else if (requestCode == StorageController.SAVE_DOCUMENT_REQUEST_CODE) {
            Uri uri = data.getData();
            App.dispatch(new Action<>(ActionType.SAVE_DOCUMENT, uri));
        }
        //OpenPDF
        else if(requestCode == StorageController.OPEN_PDF_REQUEST_CODE)
        {
            Uri uri = data.getData();
            //App.dispatch(new Action<>(ActionType.OPEN_PDF, uri));
            String FilePath = getRealPathFromURI(uri);

            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ FilePath);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(file),"application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent intent = Intent.createChooser(target, "Open File");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                //Message if no pdf viewer is present
            }
        }
    }
    //GetPathForPDF
    public String getRealPathFromURI(Uri contentUri) {
        String [] proj      = {MediaStore.Images.Media.DATA};
        Cursor cursor       = getContentResolver().query( contentUri, proj, null, null,null);
        if (cursor == null) return null;
        int column_index    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
