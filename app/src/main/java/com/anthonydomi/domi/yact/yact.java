package com.anthonydomi.domi.yact;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;



public class yact extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private Spinner dropdown1;
    private Spinner dropdown2;



    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        String curr1 = dropdown1.getSelectedItem().toString();
        String curr2 = dropdown2.getSelectedItem().toString();

        new GetRateTask().execute(getUrl(curr1, curr2));
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private class GetRateTask extends AsyncTask<String, String, Map<String, Double>> {
        TextView textStatus = (TextView) findViewById(R.id.textViewStatus);

        protected Map<String, Double> doInBackground(String... surls) {
            URL url;
            Map dictres = new HashMap();

            try {
                // get URL content

                publishProgress("Connecting to server");

                url = new URL(surls[0]);
                URLConnection conn = url.openConnection();
                publishProgress("Reading rate changes");

                // open the stream and put it into BufferedReader
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    String[] parts = inputLine.split(",");
                    dictres.put(parts[0].replace("\"", ""), Double.parseDouble(parts[2]));

                }
                br.close();
                publishProgress("done");
            } catch (MalformedURLException e) {
                publishProgress("Connection issues");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress("I/O issues");
            }


            return dictres;

        }

        protected void onProgressUpdate(String... updateText) {

            textStatus.setText(updateText[0]);
        }


        protected void onPostExecute(Map<String, Double> dictres) {

            EditText text1 = (EditText)findViewById(R.id.editText1);
            TextView text2 = (TextView)findViewById(R.id.textView);

            String curr1 = dropdown1.getSelectedItem().toString();
            String curr2 = dropdown2.getSelectedItem().toString();

            String sval = text1.getText().toString();
            if ( !sval.isEmpty())
            {
                Double val = Double.parseDouble(sval);
                text2.setText(String.format("%.2f", val * getrate(curr1, curr2, dictres)));

            }

            if (!isNetworkAvailable())
            {
                textStatus.setText("No connection available");
            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yact);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.logo);




        dropdown1= (Spinner)findViewById(R.id.spinner1);
        dropdown2= (Spinner)findViewById(R.id.spinner2);

        dropdown1.setOnItemSelectedListener(this);
        dropdown2.setOnItemSelectedListener(this);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dropdown1.setAdapter(adapter);
        dropdown2.setAdapter(adapter);

        TextView textStatus = (TextView) findViewById(R.id.textViewStatus);

        if (!isNetworkAvailable())
        {
            textStatus.setText("No connection available");
            return;
        }
        else
        {
            textStatus.setText("");
        }

        EditText editText1 = (EditText)findViewById(R.id.editText1);


        String curr1 = dropdown1.getSelectedItem().toString();
        String curr2 = dropdown2.getSelectedItem().toString();

        new GetRateTask().execute(getUrl(curr1, curr2));




        editText1.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String curr1 = dropdown1.getSelectedItem().toString();
                    String curr2 = dropdown2.getSelectedItem().toString();
                    new GetRateTask().execute(getUrl(curr1, curr2));
                    return true;
                }
                return false;
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_yact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Context context = getApplicationContext();
            CharSequence text = "Version 1.0";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public String getUrl(String curr1, String curr2)
    {

        return "http://download.finance.yahoo.com/d/quotes.csv?s=" +
                curr1 + "=X," + curr2 + "=X&f=nsl1op&e=.csv";

    }


    public Double getrate(String curr1, String curr2, Map<String, Double> currencies){

        Double k1 = 1.0;
        Double k2 = 0.0;

        for(String key: currencies.keySet())
        {
            if ( key.endsWith("/" + curr1))
            {
                k1 = currencies.get(key);
            }
            else if(key.endsWith("/" + curr2))
            {
                k2 = currencies.get(key);
            }
        }
        return k2 / k1;

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
