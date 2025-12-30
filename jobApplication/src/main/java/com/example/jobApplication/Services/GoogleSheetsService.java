package com.example.jobApplication.Services;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

import com.example.jobApplication.Entity.JobData;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.annotation.PostConstruct;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Job Application Tracker";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private String credentialsPath = System.getenv("GOOGLE_SHEETS_CREDENTIALS");;
    private String spreadsheetId = System.getenv("GOOGLE_SHEETS_SPREADSHEET_ID");
    private Sheets sheetsService;

    @PostConstruct
    public void init() throws Exception {
        InputStream in = new FileInputStream(credentialsPath);

        GoogleCredential credential = GoogleCredential.fromStream(in)
                .createScoped(List.of(SheetsScopes.SPREADSHEETS));

        sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void appendRows(List<JobData> jobs) throws Exception {
        List<List<Object>> values = mapJobsToSheetRows(jobs);
        ValueRange body = new ValueRange().setValues(values);
        System.out.println("spreadsheetId - " + spreadsheetId);
        sheetsService.spreadsheets().values()
                .append(spreadsheetId, "Sheet1!A1", body)
                .setValueInputOption("RAW")
                .execute();
    }

    private List<List<Object>> mapJobsToSheetRows(List<JobData> jobs) {
        List<List<Object>> rows = new ArrayList<>();

        rows.add(List.of(
                "Role", "Company", "Location", "Description",
                "Emails", "Job URL", "Discovered At"));

        for (JobData job : jobs) {
            List<Object> row = new ArrayList<>();
            row.add(job.getRole() != null ? job.getRole() : "");
            row.add(job.getCompany() != null ? job.getCompany() : "");
            row.add(job.getLocation() != null ? job.getLocation() : "");
            row.add(job.getDescription() != null ? job.getDescription() : "");
            row.add(job.getEmails() != null ? job.getEmails() : "");
            row.add(job.getJobUrl() != null ? job.getJobUrl() : "");
            row.add(job.getPosted() != null ? job.getPosted() : "");

            rows.add(row);
        }
        return rows;
    }
}
