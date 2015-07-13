package com.anthonydomi.domi.yact;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;



public class yact extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner dropdown1;
    private Spinner dropdown2;
    private EditText text1;

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

        protected Map<String, Double> doInBackground(String... sUrls) {
            URL url;
            Map<String, Double> dictRes = new HashMap<>();

            try {
                // get URL content

                publishProgress("Connecting to server");

                url = new URL(sUrls[0]);
                URLConnection conn = url.openConnection();
                publishProgress("Reading rate changes");

                // open the stream and put it into BufferedReader
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    String[] parts = inputLine.split(",");
                    dictRes.put(parts[0].replace("\"", ""), Double.parseDouble(parts[2]));

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

            return dictRes;

        }

        protected void onProgressUpdate(String... updateText) {

            textStatus.setText(updateText[0]);
        }


        protected void onPostExecute(Map<String, Double> dictRes) {

            //EditText text1 = (EditText)findViewById(R.id.editText1);
            TextView text2 = (TextView)findViewById(R.id.textView);
            String curr1 = dropdown1.getSelectedItem().toString();
            String curr2 = dropdown2.getSelectedItem().toString();

            NumberFormat format = NumberFormat.getInstance();
            format.setMaximumFractionDigits(2);
            Double val;

            val = getTextAndSetFormat();
            text2.setText(format.format(val * getRate(curr1, curr2, dictRes)));

            if (!isNetworkAvailable())
            {
                textStatus.setText("No connection available");
            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int cnt;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yact);

        ActionBar actionBar = getSupportActionBar();
        if ( actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.logo);
        }

        FloatingActionButton buttonSwap;
        buttonSwap = (FloatingActionButton)findViewById(R.id.button);
        dropdown1 = (Spinner)findViewById(R.id.spinner1);
        dropdown2 = (Spinner)findViewById(R.id.spinner2);
        text1 = (EditText)findViewById(R.id.editText1);
        text1.setSelectAllOnFocus(true);

        text1.setText("1");

        dropdown1.setOnItemSelectedListener(this);
        dropdown2.setOnItemSelectedListener(this);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cnt = adapter.getCount();

        dropdown1.setAdapter(adapter);
        dropdown2.setAdapter(adapter);
        if ( cnt > 4)
        {
            dropdown1.setSelection(0);
            dropdown2.setSelection(4);
        }
        else if ( cnt > 1)
        {
            dropdown1.setSelection(0);
        }



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

        String curr1 = dropdown1.getSelectedItem().toString();
        String curr2 = dropdown2.getSelectedItem().toString();

        new GetRateTask().execute(getUrl(curr1, curr2));

        buttonSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i1 = dropdown1.getSelectedItemPosition();
                int i2 = dropdown2.getSelectedItemPosition();
                dropdown1.setSelection(i2);
                dropdown2.setSelection(i1);
                String curr1 = dropdown1.getSelectedItem().toString();
                String curr2 = dropdown2.getSelectedItem().toString();
                new GetRateTask().execute(getUrl(curr1, curr2));
            }
        });

        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text1.selectAll();
            }
        });


        text1.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the key event is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String curr1 = dropdown1.getSelectedItem().toString();
                    String curr2 = dropdown2.getSelectedItem().toString();

                    InputMethodManager in = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(text1.getApplicationWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    text1.clearFocus();
                    getTextAndSetFormat();

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
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context,  BuildConfig.VERSION_NAME, duration);
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


    public Double getRate(String curr1, String curr2, Map<String, Double> currencies){

        Double k1 = 1.0;
        Double k2 = 0.0;

        if (curr1.equals(curr2))
            return 1.0;

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

    private Double getTextAndSetFormat(){

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        Double val;
        String sVal;


        try{
            val = format.parse(text1.getText().toString()).doubleValue();
        }
        catch (java.text.ParseException pE)
        {
            sVal = text1.getText().toString();
            if ( !sVal.isEmpty()) {
                val = Double.parseDouble(sVal);
            }
            else {
                val = 0.0;
            }
        }

        text1.setText(format.format(val));
        return val;
    }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
