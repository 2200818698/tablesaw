package com.deathrayresearch.outlier.examples;

import au.com.bytecode.opencsv.CSVWriter;
import com.deathrayresearch.outlier.Table;
import com.deathrayresearch.outlier.api.ColumnType;
import com.deathrayresearch.outlier.columns.CategoryColumn;
import com.deathrayresearch.outlier.columns.FloatColumn;
import com.deathrayresearch.outlier.columns.IntColumn;
import com.deathrayresearch.outlier.columns.LocalDateColumn;
import com.deathrayresearch.outlier.columns.packeddata.PackedLocalDate;
import com.deathrayresearch.outlier.io.CsvReader;
import com.deathrayresearch.outlier.io.CsvWriter;
import com.deathrayresearch.outlier.store.StorageManager;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.deathrayresearch.outlier.api.ColumnType.*;
import static com.deathrayresearch.outlier.api.QueryHelper.*;

/**
 * Tests manipulation of large (but not big) data sets
 */
public class ObservationDataTest {

  private static final String CSV_FILE = "/Users/larrywhite/IdeaProjects/testdata/obs.csv";
  private static final String DB = "/Users/larrywhite/IdeaProjects/testdata/obs.db";

  // pools to get random test data from
  private static List<String> concepts = new ArrayList<>(100_000);
  private static IntArrayList patientIds = new IntArrayList(1_000_000);
  private static int size = 60 * 365;
  private static IntArrayList dates = new IntArrayList(size);


  public static void main(String[] args) throws Exception {

    // generate data
    Table t = new Table("Observations");
    CategoryColumn conceptId = CategoryColumn.create("concept");
    LocalDateColumn date = LocalDateColumn.create("date");
    FloatColumn value = FloatColumn.create("value");
    IntColumn patientId = IntColumn.create("patient");

    t.addColumn(conceptId);
    t.addColumn(date);
    t.addColumn(value);
    t.addColumn(patientId);

    Stopwatch stopwatch = Stopwatch.createStarted();

    int numberOfRecordsInTable = 500_000_000;

//    System.out.println("Generating test data");
//    generateData(numberOfRecordsInTable, t);

//    System.out.println("Time to generate " + numberOfRecordsInTable + " records: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");


    stopwatch.reset().start();

    // ConceptId, Date, Value, PatientNo
    ColumnType[] columnTypes = {CATEGORY, LOCAL_DATE, FLOAT, INTEGER};
    t = CsvReader.read(columnTypes, CSV_FILE);
    System.out.println("Time to read to CSV File " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");

    stopwatch = stopwatch.reset().start();
    storeInDb(t);
    System.out.println("Time to write out in columnStore format " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");

    String randomConcept = t.categoryColumn("concept").get(RandomUtils.nextInt(0, t.rowCount()));

    stopwatch.reset().start();
    Table result = t.selectIf(
        column("concept").isEqualTo(randomConcept));
    System.out.println("concept found in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
    System.out.println(result.print());

  }

  private static void storeInDb(Table t) throws Exception {
    StorageManager.saveTable(DB, t);
  }

  private static void generateData(int observationCount, Table table) throws IOException {
    // create pools of random values

    while (concepts.size() <= 100_000) {
      concepts.add(RandomStringUtils.randomAscii(30));
    }

    while (patientIds.size() <= 1_000_000) {
      patientIds.add(RandomUtils.nextInt(0, 2_000_000_000));
    }

    while (dates.size() <= size){
      dates.add(PackedLocalDate.pack(randomDate()));
    }

    LocalDateColumn dateColumn = table.localDateColumn("date");
    CategoryColumn conceptColumn = table.categoryColumn("concept");
    FloatColumn valueColumn = table.floatColumn("value");
    IntColumn patientColumn = table.intColumn("patient");

    CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE));
    String[] line = new String[4];
    String[] header = {"concept", "date", "value", "patient"};

    writer.writeNext(header);
    // sample from the pools to write the data
    for (int i = 0; i < observationCount; i++) {
      line[0] = concepts.get(RandomUtils.nextInt(0, concepts.size()));
      line[1] = PackedLocalDate.toDateString(dates.getInt(RandomUtils.nextInt(0, dates.size())));
      line[2] = Float.toString(RandomUtils.nextFloat(0f, 100_000f));
      line[3] = Integer.toString(patientIds.getInt(RandomUtils.nextInt(0, patientIds.size())));
      writer.writeNext(line);
/*
      dateColumn.add(dates.getInt(RandomUtils.nextInt(0, dates.size())));
      conceptColumn.add(concepts.get(RandomUtils.nextInt(0, concepts.size())));
      valueColumn.add(RandomUtils.nextFloat(0f, 100_000f));
      patientColumn.add(patientIds.getInt(RandomUtils.nextInt(0, patientIds.size())));
*/
    }
    writer.flush();
    writer.close();
    concepts = null;
    patientIds = null;
    dates = null;
  }

  private static LocalDate randomDate() {
    Random random = new Random();
    int minDay = (int) LocalDate.of(1920, 1, 1).toEpochDay();
    int maxDay = (int) LocalDate.of(2016, 1, 1).toEpochDay();
    long randomDay = minDay + random.nextInt(maxDay - minDay);
    return LocalDate.ofEpochDay(randomDay);
  }
}
