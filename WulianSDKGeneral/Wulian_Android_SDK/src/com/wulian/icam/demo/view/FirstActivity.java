package com.wulian.icam.demo.view;

import com.wulian.icam.demo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FirstActivity extends Activity {

	private Button btn_first;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first);
		btn_first = (Button) findViewById(R.id.btn_first);
		btn_first.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					Intent intent = new Intent(FirstActivity.this, MainActivity.class);
					startActivity(intent);
			}
		});
	}
	
}
