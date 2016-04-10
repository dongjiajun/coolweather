package com.coolweather.app.util;

import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class Utility {

	/**
	* 解析和处理服务器返回的省级数据
	*/
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB
			coolWeatherDB, String response) 
	{
			if (!TextUtils.isEmpty(response)) 
			{
				//textiutil这种方式能一下判断两种是否为空的条件
				String[] allProvinces = response.split(",");
				if (allProvinces != null && allProvinces.length > 0) 
				{
					for (String p : allProvinces) 
					{
						String[] array = p.split("\\|");
						Province province = new Province();
						province.setProvinceCode(array[0]);
						province.setProvinceName(array[1]);
						// 将解析出来的数据存储到Province表
						coolWeatherDB.saveProvince(province);
					}
					return true;
				}
			}
			return false;
	}
	
	/**
	* 解析和处理服务器返回的市级数据
	*/
	public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
	String response, int provinceId) 
	{
		if (!TextUtils.isEmpty(response)) //同时判断两种方式是否为空
		{
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) 
			{
				for (String c : allCities) 
				{
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					// 将解析出来的数据存储到City表
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	* 解析和处理服务器返回的县级数据
	*/
	public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,
	String response, int cityId) 
	{
		if (!TextUtils.isEmpty(response)) 
		{
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) 
			{
				for (String c : allCounties) 
				{
					String[] array = c.split("\\|");//这里用了转义字符
					
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					// 将解析出来的数据存储到County表
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}

}
