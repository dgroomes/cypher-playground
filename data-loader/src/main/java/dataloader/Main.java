package dataloader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * Read the 'zips.jsonl' file line-by-line, parse each line as a JSON object, insert city rows as needed, and insert
 * a ZIP code row for each line.
 */
public class Main {

  public static void main(String[] args) throws SQLException {
    var log = LoggerFactory.getLogger(Main.class);
    log.info("Loading ZIP code data from the local file into Postgres ...");
    record Zip(String zipCode, String cityName, String stateCode, int population) {}
    record City(String name, String stateCode) {}

    Map<City, List<Zip>> citiesToZips;
    // Read the ZIP code data from the local JSON file. The cities are also inferred from the ZIP code data.
    {
      JsonMapper jsonMapper = JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();

      File zipsFile = new File("zips.jsonl");
      if (!zipsFile.exists()) {
        String msg = "The 'zips.jsonl' file could not be found (%s). You need to run this program from the root of the 'data-loader' module.".formatted(zipsFile.getAbsolutePath());
        throw new RuntimeException(msg);
      }

      try (Stream<String> zipsJsonLines = Files.lines(zipsFile.toPath())) {
        citiesToZips = zipsJsonLines.map(zipJson -> {
          JsonNode zipNode;
          try {
            zipNode = jsonMapper.readTree(zipJson);
          } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize the JSON representing a ZIP code", e);
          }

          return new Zip(zipNode.get("_id").asText(), zipNode.get("city").asText(), zipNode.get("state").asText(), zipNode.get("pop").asInt());
        }).collect(groupingBy(zip -> new City(zip.cityName, zip.stateCode)));
      } catch (IOException e) {
        throw new RuntimeException("There was an error while reading the ZIP data from the file.", e);
      }
    }

    // Insert the ZIP and city data into the database.
    try (var connection = DriverManager.getConnection("jdbc:postgresql:postgres", "postgres", null);
         var insertCityStmt = connection.prepareStatement("INSERT INTO cities (city_name, state_code) VALUES (?, ?) returning id");
         var insertZipStmt = connection.prepareStatement("INSERT INTO zip_codes (zip_code, city_id, population) VALUES (?, ?, ?)")) {

      int loadedCities = 0;
      int loadedZips = 0;

      for (Map.Entry<City, List<Zip>> cityListEntry : citiesToZips.entrySet()) {
        City city = cityListEntry.getKey();
        int cityId;
        {
          log.trace("Inserting city {} ...", city);
          insertCityStmt.setString(1, city.name);
          insertCityStmt.setString(2, city.stateCode);
          ResultSet resultSet = insertCityStmt.executeQuery();
          if (!resultSet.next()) throw new IllegalStateException("Expected a result set but didn't find one.");
          cityId = resultSet.getInt("id");
          loadedCities++;
        }

        List<Zip> zips = cityListEntry.getValue();
        for (Zip zip : zips) {
          log.trace("Inserting ZIP {} ...", zip);
          insertZipStmt.setString(1, zip.zipCode);
          insertZipStmt.setInt(2, cityId);
          insertZipStmt.setInt(3, zip.population);
          insertZipStmt.execute();
          loadedZips++;
        }
      }
      log.info("Loaded %,d cities and %,d ZIP codes.".formatted(loadedCities, loadedZips));
    } catch (SQLException e) {
      throw new RuntimeException("Something went wrong while inserting data into the Postgres database", e);
    }
  }
}
