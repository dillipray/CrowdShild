package com.crowdshield.stampede.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Interactive Leaflet.js Map Composable for CrowdShield.
 * Renders OpenStreetMap / CartoDB tiles with GPS position, risk sectors, CCTV nodes, and egress paths.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMapView(
    modifier: Modifier = Modifier,
    latitude: Double = 28.6128,
    longitude: Double = 77.2290,
    zoomLevel: Int = 17,
    isStaffMode: Boolean = false,
    onSectorSelected: (String) -> Unit = {}
) {
    val jsBridge = remember {
        object {
            @JavascriptInterface
            fun onSectorClick(sectorId: String) {
                onSectorSelected(sectorId)
            }
        }
    }

    val leafletHtml = remember(latitude, longitude, zoomLevel, isStaffMode) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    width: 100%;
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background: #0f172a;
                }
                .user-pin {
                    width: 18px;
                    height: 18px;
                    border-radius: 50%;
                    background: #06b6d4;
                    border: 2px solid #ffffff;
                    box-shadow: 0 0 12px #06b6d4;
                }
                .exit-pin {
                    padding: 2px 6px;
                    border-radius: 4px;
                    background: #10b981;
                    color: white;
                    font-size: 10px;
                    font-weight: bold;
                    border: 1px solid white;
                    white-space: nowrap;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: true,
                    attributionControl: false
                }).setView([$latitude, $longitude], $zoomLevel);

                L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                    maxZoom: 19,
                    subdomains: 'abcd'
                }).addTo(map);

                // User / Officer GPS marker
                var userIcon = L.divIcon({
                    className: 'custom-pin',
                    html: '<div class="user-pin"></div>',
                    iconSize: [18, 18],
                    iconAnchor: [9, 9]
                });
                L.marker([$latitude, $longitude], {icon: userIcon}).addTo(map)
                    .bindPopup('<b>Your Live Position</b><br>Accuracy: ~5m').openPopup();

                // Sectors
                var sectors = [
                    { id: 'Sector 1', name: 'Sector 1 - North Gate', color: '#10b981', coords: [[28.6148, 77.2280], [28.6148, 77.2312], [28.6134, 77.2312], [28.6134, 77.2280]] },
                    { id: 'Sector 2', name: 'Sector 2 - West Plaza', color: '#10b981', coords: [[28.6134, 77.2262], [28.6134, 77.2290], [28.6118, 77.2290], [28.6118, 77.2262]] },
                    { id: 'Sector 3', name: 'Sector 3 - Main Stage', color: '#f59e0b', coords: [[28.6134, 77.2295], [28.6134, 77.2328], [28.6118, 77.2328], [28.6118, 77.2295]] },
                    { id: 'Sector 4', name: 'Sector 4 - East Concourse', color: '#ef4444', coords: [[28.6115, 77.2280], [28.6115, 77.2312], [28.6098, 77.2312], [28.6098, 77.2280]] }
                ];

                sectors.forEach(function(sec) {
                    var poly = L.polygon(sec.coords, {
                        color: sec.color,
                        weight: 2,
                        fillColor: sec.color,
                        fillOpacity: 0.25
                    }).addTo(map);

                    poly.on('click', function() {
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onSectorClick(sec.id);
                        }
                    });
                    poly.bindTooltip(sec.name);
                });

                // Emergency Exits
                var exitIcon = L.divIcon({
                    className: 'custom-exit',
                    html: '<div class="exit-pin">🚪 Exit B (Safe)</div>',
                    iconSize: [80, 20],
                    iconAnchor: [40, 10]
                });
                L.marker([28.6126, 77.2260], {icon: exitIcon}).addTo(map)
                    .bindPopup('<b>Exit B - West Concourse</b><br>Status: Optimal / Safe');

                // Dynamic Route
                var path = [[$latitude, $longitude], [28.6128, 77.2280], [28.6126, 77.2260]];
                L.polyline(path, {color: '#10b981', weight: 4, dashArray: '6, 8'}).addTo(map);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                addJavascriptInterface(jsBridge, "AndroidBridge")
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://crowdshield.local", leafletHtml, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://crowdshield.local", leafletHtml, "text/html", "UTF-8", null)
        },
        modifier = modifier.fillMaxSize()
    )
}
