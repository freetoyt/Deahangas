package com.gms.app.barcode;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NoGasDialog {

    private Context context;
    String[] items;

    ArrayList<String> listItems;
    ListView listView ;
    ArrayAdapter adapter3 ;
    SharedPreferences sharedPreferences ;
    private String shared = "file";
    boolean isUpdate = true;
    private Spinner spinner;
    List<ProductPriceSimpleVO> productList = new ArrayList<>();

    String productType = "";
    String buttonType = "";
    String customerId="";
    String userId = "";
    String host ="";
    String value ="" ;

    public NoGasDialog(Context context, String bType) {
        this.context = context;
        this.buttonType = bType;

        sharedPreferences = context.getSharedPreferences(shared, 0);
        host = context.getString(R.string.host_name);

        value = sharedPreferences.getString("clist", "");
        //Log.e("noGasDialog ",buttonType);
        if(value ==null || value.length() <= 10)
            new HttpAsyncTask().execute(host + "api/customerAllList.do");

        new HttpAsyncTask2().execute(host + "api/ngasProductPriceList.do");
        //new HttpAsyncTask().execute("http://172.30.57.228:8080/api/carList.do");
    }

    // ????????? ??????????????? ????????? ????????????.
    public void callFunction( String id ){

        userId = id;

        // ????????? ?????????????????? ?????????????????? Dialog???????????? ????????????.
        final Dialog dlg = new Dialog(context);

        // ??????????????? ??????????????? ?????????.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // ????????? ?????????????????? ??????????????? ????????????.
        dlg.setContentView(R.layout.nogas_dialog);

        // ????????? ?????????????????? ????????????.
        dlg.show();

        // ????????? ?????????????????? ??? ???????????? ????????????.
        final TextView title = (TextView) dlg.findViewById(R.id.title);
        final EditText message = (EditText) dlg.findViewById(R.id.mesgase);
        final Button okButton = (Button) dlg.findViewById(R.id.okButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);
        final EditText productCount = (EditText) dlg.findViewById(R.id.productCount);

        title.setText(buttonType);
        // Add Data to listView
        listView = (ListView) dlg.findViewById(R.id.listview);
        spinner = (Spinner)dlg.findViewById(R.id.spinner);

        //value = sharedPreferences.getString("clist", "");
        //Log.d("noGasDialog  value ", value);
        items = value.split(",");

        listItems = new ArrayList<>(Arrays.asList(items));
        adapter3 = new ArrayAdapter(context, R.layout.item_customer, R.id.tv_customer, listItems);
        listView.setAdapter(adapter3);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, "click item", Toast.LENGTH_SHORT).show();
                String text = (String)parent.getAdapter().getItem(position);
                message.setText(text);
            }
        });


        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                productType = parent.getItemAtPosition(position).toString();
                //tv_result.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(message.getText().toString().length() <=0){
                    Toast.makeText(context, "???????????? ???????????????", Toast.LENGTH_SHORT).show();
                }else {

                    Toast.makeText(context, String.format("\"%s??? %s??? %s\"???????????????.", message.getText().toString(), productType, buttonType), Toast.LENGTH_SHORT).show();

                    customerId = message.getText().toString();
                    Integer productId = 0;
                    Integer productPriceSeq = 0;
                    int iProductCount = 0 ;
                    iProductCount = Integer.parseInt(productCount.getText().toString());

                    for(int i =0 ; i< productList.size() ; i++){
                        if(productType.equals(productList.get(i).getProductNm()) ) {
                            productId = productList.get(i).getProductId();
                            productPriceSeq = productList.get(i).getProductPriceSeq();
                            //Toast.makeText(context, productType+"="+productList.get(i).getProductId()+"="+productList.get(i).getProductPriceSeq() ,Toast.LENGTH_SHORT).show();
                        }
                    }
                    // ?????? ??????
                    new HttpAsyncTask1().execute(host + "api/controlActionNoGas.do?userId=" + userId + "&customerNm=" + customerId + "&productId=" + productId + "&productPriceSeq=" + productPriceSeq+ "&productCount=" + iProductCount);

                    //MainActivity List ??????
                    MainActivity.clearArrayList();

                    // ????????? ?????????????????? ????????????.
                    dlg.dismiss();

                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "?????? ????????????.", Toast.LENGTH_SHORT).show();

                // ????????? ?????????????????? ????????????.
                dlg.dismiss();
            }
        });
    }

    // ????????? ???????????? ?????????
    public void search(String charText) {
        Log.d("search","start =="+charText);
        // ?????? ??????????????? ???????????? ????????? ?????? ????????????.
        listItems.clear();

        // ?????? ????????? ???????????? ?????? ???????????? ????????????.
        if (charText.length() == 0) {
            listItems = new ArrayList<>(Arrays.asList(items));        }
        // ?????? ????????? ??????..
        else
        {
            // ???????????? ?????? ???????????? ????????????.
            int j=0;
            for(int i = 0;i < items.length; i++)
            {
                // arraylist??? ?????? ???????????? ???????????? ??????(charText)??? ???????????? ????????? true??? ????????????.
                //Log.d("search",charText);
                if (items[i].toLowerCase().contains(charText.toLowerCase()))
                {
                    // ????????? ???????????? ???????????? ????????????.
                    listItems.add(items[i]);
                }
            }
        }
        // ????????? ???????????? ????????????????????? ???????????? ???????????? ????????? ???????????? ????????? ????????????.
        adapter3.notifyDataSetChanged();
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, List<CustomerSimpleVO>> {
        private final String TAG = HttpAsyncTask.class.getSimpleName();
        // int REQUEST_CODE =
        // OkHttp ???????????????
        OkHttpClient client = new OkHttpClient();

        @Override
        protected List<CustomerSimpleVO> doInBackground(String... params) {
            List<CustomerSimpleVO> customerList = new ArrayList<>();
            String strUrl = params[0];
            try {
                // ??????
                Request request = new Request.Builder()
                        .url(strUrl)
                        .build();
                // ??????
                Response response = client.newCall(request).execute();

                Gson gson = new Gson();

                // import java.lang.reflect.Type
                Type listType = new TypeToken<ArrayList<CustomerSimpleVO>>() {
                }.getType();
                customerList = gson.fromJson(response.body().string(), listType);

                //Log.d(TAG, "onCreate: " + customerList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return customerList;
        }

        @Override
        protected void onPostExecute(List<CustomerSimpleVO> customerList) {
            super.onPostExecute(customerList);

            //Log.d("HttpAsyncTask", customerList.toString());
            //CustomerSimpleAdapter adapter = new CustomerSimpleAdapter(customerList);
            StringBuffer sb = new StringBuffer();
            items = new String[customerList.size()];
            for (int i = 0; i < customerList.size(); i++) {
                items[i] = customerList.get(i).getCustomerNm().toString();
                sb.append(customerList.get(i).getCustomerNm().toString());
                sb.append(",");
            }

            if((buttonType.equals("??????") || buttonType.equals("??????") || buttonType.equals("??????"))) {
                int cCount = sharedPreferences.getInt("clistCount", 0);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(cCount > 0 || cCount == customerList.size()) isUpdate = false;
                else isUpdate = true;
                //String value = id.getText().toString();
                editor.putString("clist", sb.toString());
                editor.putInt("clistCount",customerList.size());
                editor.commit();

            }
            if(isUpdate) {
                Log.d("isUpdate ture", "ture ");
                listItems = new ArrayList<>(Arrays.asList(items));
                adapter3 = new ArrayAdapter(context, R.layout.item_customer, R.id.tv_customer, listItems);
                listView.setAdapter(adapter3);
            }
        }
    }

    private class HttpAsyncTask1 extends AsyncTask<String, Void, String> {

        private final String TAG = HttpAsyncTask1.class.getSimpleName();
        // int REQUEST_CODE =
        // OkHttp ???????????????
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {
            //List<CustomerSimpleVO> customerList = new ArrayList<>();
            String strUrl = params[0];
            String result= "";
            try {
                // ??????
                Request request = new Request.Builder()
                        .url(strUrl)
                        .build();
                // ??????
                Response response = client.newCall(request).execute();
                result = response.body().string();
                //Log.d(TAG, "response.body().string(): " + result);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }
    }

    // ???????????? ????????????
    private class HttpAsyncTask2 extends AsyncTask<String, Void, List<ProductPriceSimpleVO>> {
        private final String TAG = HttpAsyncTask.class.getSimpleName();
        // int REQUEST_CODE =
        // OkHttp ???????????????
        OkHttpClient client = new OkHttpClient();

        @Override
        protected List<ProductPriceSimpleVO> doInBackground(String... params) {

            String strUrl = params[0];
            try {
                // ??????
                Request request = new Request.Builder()
                        .url(strUrl)
                        .build();
                // ??????
                Response response = client.newCall(request).execute();

                Gson gson = new Gson();

                // import java.lang.reflect.Type
                Type listType = new TypeToken<ArrayList<ProductPriceSimpleVO>>() {
                }.getType();
                productList = gson.fromJson(response.body().string(), listType);

                //Log.d(TAG, "onCreate: " + productList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return productList;
        }

        @Override
        protected void onPostExecute(List<ProductPriceSimpleVO> productList) {
            super.onPostExecute(productList);

            //Log.d("HttpAsyncTask2", productList.toString());
            List<String> spinnerArray =  new ArrayList<String>();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < productList.size(); i++) {
                spinnerArray.add(productList.get(i).getProductNm().toString());

                //sb.append(productList.get(i).getProductNm()).toString());
                //sb.append(",");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //Spinner sItems = (Spinner) findViewById(R.id.spinner1);
            spinner.setAdapter(adapter);
        }
    }

}
