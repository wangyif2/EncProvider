package com.yifandroid.encprovider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class EncProviderActivity extends Activity {

    private SQLiteAdapter sqLiteAdapter;
    ListView contentList;
    Button btInsert, btDisplay, btClear, btDeleteAll;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;
        sqLiteAdapter = new SQLiteAdapter(this);
        contentList = (ListView) findViewById(R.id.content_list);
        btInsert = (Button) findViewById(R.id.bt_insert);
        btClear = (Button) findViewById(R.id.bt_clear);
        btDeleteAll = (Button) findViewById(R.id.bt_deleteAll);
        btDisplay = (Button) findViewById(R.id.bt_display);

        btInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                final EditText usrInputView = new EditText(context);

                alert.setView(usrInputView);
                alert.setTitle("Insert into EncDatabase");
                alert.setMessage("Enter String Content:");

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Editable usrInputText = usrInputView.getText();
                        Log.i("EncProvider", usrInputText.toString());

                        insertToDatabase(usrInputText);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

                alert.show();
            }
        });

        btDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sqLiteAdapter.openToRead();

                Cursor cursor = sqLiteAdapter.queueAll();
                startManagingCursor(cursor);

                String[] from = new String[]{SQLiteAdapter.KEY_CONTENT};
                int[] to = new int[]{R.id.row_text};
                SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(context, R.layout.row, cursor, from, to, 0);
                contentList.setAdapter(simpleCursorAdapter);
                contentList.setEmptyView(findViewById(R.id.empty_list_view));

                sqLiteAdapter.close();
            }
        });

        btDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sqLiteAdapter.openToWrite();
                sqLiteAdapter.deleteAll();
                sqLiteAdapter.close();
            }
        });

        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentList.setAdapter(null);
            }
        });

    }

    private void insertToDatabase(Editable usrInputText) {
        sqLiteAdapter.openToWrite();
        sqLiteAdapter.insert(usrInputText.toString());
        sqLiteAdapter.close();
    }


}
