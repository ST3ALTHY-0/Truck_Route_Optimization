# Truck Route Optimization

This project is a web application for optimizing delivery routes for trucks. It uses OpenRouteService (ORS) for route calculations and supports visualization of optimized routes on an interactive map.

## FIX
- make time constraints a advanced mode thing
- make all the default numbers in the front end based off of passed object from backend instead of setting it in frontend code

- when going to anywhere in wisconsin we go through michigan peninsala. I think
this is because we dont include illinois in the map we use in the ORS api. Need to try adding illinois to the ors map


## Features

- Input addresses and demands for each delivery location
- Specify truck capacities and number of trucks
- Optimize routes to minimize distance or time
- Visualize all routes or step through individual truck routes
- View route directions using encoded geometry from ORS
- Error handling and user-friendly feedback

## Technologies Used

- Java 21 (Spring Boot)
- Thymeleaf (for server-side rendering)
- OpenRouteService API (for routing and directions)
- Leaflet.js (for map visualization)
- Bootstrap 5 (for UI)
- Mapbox Polyline (for decoding encoded geometry)
- Docker (for running ORS locally, optional)

## Getting Started

### Prerequisites

- Java 21+
- Maven
- Node.js (optional, for frontend build tools)
- Docker (optional, for running ORS locally)

### Setup

1. **Clone the repository:**
   ```sh
   git clone https://github.com/yourusername/truck-optimizer.git
   cd truck-optimizer
   ```

2. **Configure OpenRouteService:**
   - Set up your ORS instance (locally via Docker or use the public API).
   - Update your ORS API key in `application.properties`:
     ```
     ors.api.key=YOUR_ORS_API_KEY
     ```

3. **Build and run the application:**
   ```sh
   mvn spring-boot:run
   ```

4. **Access the app:**
   - Open [http://localhost:8082](http://localhost:8082) in your browser.

### Using the App

- Enter the depot and delivery addresses, with demands (first demand must be 0 for the depot).
- Set truck capacity and number of trucks.
- Click "Optimize" to calculate routes.
- Use the map to visualize all routes or step through each truck’s route.
- View route directions and details.

## Folder Structure

```
src/
  main/
    java/
      com.truckoptimization/
        controller/
        model/
        service/
    resources/
      static/
        javascript/
      templates/
        home.html
      application.properties
```

## Customization

- Adjust ORS settings in `ors-config.yml` if running your own ORS instance.

## License

MIT License

---

**Contributions welcome!**  
Feel free to open issues or submit pull requests.
