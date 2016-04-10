package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;


import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUitl;
import com.coolweather.app.util.Utility;



import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String > adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String >();
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	private Province selectedProvince;
	private City selectedCity;
	
	private int currentLevel;
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		titleText=(TextView)findViewById(R.id.title_text);
		listView=(ListView)findViewById(R.id.list_view);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		
		
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) 
			{
					if (currentLevel == LEVEL_PROVINCE) 
					{
						selectedProvince = provinceList.get(index);
						queryCities();
					} 
					else if (currentLevel == LEVEL_CITY) 
					{
						selectedCity = cityList.get(index);
						queryCounties();
					}
			}
		}
				);
		queryProvinces();//程序第一次运行一定要调用一次
		
		
		
	}
	/**
	* 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
	*/
	private void queryProvinces() 
	{
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) 
		{
			dataList.clear();
			for (Province province : provinceList) 
			{
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else 
		{
			queryFromServer(null, "province");//从服务器获取数据
		}
	}
	
	/**
	* 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
	*/
	private void queryCities() 
	{
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) 
		{
			dataList.clear();
			for (City city : cityList) 
			{
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else
		{
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	* 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
	*/
	private void queryCounties()
	{
		//我认为这里添加一句countyList清零的语句更好。
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) 
		{
			dataList.clear();
			for (County county : countyList) 
			{
			dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} 
		else 
		{
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/**
	* 根据传入的代号和类型从服务器上查询省市县数据。
	*/
	private void queryFromServer(final String code, final String type) 
	{
		String address;
		if (!TextUtils.isEmpty(code)) 
		{
			address = "http://www.weather.com.cn/data/list3/city" + code +
			".xml";
		} 
		else 
		{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();//一旦发送了请求，就需要显示提示框
		HttpUitl.sendHttpRequest(address, new HttpCallbackListener() 
		{
		public void onFinish(String response) 
		{
			boolean result = false;
			if ("province".equals(type)) 
			{
				result = Utility.handleProvincesResponse(coolWeatherDB,
				response);
			}
			else if ("city".equals(type)) 
			{
				result = Utility.handleCitiesResponse(coolWeatherDB,
				response, selectedProvince.getId());
			} 
			else if ("county".equals(type)) 
			{
				result = Utility.handleCountiesResponse(coolWeatherDB,
				response, selectedCity.getId());
			}
			if (result)
			{
			// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() 
				{
					public void run() 
					{
						closeProgressDialog();//
						if ("province".equals(type)) 
						{
							queryProvinces();//都涉及到UI操作。
						} 
						else if ("city".equals(type)) 
						{
							queryCities();
						} 
						else if ("county".equals(type)) 
						{
							queryCounties();
						}
			        }
				});
			}
			
		}
		
		public void onError(Exception e) 
		{
			
			runOnUiThread(new Runnable() 
			{
				public void run() 
				{
					closeProgressDialog();
					Toast.makeText(ChooseAreaActivity.this,"加载失败", Toast.LENGTH_SHORT).show();
				}
			});
		}
			});
	}
	
	/**
	* 显示进度对话框
	*/
	private void showProgressDialog() 
	{
		if (progressDialog == null) 
		{
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);//不可通过点击空余地方取消。
		}
		progressDialog.show();
	}
	/**
	* 关闭进度对话框
	*/
	private void closeProgressDialog() 
	{
		if (progressDialog != null) 
		{
			progressDialog.dismiss();
		}
	}
	/**
	* 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出。
	*/
	@Override
	public void onBackPressed() 
	{
		if (currentLevel == LEVEL_COUNTY) 
		{
			queryCities();
		} 
		else if (currentLevel == LEVEL_CITY) 
		{
			queryProvinces();
		}
		else //不为以上两种的话，也就是我们所认为的主页，自然可以退出。
		{
			finish();
		}
	}
	

}
