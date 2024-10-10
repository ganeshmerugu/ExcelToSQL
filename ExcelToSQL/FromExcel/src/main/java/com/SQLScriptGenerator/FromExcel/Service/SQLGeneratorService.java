package com.SQLScriptGenerator.FromExcel.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SQLGeneratorService {

    private final Configuration freeMarkerConfiguration;

    public SQLGeneratorService(Configuration freeMarkerConfiguration) {
        this.freeMarkerConfiguration = freeMarkerConfiguration;
    }

    // Process a single sheet and generate SQL for it
    public List<String> processAndSaveFile(MultipartFile file) throws IOException, TemplateException {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        List<String> sqlFilePaths = new ArrayList<>();

        // Ensure the output directory exists
        createOutputDirectory("output");

        // Loop through each sheet and generate SQL
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String tableName = sheet.getSheetName();

            // Extract columns and rows from the sheet
            Map<String, Object> dataModel = extractDataFromSheet(sheet);
            dataModel.put("tableName", tableName);

            // Generate SQL file for the table
            String outputPath = "output/" + tableName + ".sql";
            generateSQLScript(dataModel, outputPath);
            sqlFilePaths.add(outputPath);
        }
        return sqlFilePaths;
    }

    // Process all sheets and generate a combined SQL script for all tables
    public String processAndSaveAllTables(MultipartFile file) throws IOException, TemplateException {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        List<Map<String, Object>> allTablesData = new ArrayList<>();

        // Ensure the output directory exists
        createOutputDirectory("output");

        // Loop through each sheet and extract data
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String tableName = sheet.getSheetName();

            // Extract columns and rows from the sheet
            Map<String, Object> dataModel = extractDataFromSheet(sheet);
            dataModel.put("tableName", tableName);
            allTablesData.add(dataModel);
        }

        // Combine all table data into a single data model
        Map<String, Object> combinedDataModel = new HashMap<>();
        combinedDataModel.put("tables", allTablesData);

        // Generate a combined SQL file for all tables
        String combinedSqlFilePath = "output/combined_tables.sql";
        generateCombinedSQLScript(combinedDataModel, combinedSqlFilePath);
        return combinedSqlFilePath;
    }

    // Helper method to extract data from a sheet
    private Map<String, Object> extractDataFromSheet(Sheet sheet) {
        Map<String, Object> dataModel = new HashMap<>();
        List<String> columns = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {  // Check if the header row exists
            for (Cell cell : headerRow) {
                columns.add(cell.getStringCellValue());
            }
        }

        dataModel.put("columns", columns);

        // Iterate over the rows, starting from 1 (skipping header)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                // Skip this row if it's null
                continue;
            }

            List<Object> rowData = new ArrayList<>();
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING:
                        rowData.add(cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        rowData.add(cell.getNumericCellValue());
                        break;
                    case BOOLEAN:
                        rowData.add(cell.getBooleanCellValue());
                        break;
                    case BLANK:
                        rowData.add(null);
                        break;
                    default:
                        rowData.add(cell.toString());
                }
            }
            rows.add(rowData);
        }

        dataModel.put("rows", rows);
        return dataModel;
    }

    // Helper method to generate individual SQL scripts for each table
    public void generateSQLScript(Map<String, Object> dataModel, String outputPath) throws IOException, TemplateException {
        Template template = freeMarkerConfiguration.getTemplate("create_table.ftl");
        try (FileWriter fileWriter = new FileWriter(outputPath)) {
            template.process(dataModel, fileWriter);
        }
    }

    // Helper method to generate a combined SQL script for all tables
    public void generateCombinedSQLScript(Map<String, Object> combinedDataModel, String outputPath) throws IOException, TemplateException {
        Template template = freeMarkerConfiguration.getTemplate("combined_tables.ftl");
        try (FileWriter fileWriter = new FileWriter(outputPath)) {
            template.process(combinedDataModel, fileWriter);
        }
    }

    // Ensure the output directory exists
    private void createOutputDirectory(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            Files.createDirectories(Paths.get(directoryPath));
        }
    }
}
