package se.yifan.android.encprovider.SampleContacts;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import se.yifan.android.encprovider.R;
import se.yifan.android.encprovider.SampleContacts.contentprovider.ContactProvider;
import se.yifan.android.encprovider.SampleContacts.database.ContactTable;

public class ContactDetailActivity extends Activity {
    private EditText mContactName, mContactEmail, mContactAge;

    private Uri contactUri;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.contact_edit);

        mContactName = (EditText) findViewById(R.id.contact_name);
        mContactEmail = (EditText) findViewById(R.id.contact_email);
        mContactAge = (EditText) findViewById(R.id.contact_age);
        Button confirmButton = (Button) findViewById(R.id.contact_confirm_button);

        Bundle extras = getIntent().getExtras();

        contactUri = (bundle == null) ? null : (Uri) bundle.getParcelable(ContactProvider.CONTENT_ITEM_TYPE);

        if (extras != null) {
            contactUri = extras.getParcelable(ContactProvider.CONTENT_ITEM_TYPE);
            fillData(contactUri);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (TextUtils.isEmpty(mContactEmail.getText().toString())) {
                    makeToast();
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

    }

    private void fillData(Uri uri) {
        String[] projection = {ContactTable.COLUMN_EMAIL, ContactTable.COLUMN_AGE, ContactTable.COLUMN_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();

            mContactName.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactTable.COLUMN_NAME)));
            mContactEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactTable.COLUMN_EMAIL)));
            mContactAge.setText(cursor.getString(cursor.getColumnIndexOrThrow(ContactTable.COLUMN_AGE)));

            cursor.close();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putParcelable(ContactProvider.CONTENT_ITEM_TYPE, contactUri);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    private void saveState() {
        String name = mContactName.getText().toString();
        String email = mContactEmail.getText().toString();
        String age = mContactAge.getText().toString();

        if (age.length() == 0 && email.length() == 0) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ContactTable.COLUMN_NAME, name);
        values.put(ContactTable.COLUMN_EMAIL, email);
        values.put(ContactTable.COLUMN_AGE, age);

//        if (contactUri == null) {
//            contactUri = getContentResolver().insert(ContactProvider.CONTENT_URI, values);
//        } else {
//            getContentResolver().update(contactUri, values, null, null);
//        }
    }

    private void makeToast() {
        Toast.makeText(ContactDetailActivity.this, "Please enter a name", Toast.LENGTH_LONG).show();
    }
}
