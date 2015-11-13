package edu.lawrence.cafeorderapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ProductView extends AppCompatActivity {

    public String orderNumber;
    private JSONArray products;
    private int selected_product;
    private int productName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);
        Log.d("Cafe", "Loading");
        new ShowProducts().execute();
    }



    private class ShowProducts extends AsyncTask<String, Void, String> {
        private String uri;

        ShowProducts() {
            uri = "http://" + URIHandler.hostName + "/RESTCafeApplication/api/product";
        }

        @Override
        protected String doInBackground(String... urls) {

            try {
                return URIHandler.doGet(uri, "");
            } catch (IOException e) {
                return "";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            loadProducts(result);
        }
    }

    public void loadProducts(String json) {
        String[] productList;
        products = null;

        ListView handlesList = (ListView) findViewById(R.id.product_view);

        /*products = new JSONArray(json);
        int length = products.length();
        String lengthString = String.valueOf(length);
        Log.d("Cafe SHOWING", lengthString);*/

        try {
            products = new JSONArray(json);
            productList = new String[products.length()];
            for(int n = 0;n < productList.length;n++) {
                JSONObject handle = products.getJSONObject(n);
                productList[n] = handle.getString("name");
            }
        } catch (JSONException ex) {
            Log.d("Cafe", "Exception in loadProducts: " + ex.getMessage());
            productList = new String[0];
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, productList);
        handlesList.setAdapter(adapter);

        handlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int i, long l) {
                // remember the selection
                selected_product = i + 1;
            }
        });
    }

    private class AddOrderTaskToInvoice extends AsyncTask<String, Void, String> {
        private String uri;
        private String toSend;

        AddOrderTaskToInvoice(String toSend) {
            uri = "http://" + URIHandler.hostName + "/RESTCafeApplication/api/invoice/";
            this.toSend = toSend;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return URIHandler.doPost(uri, toSend);
            } catch (IOException e) {
                return "";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String orderId) {
            // After posting the message, we will want to post the individual recipients.
            //Log.d("Cafe", "Reaching");
            if (orderNumber == null) {
                orderNumber = orderId;
            }
            int orderNumberInt = Integer.valueOf(orderNumber);
            new AddOrderTaskToItem("{\"product\":" + selected_product + ", \"ordernumber\":" + orderNumberInt + "}").execute();
            //addOrder(orderId); //-----> CHANGE THIS
        }
    }


    private class AddOrderTaskToItem extends AsyncTask<String, Void, String> {
        private String uri;
        private String toSend;

        AddOrderTaskToItem(String toSend) {
            uri = "http://" + URIHandler.hostName + "/RESTCafeApplication/api/item/";
            this.toSend = toSend;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return URIHandler.doPost(uri, toSend);
            } catch (IOException e) {
                return "";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String orderId) {
            // After posting the message, we will want to post the individual recipients.
            //orderNumber = Integer.valueOf(orderId);
            //addOrder(orderId); //-----> CHANGE THIS
        }
    }

    public void addOrder(View view) {
        //ListView productList = (ListView) findViewById(R.id.product_view);

        //ListAdapter productListAdapter = productList.getAdapter();

        //Object product = productListAdapter.getItem(selected_product);

        //productName = product.toString();

        //Log.d("Cafe", productName);

        new AddOrderTaskToInvoice("{\"customer\":\"unknown\",\"phone\":\"unknown\"}").execute();

        //Log.d("Cafe", orderNumber + "hello");
    }

    public void viewOrder(View view) {
        Intent intent = new Intent(this, OrderView.class);
        //intent.putExtra(OrderView.orderNumber, orderNumber);
        intent.putExtra(OrderView.orderNumber, orderNumber);
        //intent.putString(OrderView.orderNumber, orderNumber);
        startActivity(intent);
    }



}
