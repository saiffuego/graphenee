package io.graphenee.vaadin.flow.component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;

import org.apache.commons.io.IOUtils;
import org.springframework.util.StreamUtils;

import io.graphenee.core.storage.FileStorage;
import io.graphenee.core.util.TRFileContentUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
public class FileUploader extends CustomField<String> {
    private static final long serialVersionUID = 1L;

    private String[] allowedFileTypes = null;
    private Boolean autoUpload = true;
    private String dropFileLabel = "Drop files here";

    //5MB
    private int maxFileSize = 5048576;

    @Setter
    @Getter
    private String label;

    private Upload upload;

    @Getter
    private String uploadedFilePath;

    @Getter
    private String uploadedFileName;

    @Getter
    private String uploadedFileMimeType;

    @Setter
    @Getter
    private String rootFolder;

    @Setter
    @Getter
    private FileStorage storage;

    private Div output;
    private MemoryBuffer buffer;

    public FileUploader() {
        this(null);
    }

    public FileUploader(String label) {
        this.label = label;
        build();
    }

    private void build() {
        output = new Div();
        buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setDropLabel(new Label(dropFileLabel));

        if (allowedFileTypes != null) {
            upload.setAcceptedFileTypes(allowedFileTypes);
        }

        upload.setMaxFileSize(maxFileSize);

        upload.addSucceededListener(event -> {
            String ext = TRFileContentUtil.getExtensionFromFilename(event.getFileName());
            String storageFileName = UUID.randomUUID().toString() + "." + ext;

            String desiredFileName = event.getFileName();

            try {
                File tempFile = File.createTempFile("uploaded", ext);
                uploadedFilePath = tempFile.getAbsolutePath();
                uploadedFileName = event.getFileName();
                uploadedFileMimeType = event.getMIMEType();

                try (FileOutputStream os = new FileOutputStream(tempFile)) {
                    InputStream is = buffer.getInputStream();
                    StreamUtils.copy(is, os);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                File receivedFile = new File(uploadedFilePath);
                File newFile = new File(receivedFile.getParent(), storageFileName);
                receivedFile.renameTo(newFile);
                receivedFile = newFile;
                uploadedFilePath = receivedFile.getAbsolutePath();
                uploadedFileName = desiredFileName;

                uploadedFileName = new File(uploadedFilePath).getName();
                String uploadedFileExtension = TRFileContentUtil.getExtensionFromFilename(uploadedFileName);
                if (!desiredFileName.endsWith(uploadedFileExtension)) {
                    uploadedFileName = desiredFileName + "." + uploadedFileExtension;
                } else {
                    uploadedFileName = desiredFileName;
                }

                setValue(uploadedFilePath);

            } catch (IOException e) {
                log.warn(e.getMessage());
            }

        });

        upload.addFileRejectedListener(event -> {
            Paragraph component = new Paragraph();
            showOutput(event.getErrorMessage(), component, output);
        });

        upload.getElement().addEventListener("file-remove", event -> {
            setValue(null);
            output.removeAll();
        });

        if (label != null) {
            add(new Label(label));
        }

        add(upload, output);
    }

    public void setAllowedFileTypes(String... fileTypes) {
        this.allowedFileTypes = fileTypes;
        upload.setAcceptedFileTypes(allowedFileTypes);
    }

    public void setMaxFiles(int maxFiles) {
        upload.setMaxFiles(maxFiles);
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
        upload.setMaxFileSize(this.maxFileSize);
    }

    public void setAutoUpload(Boolean autoUpload) {
        this.autoUpload = autoUpload;
        upload.setAutoUpload(this.autoUpload);
    }

    public void setDropFileLabel(String dropFileLabel) {
        this.dropFileLabel = dropFileLabel;
        upload.setDropLabel(new Label(dropFileLabel));
    }

    @Override
    protected String generateModelValue() {
        return getValue();
    }

    @Override
    protected void setPresentationValue(String newPresentationValue) {
        if (newPresentationValue != null) {
            Component component = createComponent(newPresentationValue);
            showOutput(null, component, output);
        } else {
            output.removeAll();
        }
    }

    private Component createComponent(String fileName) {
        if (fileName != null) {
            String mimeType = TRFileContentUtil.getMimeType(fileName);
            if (mimeType.startsWith("image")) {
                Image image = new Image();
                image.setWidth("100px");
                image.setHeight("100px");
                try {
                    InputStream stream = null;
                    String resourcePath = storage.resourcePath(getRootFolder(), fileName);
                    try {
                        stream = storage.resolve(resourcePath);
                    } catch (Exception e) {
                        stream = buffer.getInputStream();
                        fileName = buffer.getFileName();
                    }
                    byte[] bytes = IOUtils.toByteArray(stream);
                    image.getElement().setAttribute("src", new StreamResource(fileName, () -> new ByteArrayInputStream(bytes)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return image;
            }
        }
        return null;
    }

    private void showOutput(String text, Component content, HasComponents outputContainer) {
        if (text != null) {
            HtmlComponent p = new HtmlComponent(Tag.P);
            p.getElement().setText(text);
            outputContainer.add(p);
        }
        if (content != null) {
            outputContainer.removeAll();
            outputContainer.add(content);
        }
    }

}
