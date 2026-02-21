package fr._42.cinema.models;

public class Image 
{
    private Long id;
    private String filename;
    private String filepath;
    private Long userId;
    private String fileSize;

    public Image() {
    }

    public Image(Long id, String filename, String filepath, Long userId, String fileSize) {
        this.id = id;
        this.filename = filename;
        this.filepath = filepath;
        this.userId = userId;
        this.fileSize = fileSize;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public Long getUserId() {
        return userId;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}