

// Add navigation for multiple truck routes
function updateOptimizationResults(results) {

    const routeColors = [
        '#3498db', '#e74c3c', '#27ae60', '#f1c40f', '#9b59b6',
        '#1abc9c', '#e67e22', '#34495e', '#ff69b4', '#7f8c8d'
    ];

    const resultsCard = document.querySelector('.card .card-header .fa-chart-line')?.closest('.card');
    if (!resultsCard || !results || !results.results || results.results.length === 0) return;

    let currentRouteIdx = 0;
    const totalRoutes = results.results.length;

    function renderRoute(idx) {
        const routeResult = results.results[idx];
        const borderColor = routeColors[idx % routeColors.length];
        let html = `
    <div class="d-flex justify-content-between align-items-center mb-3" style="border-bottom: 4px solid ${borderColor}; padding-bottom: 8px;">
        <h5 class="fw-bold mb-0">Route Summary</h5>
        <div class="d-flex align-items-center">
        <button class="btn btn-outline-secondary me-2" id="prevRouteBtn" aria-label="Previous truck route" ${idx === 0 ? 'disabled' : ''}>
            &larr;
        </button>
        <span>Truck ${idx + 1} of ${totalRoutes}</span>
        <button class="btn btn-outline-secondary ms-2" id="nextRouteBtn" aria-label="Next truck route" ${idx === totalRoutes - 1 ? 'disabled' : ''}>
            &rarr;
        </button>
        </div>
    </div>
    `;

        html += `
            <div class="mb-4">
            <div class="d-flex justify-content-between mb-2">
            <span>Truck #${idx + 1} Route</span>
            <span class="badge bg-primary">${routeResult.totalLoad || 0} pallets</span>
            </div>
            <div class="route-path">
            <div class="d-flex align-items-center mb-2">
                <span class="route-marker bg-success"></span>
                <span>Warehouse (Start)</span>
            </div>
            ${routeResult.route.slice(1, -1).map(i => `
            <div class="d-flex align-items-center mb-2">
                <span class="route-marker"></span>
                
                <span>${results.locations[i].address} - ${results.demand?.[i] ?? 0} pallets</span>
            </div>
            `).join('')}
            <div class="d-flex align-items-center">
                <span class="route-marker bg-danger"></span>
                <span>Warehouse (End)</span>
            </div>
            </div>
            </div>
            <div class="progress mb-3" style="height: 10px;">
            <div class="progress-bar bg-success" role="progressbar" style="width: 100%;" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100"></div>
            </div>
            <div class="row text-center mb-4">
            <div class="col-4">
            <div class="text-success fw-bold">${(routeResult.distance / 1000).toFixed(1)} km</div>
            <small>Distance</small>
            </div>
            <div class="col-4">
            <div class="text-primary fw-bold">${routeResult.duration ? (Math.floor(routeResult.duration / 3600) + "h " + Math.round((routeResult.duration % 3600) / 60) + "m") : "-"}</div>
            <small>Duration</small>
            </div>
            <div class="col-4">
            <div class="text-info fw-bold">${routeResult.cost ? "$" + routeResult.cost : "-"}</div>
            <small>Cost</small>
            </div>
            </div>
            
            `;
        resultsCard.querySelector('.card-body').innerHTML = html;

        // Attach event listeners for navigation
        const prevBtn = document.getElementById('prevRouteBtn');
        const nextBtn = document.getElementById('nextRouteBtn');
        if (prevBtn) prevBtn.onclick = () => { renderRoute(idx - 1); };
        if (nextBtn) nextBtn.onclick = () => { renderRoute(idx + 1); };
    }

    renderRoute(currentRouteIdx);
}


