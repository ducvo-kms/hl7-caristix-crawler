package com.ducvo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileService {
  private final String baseUrl;

  public FileService(String baseUrl){
    this.baseUrl = baseUrl;
  }

  public void write(String name, String content, String directory) throws IOException {
    var directoryPath = Path.of(baseUrl, directory);
    if(!Files.exists(directoryPath)){
      Files.createDirectories(directoryPath);
    }

    var filePath = Path.of(directoryPath.toString(), name);

    Files.writeString(filePath, content);
  }
}
