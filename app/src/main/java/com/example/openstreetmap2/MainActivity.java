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

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSAO_REQUERIDA = 1;


    private Polyline roadOverlay;
    private Road road;
    private MapView mapView;
    private IMapController mapController;

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
        mapController.setZoom(15);
        mapView.invalidate();

            //Cria um ponto de referência com base na latitude e longitude
            // Exemplo de coordenadas UFPA Belém: R. Augusto Corrêa, 01 - Guamá, Belém - PA, 66075-110
            GeoPoint pontoInicialUFPABelemGuama = new GeoPoint(-1.4755185896289333, -48.457223630625094);

            //Cria um ponto de referência final com base na latitude e longitude
            // Exemplo de coordenadas UFPA Belém: Tv. Três de Maio, 1573 - São Brás, Belém - PA, 66063-390
            GeoPoint pontoFinalUFPABelemSaoBras = new GeoPoint(-1.4534120639444839, -48.472881666124124);



            //Centraliza o mapa no ponto de referência
            mapController.setCenter(pontoInicialUFPABelemGuama);

            //Cria um marcador no mapa ponto inicial
            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(pontoInicialUFPABelemGuama);
            startMarker.setTitle("UFPA - Guamá");
            //Posição do ícone
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(startMarker);

            //Cria um marcador no mapa ponto final
            Marker andtMarker = new Marker(mapView);
            andtMarker.setPosition(pontoFinalUFPABelemSaoBras);
            andtMarker.setTitle("UFPA - São Brás");
            //Posição do ícone
            andtMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(andtMarker);


        gerarRotasOSM(pontoInicialUFPABelemGuama, pontoFinalUFPABelemSaoBras);


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