function drawRoutesFromResults(results) {
    clearRoutes();

    const routeColors = [
        '#3498db', '#e74c3c', '#27ae60', '#f1c40f', '#9b59b6',
        '#1abc9c', '#e67e22', '#34495e', '#ff69b4', '#7f8c8d'
    ];

    (results.results || results).forEach((routeResult, idx) => {
        const route = routeResult.route;
        const locations = results.locations;

        let latLngs;
        if (useEncodedPolyline && routeResult.encodedGeometry) {
            // Decode encoded polyline (assume [lng, lat] order, convert to [lat, lng])
            latLngs = polyline.decode(routeResult.encodedGeometry).map(([lat, lng]) => [lat, lng]);
        } else {
            latLngs = route.map(idx => [
                locations[idx].latitude,
                locations[idx].longitude
            ]);
        }

        const color = routeColors[idx % routeColors.length];

        const polylineLayer = L.polyline(latLngs, {
            color: color,
            weight: 5,
            opacity: 0.8
        }).addTo(map);
        routeLayers.push(polylineLayer);

        route.forEach((locIdx, stopIdx) => {
            const loc = locations[locIdx];
            const latlng = [loc.latitude, loc.longitude];

            // Customize icon for start/end or use a bigger marker for mid points
            const isStart = stopIdx === 0;
            const isEnd = stopIdx === route.length - 1;
            let iconHtml, iconSize;
            if (isStart) {
                iconHtml = '<div style="background-color: #27ae60; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white;"></div>';
                iconSize = [20, 20];
            } else if (isEnd) {
                iconHtml = '<div style="background-color: #e74c3c; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white;"></div>';
                iconSize = [20, 20];
            } else {
                iconHtml = '<div style="background-color: #3498db; width: 28px; height: 28px; border-radius: 50%; border: 4px solid white;"></div>';
                iconSize = [28, 28];
            }

            const marker = L.marker(latlng, {
                icon: L.divIcon({
                    className: 'stop-marker',
                    html: iconHtml,
                    iconSize: iconSize
                })
            }).addTo(map).bindPopup(`${loc.address}<br>Pallets: ${loc.demand || 0}`);

            routeLayers.push(marker);
        });

    });

    // Fit map to all routes
    let allBounds = [];
    (results.results || results).forEach(routeResult => {
        let latLngs;
        if (useEncodedPolyline && routeResult.encodedGeometry) {
            latLngs = polyline.decode(routeResult.encodedGeometry).map(([lat, lng]) => [lat, lng]);
        } else {
            const route = routeResult.route;
            const locations = results.locations;
            latLngs = route.map(idx => [
                locations[idx].latitude,
                locations[idx].longitude
            ]);
        }
        if (latLngs.length > 0) {
            allBounds.push(L.latLngBounds(latLngs));
        }
    });
    if (allBounds.length > 0) {
        // Combine all bounds into one
        let combinedBounds = allBounds[0];
        for (let i = 1; i < allBounds.length; i++) {
            combinedBounds = combinedBounds.extend(allBounds[i]);
        }
        map.fitBounds(combinedBounds);
    }
    currentVisRouteIdx = -1;
    updateRouteNavControls();
}

function showOnlyRoute(idx) {
    clearRoutes();
    routeLayers = [];
    const routeColors = [
        '#3498db', '#e74c3c', '#27ae60', '#f1c40f', '#9b59b6',
        '#1abc9c', '#e67e22', '#34495e', '#ff69b4', '#7f8c8d'
    ];
    const routeResult = (results.results || results)[idx];
    const route = routeResult.route;
    const locations = results.locations;
    let latLngs;
    if (useEncodedPolyline && routeResult.encodedGeometry) {
        latLngs = polyline.decode(routeResult.encodedGeometry).map(([lat, lng]) => [lat, lng]);
    } else {
        latLngs = route.map(idx => [
            locations[idx].latitude,
            locations[idx].longitude
        ]);
    }
    const color = routeColors[idx % routeColors.length];
    const polylineLayer = L.polyline(latLngs, {
        color: color,
        weight: 7,
        opacity: 1
    }).addTo(map);
    routeLayers.push(polylineLayer);

    // Only add markers at start and end points
    route.forEach((locIdx, stopIdx) => {
        const loc = locations[locIdx];
        const latlng = [loc.latitude, loc.longitude];

        // Customize icon for start/end or use a bigger marker for mid points
        const isStart = stopIdx === 0;
        const isEnd = stopIdx === route.length - 1;
        let iconHtml, iconSize;
        if (isStart) {
            iconHtml = '<div style="background-color: #27ae60; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white;"></div>';
            iconSize = [20, 20];
        } else if (isEnd) {
            iconHtml = '<div style="background-color: #e74c3c; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white;"></div>';
            iconSize = [20, 20];
        } else {
            iconHtml = '<div style="background-color: #3498db; width: 28px; height: 28px; border-radius: 50%; border: 4px solid white;"></div>';
            iconSize = [28, 28];
        }

        const marker = L.marker(latlng, {
            icon: L.divIcon({
                className: 'stop-marker',
                html: iconHtml,
                iconSize: iconSize
            })
        }).addTo(map).bindPopup(`${loc.address}<br>Pallets: ${loc.demand || 0}`);

        routeLayers.push(marker);
    });

    if (latLngs.length > 0) {
        map.fitBounds(latLngs);
    }
    currentVisRouteIdx = idx;
    updateRouteNavControls();
}

