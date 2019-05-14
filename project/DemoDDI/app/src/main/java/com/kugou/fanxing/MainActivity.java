package com.kugou.fanxing;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import cn.shuzilm.core.Listener;
import cn.shuzilm.core.Main;

public class MainActivity extends AppCompatActivity {

    private TextView txtView;
    private Button btnInit;
    private Button btnGetId;
    private StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = (TextView) findViewById(R.id.txt_did);
        btnInit = (Button) findViewById(R.id.btn_init);
        btnGetId = (Button) findViewById(R.id.btn_get_id);
        btnGetId.setEnabled(false);

        btnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDDI(MainActivity.this);
            }
        });

        btnGetId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDID(MainActivity.this);
            }
        });

        String arch = System.getProperty("os.arch");
        long st = System.nanoTime() / 1000000;
        sb.append(st).append(": CPU ABI ").append(arch).append("\n");
        sb.append(st).append(": 等待触发初始化...");
        txtView .setText(sb.toString());

    }

    private void initDDI(Context ctx) {
        btnInit.setEnabled(false);
        try {
            long st = System.nanoTime() / 1000000;
            JSONObject obj = new JSONObject(loadJSONFromAsset(ctx, "cn.shuzilm.config.json"));

            String store = obj.getString("store");
            String apiKey = obj.getString("apiKey");
            sb.append("\n").append(st).append(": store=").append(store).append("\n").append(st).append(": apiKey=").append(apiKey);
            txtView.setText(sb.toString());

            st = System.nanoTime() / 1000000;
            Main.init(ctx,  apiKey);
            long new_st = System.nanoTime() / 1000000;

            sb.append("\n").append(new_st).append(": ").append("初始化成功，耗时").append(new_st - st).append("ms");
            txtView .setText(sb.toString());

            btnGetId.setEnabled(true);
        } catch (JSONException e) {
            e.printStackTrace();
            sb.append("\n异常 ").append(e.toString());
            txtView .setText(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            sb.append("\n异常 ").append(e.toString());
            txtView .setText(sb.toString());
        }
    }

    private void getDID(Context ctx) {
        btnGetId.setEnabled(false);
        Main.getQueryID(ctx, "channel", "message", 1, new Listener() {
                    @Override public void handler(String s) {
                        final long st = System.nanoTime() / 1000000;
                        final String sId = s;
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(MainActivity.class.getName(), "query id = " + sId);
                                sb.append("\n").append(st).append(": query id = ").append(sId);
                                txtView .setText(sb.toString());
                                btnGetId.setEnabled(true);
                            }
                        });
                    }
        });
    }

    public String loadJSONFromAsset(Context ctx, String jsonFileName) {
        String json = null;
        try {
            InputStream is = ctx.getAssets().open(jsonFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
