package com.example.kathu228.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;
    // a numeric code to identify the edit activity
    public static final int EDIT_REQUEST_CODE = 20;
    // keys used for passing data between activities
    public static final String ITEM_TEXT = "itemText";
    public static final String ITEM_POSITION = "itemPosition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // obtains a reference to the ListView
        lvItems = (ListView) findViewById(R.id.lvItems);
        // initializes the item list
//        items = new ArrayList<>();
        readItems();
        // intializes the adapter
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        // wires adapter to view
        lvItems.setAdapter(itemsAdapter);

        // adds initial items to list
//        items.add("Hello");
//        items.add("World");

        // setups the listener on creation
        setupListViewListener();
    }

    public void onAddItem(View v) {
        // obtains a reference to the EditText created with the layout
        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        // grabs the EditText's content as a String
        String itemText = etNewItem.getText().toString();
        // adds the item to the list via the adapter
        itemsAdapter.add(itemText);
        // stores the updated list
        writeItems();
        // clears the EditText by setting it to an empty String
        etNewItem.setText("");
        // display a notification to the user
        Toast.makeText(getApplicationContext(), "Item added to list", Toast.LENGTH_SHORT).show();
    }

    private void setupListViewListener() {

        // set the ListView's itemLongClickListener
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // remove the item in the list at the index given by position
                items.remove(position);
                // notify the adapter that the underlying dataset changed
                itemsAdapter.notifyDataSetChanged();
                // stores the updated list
                writeItems();
                // displays a notification that an item was removed
                Log.i("MainActivity", "Removed item " + position);
                // return true to tell the framework that the long click was consumed
                return true;
            }
        });

        // set the item regular click listener
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // first parameter is the context, second is the class of the activity to launch
                Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                // put "extras" into the bundle for access in the edit activity
                i.putExtra(ITEM_TEXT, items.get(position));
                i.putExtra(ITEM_POSITION, position);
                // brings up the edit activity with the expectation of a result
                startActivityForResult(i, EDIT_REQUEST_CODE);
            }
        });
    }

    // returns the file in which the dat is stored
    private File getDataFile() {
        return new File(getFilesDir(), "todo.txt");
    }

    // reads the items from the file system
    private void readItems() {
        try {
            // creates the array using the content in the file
            items = new ArrayList<String>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            // print the error to the console
            e.printStackTrace();
            // just load an empty list
            items = new ArrayList<>();
        }
    }

    // write the items to the filesystem
    private void writeItems() {
        try {
            // save the item list as a line-delimited text file
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            // print the error to the console
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // EDIT_REQUEST_CODE defined with constants
        if (resultCode == RESULT_OK && requestCode == EDIT_REQUEST_CODE) {
            // extract updated item value from result extras
            String updatedItem = data.getExtras().getString(ITEM_TEXT);
            // get the position of the item which was edited
            int position = data.getExtras().getInt(ITEM_POSITION, 0);
            // update the model with the new item text at the edited position
            items.set(position, updatedItem);
            // notify the adapter the model changed
            itemsAdapter.notifyDataSetChanged();
            // Store the updated items back to disk
            writeItems();
            // notify the user the operation completed OK
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
        }
    }
}