package com.example.dummycalc;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private String _operator = "";
	private String _operand1 = "";
	private String _operand2 = "";
	private String _result = "";
	
	private float doMath(final String op1, final String op2, final String operator)
	{
		float f1 = Float.parseFloat( op1 );
		float f2 = Float.parseFloat( op2 );
		float res = 0;

		if (operator.equals("+"))
		{
			res = f1 + f2;
		}
		else if (_operator.equals("-"))
		{
			res = f1 - f2;
		}
		else if (_operator.equals("/"))
		{
			res = f1 / f2;
		}
		else if (_operator.equals("*"))
		{
			res = f1 * f2;
		}
		return res;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final TextView tv = (TextView)findViewById( R.id.lblScreen);
		
		final OnClickListener oclPrint = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Button b = (Button) v;
				String c = b.getText().toString();
				char ch = c.charAt(0); 
				
				if (ch >= '0' && ch <= '9' || c.equals("."))
				{
					if (_result.length() > 0)
					{
						_result = "";
						_operator = "";
						_operand1 = "";
						_operand2 = "";
					}
					if (_operator.equals(""))
					{
						if (!c.equals(".") || (c.equals(".") && !_operand1.contains(".")))
						{
							_operand1 += c;
						}
					}
					else
					{
						if (!c.equals(".") || (c.equals(".") && !_operand2.contains(".")))
						{
							_operand2 += c;
						}
					}
				}
				else if (c.equals("+") || c.equals("-") || c.equals("/") || c.equals("*"))
				{
					_result = "";
					if ( _operand1.length()!=0 && _operand2.length()!=0)
					{
						float res = doMath(_operand1, _operand2, _operator);
						
						_operator = c;
						
						_operand1 = String.valueOf(res);
						if (_operand1.endsWith(".0"))
						{
							_operand1 = _operand1.substring(0, _operand1.length()-2);
						}

						_operand2 = "";
					}
					else if (_operand1.length()!=0)
					{
						_operator = c;
					}
				}
				else if (c.equals("="))
				{
					if (_result.length() > 0)
					{
						_operand1 = _result;
					}
					if ( _operand1.length()!=0 && _operand2.length()!=0)
					{
						float res = doMath(_operand1, _operand2, _operator);
						
						_result = String.valueOf(res);
						if (_result.endsWith(".0"))
						{
							_result = _result.substring(0, _result.length()-2);
						}
					}
				}
				else if (c.equals("+/-"))
				{
					if (_result.length() > 0)
					{ }
					else if ( _operand1.length()!=0 && _operand2.length()!=0)
					{
						_operand2 = String.valueOf( Float.parseFloat(_operand2) * -1 );
						if (_operand2.endsWith(".0"))
						{
							_operand2 = _operand2.substring(0, _operand2.length()-2);
						}						
					}
					else
					{
						_operand1 = String.valueOf( Float.parseFloat(_operand1) * -1 );
						if (_operand1.endsWith(".0"))
						{
							_operand1 = _operand1.substring(0, _operand1.length()-2);
						}
					}
				}
				else if (c.equals("<-"))
				{
					if (_result.length() > 0)
					{ 
						_operand1 = _result;
						_operand2 = "";
						_operator = "";
						_result = "";
					}
					else if ( _operand1.length() > 0 && _operand2.length() >0)
					{
						_operand2 = _operand2.substring(0, _operand2.length()-1);
						if (_operand2.endsWith( "."))
							_operand2 = _operand2.substring(0, _operand2.length()-1);
					}
					else if ( _operand1.length() > 0 && _operator.length() > 0)
					{
						_operator = "";
					}
					else if ( _operand1.length() > 0)
					{
						_operand1 = _operand1.substring(0, _operand1.length()-1);
						if (_operand1.endsWith( "."))
							_operand1 = _operand1.substring(0, _operand1.length()-1);

					}
				}
				
				StringBuilder tag = new StringBuilder();
				tag.append(_operand1);
				tag.append(_operator);
				tag.append(_operand2);
				if (_result.length() > 0)
				{
					tag.append("\n=");
					tag.append(_result);
				}
				tv.setText(tag);
			}
		};
		
		final OnClickListener oclClear = new OnClickListener() {
			@Override
			public void onClick(View v) {
				tv.setText("");
				_operand1 = "";
				_operand2 = "";
				_operator = "";
			}
		};

		final OnClickListener oclBracket = new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		};
		
		
		setButtonListener(R.id.btn0, oclPrint);
		setButtonListener(R.id.btn1, oclPrint);
		setButtonListener(R.id.btn2, oclPrint);
		setButtonListener(R.id.btn3, oclPrint);
		setButtonListener(R.id.btn4, oclPrint);
		setButtonListener(R.id.btn5, oclPrint);
		setButtonListener(R.id.btn6, oclPrint);
		setButtonListener(R.id.btn7, oclPrint);
		setButtonListener(R.id.btn8, oclPrint);
		setButtonListener(R.id.btn9, oclPrint);
		
		setButtonListener(R.id.btnPlus, oclPrint);
		setButtonListener(R.id.btnMin, oclPrint);
		setButtonListener(R.id.btnDiv, oclPrint);
		setButtonListener(R.id.btnMul, oclPrint);
		setButtonListener(R.id.btnEqual, oclPrint);
		
		setButtonListener(R.id.btnDot, oclPrint);
		setButtonListener(R.id.btnPosNeg, oclPrint);
		setButtonListener(R.id.btnCanc, oclPrint);
		
		setButtonListener(R.id.btnC, oclClear);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void setButtonListener(int id, OnClickListener listener)
	{
		Button btn = (Button) findViewById(id);
		btn.setOnClickListener(listener);
	}

}
