package com.example.openstreetmap2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import static android.os.Build.VERSION_CODES.M;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberto on 2024.
 */
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSAO_REQUERIDA = 1;


    private Polyline roadOverlay;
    private Road road;
    private MapView mapView;
    private IMapController mapController;

    //Cria um ponto de referência com base na latitude e longitude
    //Coordenadas TV. paulo nogueira
    private GeoPoint pontoInicial = new GeoPoint(-2.2478831914134654, -49.505085540790915);
    // coordenadas UFPA cametá
    private GeoPoint pontoFinalUFPACameta = new GeoPoint(-2.246507196553819, -49.50431199886905);

    private Marker markerBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar o mapa
        Configuration.getInstance().setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);
        // Permitir operações de rede na thread principal (apenas para fins de demonstração) // necessario para desenhar rota
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Inicializar o mapa
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        // Configurar o controlador do mapa
        mapController = mapView.getController();
        mapController.setZoom(20);
        mapController.setCenter(pontoInicial);



        addMarkers();
        drawRoute();


    }

    private void addMarkers() {
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(pontoInicial);
        //startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pontoonibus64));
        startMarker.setTitle("Start Point");
        mapView.getOverlays().add(startMarker);

        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(pontoFinalUFPACameta);
        //endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pontoonibus64));
        endMarker.setTitle("End Point");
        mapView.getOverlays().add(endMarker);

        // Adding bus marker
        markerBus = new Marker(mapView);
        markerBus.setPosition(pontoInicial);
       // markerBus.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerBus.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.iconsonibus64));
        markerBus.setTitle("Bus");
        mapView.getOverlays().add(markerBus);
    }


    private void drawRoute() {
        RoadManager roadManager = new OSRMRoadManager(this, "MyApp/1.0");
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(pontoInicial);
        waypoints.add(pontoFinalUFPACameta);
        new Thread(() -> {
            Road road = roadManager.getRoad(waypoints);
            runOnUiThread(() -> {
                roadOverlay = RoadManager.buildRoadOverlay(road);

                // Configurar a cor, espesura e a transparência da linha da rota:
                Paint paint = roadOverlay.getOutlinePaint();
                paint.setStrokeWidth(10); // espesura da linha
                paint.setColor(Color.argb(120, 0, 100, 255));

                mapView.getOverlays().add(roadOverlay);


                animateBus(roadOverlay.getPoints());
            });
        }).start();
    }

    private void animateBus(ArrayList<GeoPoint> points) {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (index < points.size()) {
                    markerBus.setPosition(points.get(index));
                    mapView.invalidate();
                    index++;
                    handler.postDelayed(this, 500); // Atraso de 500 ms para animação suave
                }
            }
        };
        handler.post(runnable);
    }



    private void gerarRotasOSM(GeoPoint origem, GeoPoint destino) {
        // Gerente de estrada
        RoadManager roadManager = new OSRMRoadManager(getApplicationContext(), BuildConfig.LIBRARY_PACKAGE_NAME);
        ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);

        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(origem);
        waypoints.add(destino);

        // Recuperar o caminho entre esses pontos
        road = roadManager.getRoad(waypoints);

        // Criar uma Polyline com a forma de rota
        roadOverlay = RoadManager.buildRoadOverlay(road);
        // Configurar a cor, espessura e transparência da linha da rota
        Paint paint = roadOverlay.getOutlinePaint();
        paint.setStrokeWidth(10); // Espessura da linha
        paint.setColor(Color.argb(120, 0, 100, 255));
        // Adicionar a Polyline às sobreposições do mapa
        mapView.getOverlays().add(roadOverlay);
        // Atualizar o mapa
        mapView.invalidate();

        // Adicionar marcadores para os nós da rota
        Drawable nodeIcon = getResources().getDrawable(R.drawable.location_start);
        for (int i = 0; i < road.mNodes.size(); i++) {
            RoadNode node = road.mNodes.get(i);
            Marker nodeMarker = new Marker(mapView);
            nodeMarker.setPosition(node.mLocation);
            nodeMarker.setIcon(nodeIcon);
            nodeMarker.setTitle("Step " + i);
            mapView.getOverlays().add(nodeMarker);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSAO_REQUERIDA: {
                // Se a solicitação de permissão foi cancelada o array vem vazio.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permissão cedida, recria a activity para carregar o mapa, só será executado uma vez
                    this.recreate();

                }

            }
        }
    }
}