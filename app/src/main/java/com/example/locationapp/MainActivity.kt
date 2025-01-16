package com.example.locationapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View // Import để xử lý ẩn/hiện View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.random.Random

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var tvConnection: TextView
    private lateinit var btnConnect: Button
    private lateinit var btnSearch: Button
    private lateinit var etInputCode: EditText

    private var generatedCode: String? = null // Mã xe ngẫu nhiên
    private var vehicleLocation: LatLng? = null // Vị trí của người tạo mã

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ánh xạ các thành phần giao diện
        tvConnection = findViewById(R.id.tvConnection)
        btnConnect = findViewById(R.id.btnConnect)
        etInputCode = findViewById(R.id.etInputCode)
        btnSearch = findViewById(R.id.btnSearch)
        val btnCreateCode: Button = findViewById(R.id.btnCreateCode)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Xử lý nút "Tạo mã xe"
        btnCreateCode.setOnClickListener {
            val generatedCode = generateRandomCode()
            etInputCode.setText(generatedCode) // Hiển thị mã xe vào EditText
            btnSearch.visibility = View.GONE // Ẩn nút tìm kiếm sau khi tạo mã
            this.generatedCode = generatedCode // Lưu mã xe được tạo
            Toast.makeText(this, "Mã xe đã được tạo: $generatedCode", Toast.LENGTH_SHORT).show()
        }

        // Xử lý nút "Tìm kiếm"
        btnSearch.setOnClickListener {
            val inputCode = etInputCode.text.toString()
            if (inputCode.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã xe!", Toast.LENGTH_SHORT).show()
            } else if (inputCode == generatedCode) {
                // Nếu mã đúng, lấy vị trí và di chuyển bản đồ đến đó
                if (vehicleLocation != null) {
                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(vehicleLocation!!).title("Vị trí xe"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vehicleLocation!!, 15f))
                    Toast.makeText(this, "Mã xe đúng, vị trí được xác định!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Vị trí người tạo mã chưa được xác định.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Mã xe không đúng!", Toast.LENGTH_SHORT).show()
            }
        }

        // Xử lý nút "Kết nối" để lấy vị trí thực tế
        btnConnect.setOnClickListener {
            getCurrentLocation()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    tvConnection.text = "Accessed"
                    btnConnect.text = "Connected"

                    val latLng = LatLng(location.latitude, location.longitude)
                    vehicleLocation = latLng // Lưu vị trí người tạo mã
                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(latLng).title("Vị trí xe"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                    Toast.makeText(this, "Vị trí của người tạo mã đã được xác định!", Toast.LENGTH_SHORT).show()
                } else {
                    tvConnection.text = "Unable to get location"
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    @SuppressLint("SetTextI18n")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                tvConnection.text = "Permission denied"
            }
        }

    // Hàm tạo mã xe ngẫu nhiên
    private fun generateRandomCode(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { Random.nextInt(0, characters.length) }
            .map(characters::get)
            .joinToString("")
    }
}
