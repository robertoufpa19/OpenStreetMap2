package com.example.openstreetmap2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import static android.os.Build.VERSION_CODES.M;
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSAO_REQUERIDA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         /// permissções de acessos
        if (Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissoes = {android.Manifest.permission.INTERNET, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissoes, PERMISSAO_REQUERIDA);
            }
        }

        // Configurar o mapa
        Configuration.getInstance().setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);

        //Pega o mapa adicionada no arquivo activity_main.xml
        MapView mapa = (MapView) findViewById(R.id.mapView);
        //Fonte de imagens
        mapa.setTileSource(TileSourceFactory.MAPNIK);

        //Cria um ponto de referência com base na latitude e longitude
        // Exemplo de coordenadas UFPA Belém: R. Augusto Corrêa, 01 - Guamá, Belém - PA, 66075-110
        GeoPoint pontoInicialUFPABelemGuama = new GeoPoint(-1.4755185896289333, -48.457223630625094);

        //Cria um ponto de referência final com base na latitude e longitude
        // Exemplo de coordenadas UFPA Belém: Tv. Três de Maio, 1573 - São Brás, Belém - PA, 66063-390
        GeoPoint pontoFinalUFPABelemSaoBras = new GeoPoint(-1.4534120639444839, -48.472881666124124);


        IMapController mapController = mapa.getController();
         //Faz zoom no mapa
        mapController.setZoom(15);
        //Centraliza o mapa no ponto de referência
        mapController.setCenter(pontoInicialUFPABelemGuama);

        //Cria um marcador no mapa ponto inicial
        Marker startMarker = new Marker(mapa);
        startMarker.setPosition(pontoInicialUFPABelemGuama);
        startMarker.setTitle("UFPA - Guamá");
        //Posição do ícone
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapa.getOverlays().add(startMarker);

        //Cria um marcador no mapa ponto final
        Marker andtMarker = new Marker(mapa);
        andtMarker.setPosition(pontoFinalUFPABelemSaoBras);
        andtMarker.setTitle("UFPA - São Brás");
        //Posição do ícone
        andtMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapa.getOverlays().add(andtMarker);

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