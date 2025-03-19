package com.seciron.ironsky.ironsky_integration

import android.Manifest
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import com.appsec.everisk.core.CallBack
import com.appsec.everisk.core.RiskStubAPI
import com.appsec.everisk.core.Type
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject


class MainActivity : FlutterActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
	super.onCreate(savedInstanceState)

	initPermissions()
	registerSecurityEventCallback()
	setupMethodChannel()
	initializeRiskStubAPI()
  }


  private fun initPermissions() {
	val reqPermissions = arrayOf(
	  Manifest.permission.ACCESS_FINE_LOCATION,
	  Manifest.permission.ACCESS_COARSE_LOCATION,
	  Manifest.permission.READ_PHONE_STATE,
	  Manifest.permission.ACCESS_NETWORK_STATE,
	  Manifest.permission.ACCESS_WIFI_STATE
	)

	RiskStubAPI.initAppsecPermission(this, reqPermissions)
  }


  private fun setupMethodChannel() {
	val channel = "com.example.myapp/channel"  // Ganti dengan nama channel yang sesuai

	flutterEngine?.dartExecutor?.binaryMessenger?.let {
	  MethodChannel(it, channel).setMethodCallHandler { call, result ->
		when (call.method) {
		  "setIronSkyUserID" -> {
			val userID = call.argument<String>("userID")
			if (userID != null) {
			  val nativeResult = setUserID(userID)
			  if (nativeResult != null) {
				result.success(nativeResult)
			  } else {
				result.error("UNAVAILABLE", "Native function not available.", null)
			  }
			} else {
			  result.error("INVALID_ARGUMENT", "Argument 'userID' is required.", null)
			}
		  }

		  else -> result.notImplemented()
		}
	  }
	}
  }

  private fun setUserID(userID: String): String? {
	return try {
	  RiskStubAPI.setUserId(this, userID)
	  "Success"

	} catch (e: Exception) {
	  Log.e("nativeFunction", "Error setting user ID", e)
	  null
	}
  }


  private fun registerSecurityEventCallback() {
	RiskStubAPI.initEventResponse(this)

	RiskStubAPI.registerService(object : CallBack {
	  override fun onResult(type: Type, result: Any) {
		val jsonObject = result as JSONObject
		val responseI18n = jsonObject.optString("responseI18n")
		val key = jsonObject.optString("key")

		Log.d("responseI18n", responseI18n)
		Log.d("ruleengine", "onReceive:$key")


		val responseType = jsonObject.optInt("responseType", -1)


		// Lakukan Handling Response di sini
		// Ex: Melakukan pengiriman data ke REST API Lainnya.

	  }
	}, Type.RISKEVENT, 0.5)
  }



  private fun initializeRiskStubAPI() {
	val key = "+a88XBOU/bvfk6al7Sliem8RKQWb/N5F+LkjPlX44KxGiVjJTm/QZ1f1Pgawoioacgpk8LKCyN/qlcWti5DRm8PilTe/J/hOWKg8kJ6MR2KCbW++kI2ecX+SThK0OztOQMs2U5cjWUM4s/pavLRTh1TP4wmFfSnDgtC03jxbGUPpPeP8vc1FZXyRqNt4AI6U7qgC0iMZtkFy8ZS4s6XvDW7UrIjGyecb8pRBv8xFyCuSgdvWQ6FrCH3un8A9ASWL"
	val isInit = RiskStubAPI.initAppsecEverisk(this, key)
	Log.d("Status", "is init everisk: $isInit")
  }



  private fun registerEventReceiver() {
	val intentFilter = IntentFilter().apply {
	  addAction("SecEventDetected")
	}
  }


}

