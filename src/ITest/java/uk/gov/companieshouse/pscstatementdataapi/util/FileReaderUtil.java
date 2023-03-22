package uk.gov.companieshouse.pscstatementdataapi.util;

import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReaderUtil {

    public static String readFile(String path) {
        String data;
        try {
            data = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(new File(path))));
        } catch (IOException e) {
            data = null;
        }
        return data;
    }
}
