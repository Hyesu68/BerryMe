package com.susuryo.berryme

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.susuryo.berryme.databinding.ActivityLocationBinding

//import kotlinx.android.synthetic.main.activity_location.*
//import kotlinx.android.synthetic.main.activity_setting.back_button

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityLocationBinding
//    private lateinit var mapView: MapView
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    private lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10
    val apiKey = BuildConfig.googlemap_api_key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.fromright, R.anim.none)

//        mapView = findViewById(R.id.locationactivity_map)
        binding.locationactivityMap.onCreate(savedInstanceState)
        binding.locationactivityMap.getMapAsync(this)

        mLocationRequest =  LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (checkPermissionForLocation(applicationContext)) {
            startLocationUpdates()
        }

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }


        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.none, R.anim.horizon_exit)
    }

    private fun setLaunchActivityClickListener(
        onClickResId: Int, activityClassToLaunch: Class<out AppCompatActivity?>
    ) {
        findViewById<View>(onClickResId)
            .setOnClickListener { v: View? ->
                val intent = Intent(this@LocationActivity, activityClassToLaunch)
                startActivity(intent)
            }
    }

    override fun onStart() {
        super.onStart()
        binding.locationactivityMap.onStart()
    }

    override fun onStop() {
        binding.locationactivityMap.onStop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        binding.locationactivityMap.onResume()
    }

    lateinit var googleMap: GoogleMap
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
//        val seoul = LatLng(lat, lng)
//        p0.moveCamera(CameraUpdateFactory.newLatLng(seoul))
//        p0.animateCamera(CameraUpdateFactory.zoomTo(13F))
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        p0.isMyLocationEnabled = true
    }

    private fun startLocationUpdates() {
        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청
        mFusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()!!
        )
    }

    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
//            locationResult.lastLocation
            locationResult.lastLocation?.let { onLocationChanged(it) }
        }
    }

    var lat = 37.54754
    var lng = 126.987564
    // 시스템으로 부터 받은 위치정보를 화면에 갱신해주는 메소드
    fun onLocationChanged(location: Location) {
        Log.d(
            "susuryodebug",
            "onLocationChanged location : " + location.latitude + " " + location.longitude
        )
        mLastLocation = location
        lat = mLastLocation.latitude // 갱신 된 위도
        lng = mLastLocation.longitude // 갱신 된 경도
        val point = LatLng(location.latitude, location.longitude)
        val cameraPosition = CameraPosition(
            point,
            18F,
            googleMap.cameraPosition.tilt,
            googleMap.cameraPosition.bearing
        )
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13F))
        binding.locationactivityProgressbar.visibility = View.GONE
        binding.locationactivityMapIcon.visibility = View.VISIBLE
    }

    // 위치 권한이 있는지 확인하는 메서드
    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
                false
            }
        } else {
            true
        }
    }

    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d("ttt", "onRequestPermissionsResult() _ 권한 허용 거부")
                Toast.makeText(applicationContext, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}