package com.jkm.harukatest;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final double GANCIT_LAT = -6.243376;
    private static final double GANCIT_LNG = 106.784425;

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_DISTANCE = "distance";

    private GoogleMap mMap;
    float zoomLevel = (float) 16.0;

    Marker gancitMarker;
    Marker studentMarker;
    LatLng gancitLatLng;
    LatLng studentLatLng;
    Polyline polyline;
    boolean isViewingMap = false;

    List<Student> studentList;
    ArrayList<HashMap<String, String>> studentsInfo = new ArrayList<>();
    ListView lvStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvStudents = (ListView) findViewById(R.id.list_view_students);

        setUpMap();
        gancitLatLng = new LatLng(GANCIT_LAT, GANCIT_LNG);

        DbHandler dbHandler = new DbHandler(this);
        studentList = dbHandler.getStudentList();

        lvStudents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lvStudents.setVisibility(View.GONE);
                isViewingMap = true;
                invalidateOptionsMenu();

                studentLatLng = new LatLng(Double.valueOf(studentsInfo.get(position).get(KEY_LAT)),
                        Double.valueOf(studentsInfo.get(position).get(KEY_LNG)));

                if (studentMarker != null) studentMarker.remove();
                studentMarker = mMap.addMarker(new MarkerOptions()
                        .position(studentLatLng)
                        .title(studentsInfo.get(position).get(KEY_NAME) + ", " + studentsInfo.get(position).get(KEY_DISTANCE))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.student_icon)));

                if (polyline != null) polyline.remove();
                polyline = mMap.addPolyline(new PolylineOptions()
                        .add(gancitLatLng, studentLatLng)
                        .width(7)
                        .color(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));

                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boundsBuilder.include(gancitMarker.getPosition());
                boundsBuilder.include(studentMarker.getPosition());
                LatLngBounds bounds = boundsBuilder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (Student student : studentList) {
            double distance = getDistance(gancitLatLng,
                    new LatLng(Double.valueOf(student.getLatitude()), Double.valueOf(student.getLongitude()))) / 1000.0;
            distance = Math.round(distance * 100) / 100.0d;
            Log.d(TAG, "distance: " + distance);

            if (distance < 14.00) {
                HashMap<String, String> studentMap = new HashMap<>();
                studentMap.put(KEY_ID, String.valueOf(student.getID()));
                studentMap.put(KEY_NAME, student.getName());
                studentMap.put(KEY_LAT, student.getLatitude());
                studentMap.put(KEY_LNG, student.getLongitude());
                studentMap.put(KEY_DISTANCE, String.valueOf(distance) + " km");
                studentsInfo.add(studentMap);
            }
        }

        Log.d(TAG, "size: " + studentsInfo.size());

        ListAdapter adapter = new SimpleAdapter(
                MainActivity.this, studentsInfo, R.layout.student_list,
                new String[]{KEY_ID, KEY_NAME, KEY_DISTANCE},
                new int[]{R.id.text_view_id, R.id.text_view_name, R.id.text_view_distance});
        lvStudents.setDivider(null);
        lvStudents.setAdapter(adapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "Map is ready.");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (mMap != null) {
            gancitMarker = mMap.addMarker(new MarkerOptions().position(gancitLatLng).title("Gandaria City")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.main_icon)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gancitLatLng, zoomLevel));
        }
    }

    private void setUpMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private float getDistance(LatLng begin, LatLng end) {
        final int earthRadius = 6371000;
        double thetaA = Math.toRadians(begin.latitude);
        double lambdaA = Math.toRadians(begin.longitude);
        double thetaB = Math.toRadians(end.latitude);
        double lambdaB = Math.toRadians(end.longitude);
        double dTheta = thetaB - thetaA;
        double dLambda = lambdaB - lambdaA;

        double a = Math.sin(dTheta / 2) * Math.sin(dTheta / 2) +
                Math.cos(thetaA) * Math.cos(thetaB) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        return (float) (2 * earthRadius * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (isViewingMap) {
            menu.findItem(R.id.list).setVisible(true);
        } else {
            menu.findItem(R.id.list).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.list) {
            lvStudents.setVisibility(View.VISIBLE);
            isViewingMap = false;
            invalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
