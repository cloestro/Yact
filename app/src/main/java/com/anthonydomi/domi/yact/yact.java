package com.anthonydomi.domi.yact;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class yact extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner dropdown1;
    private Spinner dropdown2;
    private EditText text1;
    private Switch switchManYen;
    private String ratesURL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    private SwipeRefreshLayout mySwipeRefreshLayout;

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        String curr1 = dropdown1.getSelectedItem().toString();
        String curr2 = dropdown2.getSelectedItem().toString();
        updateManYen(curr1, curr2);

        //new GetCurrenciesTask().execute(ratesURL);
        updateFromPrefs();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int cnt;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_yact);

        TextView textStatus = (TextView) findViewById(R.id.textViewStatus);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.logo);
        }

        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        TextView textStatus1 = (TextView) findViewById(R.id.textViewStatus);

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        if (!isNetworkAvailable()) {
                            textStatus1.setText(R.string.noConnection);
                            mySwipeRefreshLayout.setRefreshing(false);
                        } else {
                            new GetCurrenciesTask().execute(ratesURL);
                            mySwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
        );

        FloatingActionButton buttonSwap;
        buttonSwap = (FloatingActionButton) findViewById(R.id.button);
        switchManYen = (Switch) findViewById(R.id.switchManYen);
        dropdown1 = (Spinner) findViewById(R.id.spinner1);
        dropdown2 = (Spinner) findViewById(R.id.spinner2);
        text1 = (EditText) findViewById(R.id.editText1);
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
        if (cnt > 4) {
            dropdown1.setSelection(0);
            dropdown2.setSelection(4);
        } else if (cnt > 1) {
            dropdown1.setSelection(0);
        }



        String curr1 = dropdown1.getSelectedItem().toString();
        String curr2 = dropdown2.getSelectedItem().toString();

        updateManYen(curr1, curr2);

        if (!isNetworkAvailable()) {
            textStatus.setText(R.string.noConnection);
            // Use last saved rates
            updateFromPrefs();
            //return;
        } else {
            textStatus.setText("");
            new GetCurrenciesTask().execute(ratesURL);
        }


        buttonSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i1 = dropdown1.getSelectedItemPosition();
                int i2 = dropdown2.getSelectedItemPosition();
                dropdown1.setSelection(i2);
                dropdown2.setSelection(i1);
                String curr1 = dropdown1.getSelectedItem().toString();
                String curr2 = dropdown2.getSelectedItem().toString();
                updateManYen(curr1, curr2);
                updateFromPrefs();
                //new GetCurrenciesTask().execute(ratesURL);
            }
        });

        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text1.selectAll();
            }
        });

        switchManYen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String curr1 = dropdown1.getSelectedItem().toString();
                String curr2 = dropdown2.getSelectedItem().toString();
                //new GetCurrenciesTask().execute(ratesURL);
                updateFromPrefs();
                //new GetRateTask().execute(getUrl(curr1, curr2));
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
                    updateManYen(curr1, curr2);
                    //new GetCurrenciesTask().execute(ratesURL);
                    updateFromPrefs();
                    //new GetRateTask().execute(getUrl(curr1, curr2));
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
        TextView textStatus = (TextView) findViewById(R.id.textViewStatus);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, BuildConfig.VERSION_NAME, duration);
            toast.show();
            return true;
        }
        if (id == R.id.menu_refresh){
            mySwipeRefreshLayout.setRefreshing(true);
            if (!isNetworkAvailable()) {
                textStatus.setText(R.string.noConnection);
                mySwipeRefreshLayout.setRefreshing(false);
            } else {
                new GetCurrenciesTask().execute(ratesURL);
                mySwipeRefreshLayout.setRefreshing(false);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    public void computeRate(Map<String, Double> dictRates){
        TextView text2 = (TextView) findViewById(R.id.textView);
        String curr1 = dropdown1.getSelectedItem().toString();
        String curr2 = dropdown2.getSelectedItem().toString();

        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        Double val;

        val = getTextAndSetFormat();
        if (curr1.equals("JPY") && switchManYen.isChecked()) {
            text2.setText(format.format(1.0e4 * val * getRateEU(curr1, curr2, dictRates)));
        } else if (curr2.equals("JPY") && switchManYen.isChecked()) {
            text2.setText(format.format(1.0e-4 * val * getRateEU(curr1, curr2, dictRates)));
        } else {
            text2.setText(format.format(val * getRateEU(curr1, curr2, dictRates)));
        }
    }


    public Double getRateEU(String curr1, String curr2, Map<String, Double> currencies) {

        Double k1 = 1.0;
        Double k2 = 0.0;

        if (curr1.equals(curr2))
            return 1.0;

        if (curr1.equals("EUR"))
            k1 = 1.0;

        if (curr2.equals("EUR"))
            k2 = 1.0;

        for (String key : currencies.keySet()) {
            if (key.endsWith(curr1)) {
                k1 = currencies.get(key);
            } else if (key.endsWith(curr2)) {
                k2 = currencies.get(key);
            }
        }
        return k2 / k1;

    }

    private void writePrefs(Map<String, Double> EURates){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (String key : EURates.keySet()) {
            editor.putInt(key, (int)Math.round(EURates.get(key)*10000));
        }
        editor.apply();
    }
    private Map<String, Double> readPrefsRate(){
        Map<String, Double> dictRates = new HashMap<>();
        String[] myStrings = getResources().getStringArray(R.array.currencies_array);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        for (String s: myStrings) {
            if (s.equals("EUR")){
                dictRates.put(s, 1.0);
            }
            else{
                Double val = sharedPref.getInt(s, 0)/10000.0;
                dictRates.put(s, val);
            }

        }
        return dictRates;
    }

    private void updateFromPrefs()
    {
        Map<String, Double> dictRates = new HashMap<>();
        dictRates = readPrefsRate();
        computeRate(dictRates);
    }

    private Double getTextAndSetFormat() {

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        Double val;
        String sVal;
        try {
            val = format.parse(text1.getText().toString()).doubleValue();
        } catch (java.text.ParseException pE) {
            sVal = text1.getText().toString();
            if (!sVal.isEmpty()) {
                val = Double.parseDouble(sVal);
            } else {
                val = 0.0;
            }
        }
        text1.setText(format.format(val));
        return val;
    }

    private void updateManYen(String curr1, String curr2) {
        if (curr1.equals("JPY") || curr2.equals("JPY")) {
            switchManYen.setVisibility(View.VISIBLE);
        } else {
            switchManYen.setVisibility(View.GONE);
        }
    }

    private  String getDateCurrentTimeZone(long timestamp) {

            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            TimeZone tz = TimeZone.getDefault();
            //calendar.setTimeInMillis(timestamp * 1000);
            calendar.setTimeInMillis(timestamp);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class GetCurrenciesTask extends AsyncTask<String, String, Map<String, Double>> {
        String xmlContent = "";
        TextView textStatus = (TextView) findViewById(R.id.textViewStatus);
        Map<String, Double> dictRates = new HashMap<>();
        long lastDate = 0;

        protected Map<String, Double> doInBackground(String... sUrls) {
            URL url;
            try {
                // get URL content
                url = new URL(sUrls[0]);
                URLConnection conn = url.openConnection();

                publishProgress("Connecting");
                //publishProgress(R.string.connectingServer);

                // open the stream and put it into BufferedReader
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                publishProgress("reading rates");
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    xmlContent += inputLine;
                }
                lastDate=conn.getLastModified();
                br.close();

            } catch (MalformedURLException e) {
                publishProgress("Network issues");
                e.printStackTrace();
            } catch (IOException e) {
                publishProgress("Input output errors");
                e.printStackTrace();
            }
            publishProgress("done");


            //reading xml
            publishProgress("reading xml");
            try{
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput( new StringReader( xmlContent) );
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_TAG) {
                        String currName = "";
                        double changeVal = -1;
                        for (int i=0; i < xpp.getAttributeCount(); i++){
                            //Log.d("plop", xpp.getAttributeName(i));

                            if (xpp.getAttributeName(i).contains("currency")){
                                currName = xpp.getAttributeValue(i);
                                publishProgress(xpp.getAttributeValue(i));
                            }
                            if (xpp.getAttributeName(i).contains("rate")){
                                changeVal = Double.parseDouble(xpp.getAttributeValue(i));
                            }

                            if ( !currName.isEmpty() && changeVal > 0){
                                dictRates.put(currName, changeVal);
                            }
                        }
                    }
                    eventType = xpp.next();
                }
            }catch (XmlPullParserException e) {
                publishProgress(e.getLocalizedMessage());
            } catch (IOException e) {
                publishProgress("Input output error");
            }

            if (lastDate != 0){
                publishProgress("Last Modified: " + getDateCurrentTimeZone(lastDate));
            }
            return dictRates;
        }

        protected void onProgressUpdate(String... updateText) {
            textStatus.setText(updateText[0]);

        }
        protected void onPostExecute(Map<String, Double> dictRates) {
            computeRate(dictRates);
            writePrefs(dictRates);
            mySwipeRefreshLayout.setRefreshing(false);
        }

    }

}
