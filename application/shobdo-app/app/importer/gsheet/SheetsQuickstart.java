package importer.gsheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import exceptions.EntityDoesNotExist;
import word.objects.Meaning;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

// Following https://developers.google.com/sheets/api/quickstart/java

public class SheetsQuickstart {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/gcred/credentials.json";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void get(String spreadsheetID) throws IOException, GeneralSecurityException {
        final String range = "Data!A1:J";

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();

        ValueRange response = service.spreadsheets().values()
            .get(spreadsheetID, range)
            .execute();

        Map<String, List<Meaning>> wordStrToMeaningListMap = new HashMap<>();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            System.out.printf("Total $d rows found for spreadsheetID: %s\n", values.size(), spreadsheetID);
            for (List row : values) {
                WordMeaning wordMeaning = getWordMeaning(row);
                List<Meaning> meanings = wordStrToMeaningListMap.computeIfAbsent(wordMeaning.getWord(), v -> new ArrayList<>());
                meanings.add(wordMeaning.getMeaning());
            }
        }
    }

    public static WordMeaning getWordMeaning(List<Object> row) throws EntityDoesNotExist {
        if (row.size() < 9) {
            // TODO - we found some data-set are not complete and needs to be fixed
            System.out.println("Row does not include part of speech");
            if (row.size() < 3) {
                System.out.println("Row does not contain enough data");
                throw new EntityDoesNotExist("Row does not contain enough data to return corresponding meaning. Row: " + row);
            }
        }

        String count = row.size() > 0? row.get(0).toString() : "-NOT-FOUND-";
        String ref = row.size() > 1? row.get(1).toString() : "-NOT-FOUND-";

        String word = row.size() > 3? row.get(3).toString() : "-NOT-FOUND-";
        String meaning = row.size() > 4? row.get(4).toString() : "-NOT-FOUND-";
        String exampleSentence = row.size() > 5? row.get(5).toString() : "-NOT-FOUND-";
        String partOfSpeech = row.size() > 8? row.get(8).toString() : "-NOT-FOUND-";

        String synonyms = row.size() > 6? row.get(6).toString() : "-NOT-FOUND-";
        String antonyms =  row.size() > 7? row.get(7).toString() : "-NOT-FOUND-";
        String extra = row.size() > 9? row.get(9).toString() : "-NOT-FOUND-";

        System.out.println("Row Size: " + row.size() + " CountStr: " + count + " wordStr:" + word
            + " meaning:" + meaning + " exampleSentence:" + exampleSentence
            + " synonyms:" + synonyms + " antonyms:" + antonyms + " partOfSpeech:" + partOfSpeech);

        return importer.gsheet.WordMeaning.builder()
            .word(word)
            .meaning(Meaning.builder()
                .meaning(meaning)
                .exampleSentence(exampleSentence)
                .partOfSpeech(partOfSpeech)
                .strength(0)
                .pronunciation("NA")
                .build())
            .build();
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        String[] sheetIds = {
        };
        /*
        for (String spreadsheetId: sheetIds) {
            get(spreadsheetId);
        }
         */
    }
}
