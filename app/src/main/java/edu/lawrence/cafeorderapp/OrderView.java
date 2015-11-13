package edu.lawrence.cafeorderapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by malikgraham on 11/2/15.
 */
public class OrderView extends AppCompatActivity {

    public static String orderNumber;
    private JSONArray orders;
    private int selected_order;
    private ArrayList<String> product_info = new ArrayList<String>();
    private HashMap<String, Integer> productNameToId = new HashMap<String, Integer>();
    private int toBeDeletedItem;
    private String orderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_view);
        Log.d("Cafe", "Loading");

        Intent intent = getIntent();
        orderNumber = intent.getStringExtra(OrderView.orderNumber);

        new ShowOrderList().execute();
    }

    private class ShowOrderList extends AsyncTask<String, Void, String> {
        private String uri;

        ShowOrderList() {
            uri = "http://" + URIHandler.hostName + "/RESTCafeApplication/api/item?ordernumber=" + Integer.valueOf(orderNumber);
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
            loadOrderList(result);
        }
    }

    private class getProductName extends AsyncTask<String, Void, String> {
        private String uri;


        getProductName(String productID) {
            uri = "http://" + URIHandler.hostName + "/RESTCafeApplication/api/product/"+ Integer.valueOf(productID);

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
                showProductName(result);
            //product_info.add(result);
        }
    }

    public void loadOrderList(String json) {
        String[] productList;
        orders = null;

        /** Here you were missing some logic to reset some of your lists
         *  when the order details change. I added that logic here:
         */
        product_info.clear();
        productNameToId.clear();

        try {
            orders = new JSONArray(json);
            productList = new String[orders.length()];
            for(int n = 0;n < productList.length;n++) {
                JSONObject handle = orders.getJSONObject(n);
                new getProductName(handle.getString("product")).execute();
            }
        } catch (JSONException ex) {
            Log.d("Cafe", "Exception in loadOrderList: " + ex.getMessage());
            productList = new String[0];
        }

    }

    public void showProductName(String json) {

        ListView handlesList = (ListView) findViewById(R.id.order_view);

        try {
                JSONObject JSONorder = new JSONObject(json);
                productNameToId.put(JSONorder.getString("name"), JSONorder.getInt("idproduct"));
                product_info.add(JSONorder.getString("name"));
        } catch (JSONException ex) {
            Log.d("Cafe", "Exception in showProductsName: " + ex.getMessage());
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, product_info);
        handlesList.setAdapter(adapter);

        handlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int i, long l) {

                selected_order = i;
            }
        });
    }

    public class DeleteOrder extends AsyncTask<String, Void, Void> {
        private String uri;
        private int id;

        DeleteOrder(int id) {
            uri = "http://" + URIHandler.hostName + "/RESTCafeApplication/api/item/" + id;
            this.id = id;
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                URIHandler.doDelete(uri);
            } catch (IOException e) {
            }
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            selected_order = -1;
            //productNameToId.
            for (String x : product_info) {
                if (x.equals(orderName)) {
                    product_info.remove(x);
                }

            }
           new ShowOrderList().execute();
        }
    }

    public void removeOrder(View view) {
        ListView productList = (ListView) findViewById(R.id.order_view);
        ListAdapter productListAdapter = productList.getAdapter();
        Object order = productListAdapter.getItem(selected_order);
        orderName = order.toString();
        Integer productId = productNameToId.get(orderName);

        String[] productListStringArray;

        try {
            //orders = new JSONArray(json);
            productListStringArray = new String[orders.length()];
            for (int n = 0; n < productListStringArray.length; n++) {
                JSONObject handle = orders.getJSONObject(n);
                if (productId == handle.getInt("product")) {
                    toBeDeletedItem = handle.getInt("iditem");
                }
                //new getProductName(handle.getString("product")).execute();
            }
        }
        catch (JSONException ex) {
            Log.d("Cafe", "Exception in removeOrder: " + ex.getMessage());
        }

        new DeleteOrder(toBeDeletedItem).execute();

        /*private class RemoveFromItemTable extends AsyncTask<String, Void, String> {
            private String uri;
            private String toSend;

            RemoveFromItemTable(String toSend) {
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
        }*/




    }

    public void addCustomerInfo(View view) {
        EditText userText = (EditText) findViewById(R.id.name);
        EditText userPhoneNumber = (EditText) findViewById(R.id.phone_number);

        /** You had
        new PutToInvoice("{\"customer\":" + userText + ",\"phone\":" + userPhoneNumber + "}").execute();
         The JSON expression is missing an idorder property.
         You were missing quote marks around the customer name and the phone number.
         Also you were missing a couple of calls to getText(). **/
        String json = "{\"idorder\":"+orderNumber+",\"customer\":\"" + userText.getText() + "\",\"phone\":\"" + userPhoneNumber.getText() + "\"}";
        new PutToInvoice(json).execute();
    }

    private class PutToInvoice extends AsyncTask<String, Void, String> {
        private String uri;
        private String toSend;

        PutToInvoice(String toSend) {
            uri = "http://" + URIHandler.hostName + "/RESTCafeApplication/api/invoice/" + orderNumber;
            this.toSend = toSend;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return URIHandler.doPut(uri, toSend);
            } catch (IOException e) {
                return "";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String orderId) {
            // After posting the message, we will want to post the individual recipients.
            //Log.d("Cafe", "Reaching");
            /*if (orderNumber == null) {
                orderNumber = orderId;
            }
            int orderNumberInt = Integer.valueOf(orderNumber);
            new AddOrderTaskToItem("{\"product\":" + selected_product + ", \"ordernumber\":" + orderNumberInt + "}").execute();*/
            //addOrder(orderId); //-----> CHANGE THIS
        }
    }
}
