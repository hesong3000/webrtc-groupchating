package android.webrtc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webrtc.avgroupchatproxy.AVGC;
import android.widget.Button;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private Button result_ok;
    private Button result_cancel;
    private Button back_press;

    @Override
    public void onBackPressed() {
        Log.d("InitialActivity","ResultActivity onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        findViews();
    }

    private void findViews(){
        result_ok = (Button)findViewById(R.id.result_ok);
        result_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                List<String> resultList = new LinkedList<String>();
                resultList.add("wkwkwk");
                resultList.add("hello");
                resultList.add("listView");
                intent.putExtra(AVGC.startActivity_Result_OK, (Serializable)resultList);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        result_cancel = (Button)findViewById(R.id.result_cancel);
        result_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra(AVGC.startActivity_Result_CANCEL,"用户取消");
                setResult(RESULT_CANCELED,intent);
                finish();
            }
        });

        back_press = (Button)findViewById(R.id.back_press);
        back_press.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
