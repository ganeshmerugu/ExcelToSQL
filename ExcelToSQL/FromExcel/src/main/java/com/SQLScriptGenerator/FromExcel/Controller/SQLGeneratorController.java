package com.SQLScriptGenerator.FromExcel.Controller;

import com.SQLScriptGenerator.FromExcel.Service.SQLGeneratorService;
import freemarker.template.TemplateException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/sqlConverter")
public class SQLGeneratorController {

    private final SQLGeneratorService sqlGeneratorService;

    public SQLGeneratorController(SQLGeneratorService sqlGeneratorService) {
        this.sqlGeneratorService = sqlGeneratorService;
    }

    // Upload file and generate SQL for individual tables
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            List<String> sqlFilePaths = sqlGeneratorService.processAndSaveFile(file);
            return ResponseEntity.ok(sqlFilePaths);
        } catch (IOException | TemplateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Upload file and generate combined SQL for all tables
    @PostMapping("/upload/all")
    public ResponseEntity<String> uploadFileAndCreateAllTables(@RequestParam("file") MultipartFile file) {
        try {
            String combinedSqlFilePath = sqlGeneratorService.processAndSaveAllTables(file);
            return ResponseEntity.ok("All tables processed. Download SQL: /sqlizer/download/all/" + combinedSqlFilePath);
        } catch (IOException | TemplateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process tables: " + e.getMessage());
        }
    }

    // Download SQL file for individual tables
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadSQL(@PathVariable String fileName) {
        try {
            File sqlFile = new File("output/" + fileName);
            Path path = Paths.get(sqlFile.getAbsolutePath());
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/sql"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + sqlFile.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Download combined SQL file for all tables
    @GetMapping("/download/all/{fileName}")
    public ResponseEntity<Resource> downloadCombinedSQL(@PathVariable String fileName) {
        return downloadSQL(fileName);  // Reuse the same download method
    }
}
