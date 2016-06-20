package wigzo.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import wigzo.sdk.WigzoSDK;

public class DirectLinkActivity extends AppCompatActivity {
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_link);

        WigzoSDK sdk = WigzoSDK.getInstance();
        sdk.onStart();
        sdk.initializeWigzoData(this, "2c271df3-713f-4802-ae4c-b0dec708c988", "1080912767729");

        extras = getIntent().getExtras();
        TextView nameView = (TextView) findViewById(R.id.direct_data_view);
        nameView.setText((String )extras.get("name") + " & " + (String )extras.get("age"));

    }
}