function drawSampleRoute() {
    // Sample coordinates for routes
    const warehouse = [39.7684, -86.1581];
    const coordinates = [
        [39.9612, -82.9988], // Columbus, OH
        [41.4993, -81.6944], // Cleveland, OH
        [39.1031, -84.5120], // Cincinnati, OH
        [39.7589, -84.1916], // Dayton, OH
        [40.4406, -79.9959], // Pittsburgh, PA;
    ];

    //Add markers for warehouse
    const marker = L.marker(warehouse, {
        icon: L.divIcon({
            className: 'warehouse-icon',
            html: '<div style="background-color: #27ae60; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 10px rgba(0,0,0,0.3);"></div>',
            iconSize: [20, 20]
        })
    }).addTo(map).bindPopup('Warehouse (Start/End Point)');
    routeLayers.push(marker);

    // Add markers for stops
    coordinates.forEach((coord, index) => {
        L.marker(coord).addTo(map).bindPopup(`Stop ${index + 1}`);
    });

    // Create and style routes
    const route1 = [warehouse, coordinates[0], coordinates[4], coordinates[3], warehouse];
    const route2 = [warehouse, coordinates[1], coordinates[2], warehouse];

    const polyline = L.polyline(route1, {
        color: '#3498db',
        weight: 5,
        opacity: 0.8
    }).addTo(map).bindPopup('Route 1: Warehouse → Columbus → Dayton → Cincinnati → Warehouse');
    routeLayers.push(polyline);

    const polyline1 = L.polyline(route2, {
        color: '#e74c3c',
        weight: 5,
        opacity: 0.8
        }).addTo(map).bindPopup('Route 2: Warehouse → Cleveland → Indianapolis → Warehouse');

    // Focus map on Indianapolis (warehouse)
    map.setView(warehouse, 8);
}

function updateRouteNavControls() {
    const routeData = results?.results || results;
    if (!Array.isArray(routeData)) return; // Prevent crashing on sample routes

    const totalRoutes = routeData.length;
    const label = document.getElementById('visRouteLabel');
    const prevBtn = document.getElementById('prevVisRouteBtn');
    const nextBtn = document.getElementById('nextVisRouteBtn');
    const showAllBtn = document.getElementById('showAllRoutesBtn');
    if (!label || !prevBtn || !nextBtn || !showAllBtn) return;

    if (currentVisRouteIdx === -1) {
        label.textContent = "All Routes";
        prevBtn.disabled = true;
        nextBtn.disabled = true;
        showAllBtn.classList.add('active');
    } else {
        const routeResult = routeData[currentVisRouteIdx];
        if (routeResult && routeResult.route && results.locations) {
            const stops = routeResult.route
                .map(idx => results.locations[idx]?.address)
                .filter(addr => !!addr);
            if (stops.length > 2) {
                label.textContent = `Truck ${currentVisRouteIdx + 1}`;
            } else if (stops.length === 2) {
                label.textContent = `Truck ${currentVisRouteIdx + 1}: ${stops[0]} → ${stops[1]}`;
            } else {
                label.textContent = `Truck ${currentVisRouteIdx + 1} of ${totalRoutes}`;
            }
        } else {
            label.textContent = `Truck ${currentVisRouteIdx + 1} of ${totalRoutes}`;
        }

        prevBtn.disabled = currentVisRouteIdx <= 0;
        nextBtn.disabled = currentVisRouteIdx >= totalRoutes - 1;
        showAllBtn.classList.remove('active');
    }
}

function clearRoutes() {
            routeLayers.forEach(layer => {
                map.removeLayer(layer);
            });
            routeLayers = [];
        }


        