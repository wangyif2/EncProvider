package se.yifan.android.encprovider.SampleContacts;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import se.yifan.android.encprovider.R;
import se.yifan.android.encprovider.SampleContacts.contentprovider.ContactProvider;
import se.yifan.android.encprovider.SampleContacts.database.ContactTable;

public class ContactsOverviewActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int DELETE_ID = Menu.FIRST;
    private SimpleCursorAdapter adapter;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);
        this.getListView().setDividerHeight(2);
        fillData();
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                Uri uri = Uri.parse(ContactProvider.CONTENT_URI + "/" + info.id);
                getContentResolver().delete(uri, null, null);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert:
                createContact();
                return true;
            case R.id.sample_data:
                loadSampleContacts();
                return true;
            case R.id.delete_all:
                deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, ContactDetailActivity.class);
        Uri todoUri = Uri.parse(ContactProvider.CONTENT_URI + "/" + id);
        i.putExtra(ContactProvider.CONTENT_ITEM_TYPE, todoUri);

        startActivity(i);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {ContactTable.COLUMN_ID, ContactTable.COLUMN_NAME};
        return new CursorLoader(this, ContactProvider.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void fillData() {
        String[] from = new String[]{ContactTable.COLUMN_NAME};
        int[] to = new int[]{R.id.label};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(this, R.layout.contact_row, null, from, to, 0);

        setListAdapter(adapter);
    }

    private void deleteAll() {
        getContentResolver().delete(ContactProvider.CONTENT_URI, null, null);
    }

    private void loadSampleContacts() {
        ContentValues John = new ContentValues();
        John.put(ContactTable.COLUMN_NAME, "John Doe");
        John.put(ContactTable.COLUMN_EMAIL, "john.doe@gmail.com");
        John.put(ContactTable.COLUMN_AGE, "32");

        ContentValues Jane = new ContentValues();
        Jane.put(ContactTable.COLUMN_NAME, "Jane Doe");
        Jane.put(ContactTable.COLUMN_EMAIL, "jane.doe@gmail.com");
        Jane.put(ContactTable.COLUMN_AGE, "22");

        ContentValues Richard = new ContentValues();
        Richard.put(ContactTable.COLUMN_NAME, "Richard Hamilton");
        Richard.put(ContactTable.COLUMN_EMAIL, "richard.hamilton@gmail.com");
        Richard.put(ContactTable.COLUMN_AGE, "15");

        ContentValues Ada = new ContentValues();
        Ada.put(ContactTable.COLUMN_NAME, "Ada Li");
        Ada.put(ContactTable.COLUMN_EMAIL, "ada.li@gmail.com");
        Ada.put(ContactTable.COLUMN_AGE, "23");

        ContentValues Maria = new ContentValues();
        Maria.put(ContactTable.COLUMN_NAME, "Maria Maureen");
        Maria.put(ContactTable.COLUMN_EMAIL, "maria.maureen@gmail.com");
        Maria.put(ContactTable.COLUMN_AGE, "86");

        ContentValues[] contacts = new ContentValues[]{
                Jane, John, Richard, Ada, Maria
        };
        getContentResolver().bulkInsert(ContactProvider.CONTENT_URI, contacts);
        fillData();
    }

    private void createContact() {
        Intent i = new Intent(this, ContactDetailActivity.class);
        startActivity(i);
    }

}