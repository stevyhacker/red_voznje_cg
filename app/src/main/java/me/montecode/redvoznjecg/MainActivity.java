package me.montecode.redvoznjecg;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class MainActivity extends AppCompatActivity {

    String jsonResponse;
    LinearLayout progress;
    TableView resultsTableView;
    String dateTimeFormat = "dd.MM.yyyy";
    TextView statusTextView;
    private ArrayList<String[]> values = new ArrayList<>();
    SimpleTableDataAdapter simpleTableDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = (LinearLayout) findViewById(R.id.importapiProgressLayout);
        final Spinner polazisteSpinner = (Spinner) findViewById(R.id.polazisteSpinner);
        final Spinner odredisteSpinner = (Spinner) findViewById(R.id.odredisteSpinner);
        final EditText dateEditText = (EditText) findViewById(R.id.dateEditText);
        Button searchButton = (Button) findViewById(R.id.searchButton);
        resultsTableView = (TableView) findViewById(R.id.tableView);
        statusTextView = (TextView) findViewById(R.id.statusTextView);

        ArrayAdapter<CharSequence> polazisteAdapter = ArrayAdapter.createFromResource(this,
                R.array.stanice_array, android.R.layout.simple_spinner_item);
        polazisteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        polazisteSpinner.setAdapter(polazisteAdapter);
        polazisteSpinner.setSelection(1);

        ArrayAdapter<CharSequence> odredisteAdapter = ArrayAdapter.createFromResource(this,
                R.array.stanice_array, android.R.layout.simple_spinner_item);
        odredisteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        odredisteSpinner.setAdapter(odredisteAdapter);


        SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(this, "Polazak", "Dolazak", "Prevoznik");
        simpleTableHeaderAdapter.setTextSize(16);
        simpleTableHeaderAdapter.setPaddingBottom(5);
        simpleTableHeaderAdapter.setPaddingTop(5);
        resultsTableView.setHeaderAdapter(simpleTableHeaderAdapter);

        resultsTableView.setColumnWeight(2, 5);
        resultsTableView.setColumnWeight(1, 3);
        resultsTableView.setColumnWeight(0, 3);

        simpleTableDataAdapter = new SimpleTableDataAdapter(this, values);
        resultsTableView.setDataAdapter(simpleTableDataAdapter);


        Date dateTime = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
        dateEditText.setText(simpleDateFormat.format(dateTime));

        polazisteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resultsTableView.setVisibility(View.GONE);
                if (position == odredisteSpinner.getSelectedItemPosition()) {
                    Toast.makeText(getApplicationContext(), "Izabrali ste isto polazište kao i odredište.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        odredisteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resultsTableView.setVisibility(View.GONE);
                if (position == polazisteSpinner.getSelectedItemPosition()) {
                    Toast.makeText(getApplicationContext(), "Izabrali ste isto polazište kao i odredište.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                statusTextView.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);


                String import_io_api_key = "243fc9f166cd427c85db02508655710978ff11e57c0828ba0975d25099567bdb005cbe46591a0c929b794688e69c00667a6bdcc6c50475d597865012dfa1b2f74430356c9e66deb881e22b93770b31f6";
                String import_io_user_key = "243fc9f1-66cd-427c-85db-025086557109";
                String date = dateEditText.getText().toString();
                String start_destination = polazisteSpinner.getSelectedItem().toString().toLowerCase() + "-mne";
                String end_destination = odredisteSpinner.getSelectedItem().toString().toLowerCase() + "-mne";
                String url = " https://api.import.io/store/data/0ea9345f-b9af-4e57-9029-3f923d6f72d3/_query?input/webpage/url=http%3A%2F%2Fwww.balkanviator.com%2Fme%2Fred-voznje%2F" + start_destination + "%2F" + end_destination + "%2F" + date + "&_user=" + import_io_user_key + "&_apikey=" + import_io_api_key;

                Log.e("WEBTASK URL", url);


                if (checkNetworkStatus(getApplicationContext())) {
                    statusTextView.setText("Nema rezultata.");
                    WebTask task = new WebTask();
                    task.execute(url);
                } else {
                    statusTextView.setVisibility(View.VISIBLE);
                    statusTextView.setText("Provjerite vašu internet konekciju.");
                }
            }
        });

    }

    public Boolean checkNetworkStatus(Context context) {
        Boolean status = false;

        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conManager != null) {
            NetworkInfo ni = conManager.getActiveNetworkInfo();
            if (ni != null && ni.isConnected()) {
                status = true;
            } else {
                status = false;
            }
        }

        return status;
    }

    class WebTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... url) {
            try {
                return run(url[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            }

        }

        public String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            OkHttpClient client;
            client = new OkHttpClient();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }


        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            progress.setVisibility(View.GONE);
//            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
            values.clear();
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray jsonResults = jsonResponse.getJSONArray("results");


                if (jsonResults.length() == 0) {
                    statusTextView.setVisibility(View.VISIBLE);
                } else {
                    for (int i = 0; i < jsonResults.length(); i++) {

                        JSONObject item = jsonResults.getJSONObject(i);
                        if (item.has("prevoznik/_text")) {

                            String[] drive = {item.getJSONArray("polazak_dolazak").getString(0).substring(0, 5), item.getJSONArray("polazak_dolazak").getString(1).substring(0, 5), item.getString("prevoznik/_text")};
                            Log.e("DRIVE JSON ITEM DATA", item.getJSONArray("polazak_dolazak").getString(0) + item.getJSONArray("polazak_dolazak").getString(1) + item.getString("vrijeme_putovanja") + item.getString("prevoznik/_text"));

                            values.add(drive);

                        }
                    }
                    simpleTableDataAdapter = null;
                    simpleTableDataAdapter = new SimpleTableDataAdapter(getApplicationContext(), values);
                    resultsTableView.setDataAdapter(simpleTableDataAdapter);
                    resultsTableView.setVisibility(View.VISIBLE);

                    //known issue 0.9.6 will be fixed FIX UPDATING ADAPTER
//                simpleTableDataAdapter.notifyDataSetChanged();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
}

